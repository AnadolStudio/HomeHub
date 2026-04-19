package com.anadolstudio.template.core.websocket

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

class WebSocketCoreImpl @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val parser: WebSocketMessageParser,
    private val json: Json,
    private val logger: WebSocketLogger,
    private val authRefresher: WebSocketAuthRefresher,
) : WebSocketCore {

    private var scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _connectionState = MutableStateFlow<WebSocketConnectionState>(WebSocketConnectionState.Disconnected)
    override val connectionState: StateFlow<WebSocketConnectionState> = _connectionState.asStateFlow()

    private val connectionMutex = Mutex()
    private val messageIdCounter = AtomicLong(1)
    private val pendingRequests = ConcurrentHashMap<Long, CompletableDeferred<WsMessage>>()
    private val closedManually = AtomicBoolean(false)

    private val _incomingMessages = MutableSharedFlow<WsMessage>(
        replay = 0,
        extraBufferCapacity = 256,
    )

    private var webSocket: WebSocket? = null
    private var currentUrl: String? = null
    private var currentAccessToken: String? = null
    private var config: WebSocketConfig = WebSocketConfig()
    private var pingJob: Job? = null
    private var reconnectJob: Job? = null

    private val listener = object : WebSocketListener() {

        override fun onOpen(webSocket: WebSocket, response: Response) {
            if (isStale(webSocket)) return
            logger.info(TAG, "WebSocket opened")
            _connectionState.value = WebSocketConnectionState.ConnectedUnauthenticated
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            if (isStale(webSocket)) return
            logger.debug(TAG, "Received: $text")
            val message = parser.parse(text)
            routeMessage(message)
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            if (isStale(webSocket)) return
            logger.info(TAG, "WebSocket closing: code=$code, reason=$reason")
            webSocket.close(code, reason)
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            if (isStale(webSocket)) return
            logger.info(TAG, "WebSocket closed: code=$code, reason=$reason")
            handleDisconnect(code)
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            if (isStale(webSocket)) return
            logger.error(TAG, "WebSocket failure", t)
            val exception = WebSocketCoreException.ConnectionError("WebSocket failure: ${t.message}", t)
            failAllPending(exception)

            if (closedManually.get()) {
                _connectionState.value = WebSocketConnectionState.Disconnected
            } else {
                _connectionState.value = WebSocketConnectionState.Failed(exception)
                attemptReconnect()
            }
        }
    }

    /**
     * Возвращает true, если колбэк пришёл от отвергнутого (старого) сокета.
     * Нужно после close() + пересоздания WS при full reconnect'е (например,
     * после refresh токена), чтобы stale-колбэки не ломали новое соединение.
     */
    private fun isStale(ws: WebSocket): Boolean = ws !== webSocket

    fun setConfig(config: WebSocketConfig) {
        this.config = config
    }

    override suspend fun connect(url: String, accessToken: String) {
        connectionMutex.withLock {
            closedManually.set(false)
            reconnectJob?.cancel()
            reconnectJob = null
            currentUrl = url
            currentAccessToken = accessToken
            connectInternal(url, accessToken)
        }
    }

    override suspend fun disconnect() {
        connectionMutex.withLock {
            closedManually.set(true)
            reconnectJob?.cancel()
            reconnectJob = null
            stopPingLoop()
            val exception = WebSocketCoreException.ConnectionError("Disconnected by client")
            failAllPending(exception)
            webSocket?.close(NORMAL_CLOSURE_CODE, "Client disconnect")
            webSocket = null
            _connectionState.value = WebSocketConnectionState.Disconnected
        }
    }

    override suspend fun sendCommand(request: WsRequest): WsResultMessage {
        ensureAuthenticated()
        val id = nextMessageId()
        val requestWithId = request.copy(id = id)
        val deferred = CompletableDeferred<WsMessage>()
        pendingRequests[id] = deferred

        val sent = sendRaw(requestWithId.toJsonString(json))
        if (!sent) {
            pendingRequests.remove(id)
            throw WebSocketCoreException.SendError("Failed to send command: ${request.type}")
        }

        return try {
            val result = withTimeout(config.commandTimeoutMs) { deferred.await() }
            result as? WsResultMessage
                ?: throw WebSocketCoreException.ProtocolError("Expected WsResultMessage, got ${result::class.simpleName}")
        } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
            pendingRequests.remove(id)
            throw WebSocketCoreException.TimeoutError("Command '${request.type}' timed out after ${config.commandTimeoutMs}ms")
        }
    }

    override suspend fun <T> sendCommandForResult(
        request: WsRequest,
        deserializer: DeserializationStrategy<T>,
    ): T {
        val result = sendCommand(request)

        if (!result.success) {
            val errorMsg = result.error?.let { "${it.code}: ${it.message}" } ?: "Unknown error"
            throw WebSocketCoreException.ProtocolError("Command failed: $errorMsg")
        }

        val resultJson = result.result
            ?: throw WebSocketCoreException.ProtocolError("Command succeeded but result is null")

        return try {
            json.decodeFromJsonElement(deserializer, resultJson)
        } catch (e: Exception) {
            throw WebSocketCoreException.SerializationError("Failed to deserialize result: ${e.message}", e)
        }
    }

    override fun subscribe(subscriptionRequest: WsRequest): Flow<WsEventMessage> = callbackFlow {
        val subscriptionResult = sendCommand(subscriptionRequest)
        val subscriptionId = subscriptionResult.id

        if (!subscriptionResult.success) {
            val errorMsg = subscriptionResult.error?.let { "${it.code}: ${it.message}" } ?: "Unknown error"
            close(WebSocketCoreException.ProtocolError("Subscription failed: $errorMsg"))
            return@callbackFlow
        }

        val collectJob = scope.launch {
            _incomingMessages
                .filterIsInstance<WsEventMessage>()
                .collect { event ->
                    if (event.id == subscriptionId) {
                        trySend(event)
                    }
                }
        }

        awaitClose {
            collectJob.cancel()
            // TODO: Здесь можно отправить unsubscribe-команду, если протокол это требует.
            // Например: scope.launch { sendCommand(WsRequest(type = "unsubscribe_events", id = subscriptionId)) }
        }
    }

    override suspend fun sendPing(): WsPongMessage {
        ensureAuthenticated()
        val id = nextMessageId()
        val deferred = CompletableDeferred<WsMessage>()
        pendingRequests[id] = deferred

        val pingJson = buildJsonObject {
            put("type", "ping")
            put("id", id)
        }
        val sent = sendRaw(json.encodeToString(JsonObject.serializer(), pingJson))
        if (!sent) {
            pendingRequests.remove(id)
            throw WebSocketCoreException.SendError("Failed to send ping")
        }

        return try {
            val result = withTimeout(config.commandTimeoutMs) { deferred.await() }
            result as? WsPongMessage
                ?: throw WebSocketCoreException.ProtocolError("Expected WsPongMessage, got ${result::class.simpleName}")
        } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
            pendingRequests.remove(id)
            throw WebSocketCoreException.TimeoutError("Ping timed out")
        }
    }

    override fun close() {
        closedManually.set(true)
        reconnectJob?.cancel()
        pingJob?.cancel()
        val exception = WebSocketCoreException.ConnectionError("WebSocketCore closed")
        failAllPending(exception)
        webSocket?.close(NORMAL_CLOSURE_CODE, "Core closed")
        webSocket = null
        _connectionState.value = WebSocketConnectionState.Disconnected
        scope.cancel()
        scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    }

    fun nextMessageId(): Long = messageIdCounter.getAndIncrement()

    // region Private

    private suspend fun connectInternal(url: String, accessToken: String, isAuthRetry: Boolean = true) {
        _connectionState.value = WebSocketConnectionState.Connecting

        val request = Request.Builder().url(url).build()
        webSocket = okHttpClient.newWebSocket(request, listener)

        try {
            // Ждём auth_required
            withTimeout(config.connectTimeoutMs) {
                _incomingMessages.first { it is WsAuthRequiredMessage }
            }
        } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
            webSocket?.close(NORMAL_CLOSURE_CODE, "Connect timeout")
            webSocket = null
            val exception = WebSocketCoreException.TimeoutError("Timed out waiting for auth_required")
            _connectionState.value = WebSocketConnectionState.Failed(exception)
            throw exception
        }

        performAuth(accessToken, isRetry = isAuthRetry)
        startPingLoop()
    }

    private suspend fun performAuth(accessToken: String, isRetry: Boolean = false) {
        _connectionState.value = WebSocketConnectionState.Authenticating

        val authMessage = buildJsonObject {
            put("type", "auth")
            put("access_token", accessToken)
        }
        val sent = sendRaw(json.encodeToString(JsonObject.serializer(), authMessage))
        if (!sent) {
            val exception = WebSocketCoreException.SendError("Failed to send auth message")
            _connectionState.value = WebSocketConnectionState.Failed(exception)
            throw exception
        }

        try {
            val response = withTimeout(config.authTimeoutMs) {
                _incomingMessages.first { it is WsAuthOkMessage || it is WsAuthInvalidMessage }
            }

            when (response) {
                is WsAuthOkMessage -> {
                    logger.info(TAG, "Authentication successful")
                    _connectionState.value = WebSocketConnectionState.ConnectedAuthenticated
                }
                is WsAuthInvalidMessage -> {
                    if (!isRetry) {
                        logger.info(TAG, "Auth invalid, attempting token refresh")
                        val newToken = authRefresher.refresh()
                        if (newToken != null) {
                            currentAccessToken = newToken
                            val url = currentUrl
                            if (url == null) {
                                val exception = WebSocketCoreException.ConnectionError("Cannot reconnect: currentUrl is null")
                                _connectionState.value = WebSocketConnectionState.Failed(exception)
                                throw exception
                            }
                            // Полный reconnect с новым токеном: закрываем старый сокет,
                            // открываем новый, ждём auth_required и шлём auth.
                            logger.info(TAG, "Token refreshed, reconnecting with new token")
                            val oldWs = webSocket
                            webSocket = null
                            oldWs?.close(NORMAL_CLOSURE_CODE, "Reconnect after token refresh")
                            connectInternal(url, newToken)
                            return
                        }
                        logger.warning(TAG, "Token refresh failed, session expired")
                    }
                    val exception = WebSocketCoreException.AuthError("Auth invalid: ${response.message}")
                    _connectionState.value = WebSocketConnectionState.Failed(exception)
                    webSocket?.close(NORMAL_CLOSURE_CODE, "Auth invalid")
                    webSocket = null
                    throw exception
                }
                else -> {
                    val exception = WebSocketCoreException.ProtocolError("Unexpected auth response: ${response::class.simpleName}")
                    _connectionState.value = WebSocketConnectionState.Failed(exception)
                    throw exception
                }
            }
        } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
            val exception = WebSocketCoreException.TimeoutError("Auth response timed out")
            _connectionState.value = WebSocketConnectionState.Failed(exception)
            webSocket?.close(NORMAL_CLOSURE_CODE, "Auth timeout")
            webSocket = null
            throw exception
        }
    }

    private fun routeMessage(message: WsMessage) {
        when (message) {
            is WsResultMessage -> {
                val deferred = pendingRequests.remove(message.id)
                if (deferred != null) {
                    deferred.complete(message)
                } else {
                    logger.warning(TAG, "No pending request for result id=${message.id}")
                }
            }
            is WsPongMessage -> {
                val deferred = pendingRequests.remove(message.id)
                if (deferred != null) {
                    deferred.complete(message)
                } else {
                    logger.warning(TAG, "No pending request for pong id=${message.id}")
                }
            }
            is WsEventMessage -> {
                if (!_incomingMessages.tryEmit(message)) {
                    logger.warning(TAG, "Incoming messages buffer overflow, event dropped")
                }
            }
            is WsAuthRequiredMessage,
            is WsAuthOkMessage,
            is WsAuthInvalidMessage -> {
                if (!_incomingMessages.tryEmit(message)) {
                    logger.warning(TAG, "Incoming messages buffer overflow, auth message dropped")
                }
            }
            is WsUnknownMessage -> {
                logger.warning(TAG, "Unknown message: ${message.rawText.take(200)}")
            }
        }
    }

    private fun handleDisconnect(code: Int) {
        stopPingLoop()
        val exception = WebSocketCoreException.ConnectionError("WebSocket closed with code $code")
        failAllPending(exception)
        webSocket = null

        if (!closedManually.get() && code != NORMAL_CLOSURE_CODE) {
            _connectionState.value = WebSocketConnectionState.Failed(exception)
            attemptReconnect()
        } else {
            _connectionState.value = WebSocketConnectionState.Disconnected
        }
    }

    private fun attemptReconnect() {
        if (closedManually.get()) return
        if (reconnectJob?.isActive == true) return

        val url = currentUrl ?: return
        val token = currentAccessToken ?: return

        reconnectJob = scope.launch {
            var attempt = 0
            var delayMs = config.reconnect.initialDelayMs

            while (attempt < config.reconnect.maxAttempts && isActive) {
                attempt++
                _connectionState.value = WebSocketConnectionState.Reconnecting(attempt)
                logger.info(TAG, "Reconnect attempt $attempt/${config.reconnect.maxAttempts}, delay=${delayMs}ms")

                delay(delayMs)

                try {
                    connectionMutex.withLock {
                        connectInternal(url, token)
                    }
                    logger.info(TAG, "Reconnected successfully on attempt $attempt")
                    return@launch
                } catch (e: WebSocketCoreException.AuthError) {
                    // Auth ошибка — не ретраим
                    logger.error(TAG, "Reconnect failed with auth error, stopping", e)
                    _connectionState.value = WebSocketConnectionState.Failed(e)
                    return@launch
                } catch (e: Exception) {
                    logger.warning(TAG, "Reconnect attempt $attempt failed: ${e.message}")
                }

                delayMs = (delayMs * config.reconnect.backoffMultiplier)
                    .toLong()
                    .coerceAtMost(config.reconnect.maxDelayMs)
            }

            if (isActive) {
                val exception = WebSocketCoreException.ConnectionError(
                    "Reconnect failed after ${config.reconnect.maxAttempts} attempts"
                )
                _connectionState.value = WebSocketConnectionState.Failed(exception)
            }
        }
    }

    private fun startPingLoop() {
        stopPingLoop()
        pingJob = scope.launch {
            while (isActive) {
                delay(config.pingIntervalMs)
                try {
                    sendPing()
                } catch (e: Exception) {
                    logger.warning(TAG, "Ping failed: ${e.message}")
                }
            }
        }
    }

    private fun stopPingLoop() {
        pingJob?.cancel()
        pingJob = null
    }

    private fun failAllPending(exception: WebSocketCoreException) {
        val entries = pendingRequests.entries.toList()
        pendingRequests.clear()
        entries.forEach { (_, deferred) ->
            deferred.completeExceptionally(exception)
        }
    }

    private fun sendRaw(text: String): Boolean {
        val ws = webSocket ?: return false
        logger.debug(TAG, "Sending: $text")
        return ws.send(text)
    }

    private fun ensureAuthenticated() {
        val state = _connectionState.value
        if (state !is WebSocketConnectionState.ConnectedAuthenticated) {
            throw WebSocketCoreException.ConnectionError("Not authenticated. Current state: $state")
        }
    }

    // endregion
    private companion object {
        const val TAG = "WebSocketCore"
        const val NORMAL_CLOSURE_CODE = 1000
    }
}
