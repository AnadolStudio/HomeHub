package com.anadolstudio.homehub.core.websocket

import com.anadolstudio.homehub.core.websocket.bus.HaWebSocketEventBus
import com.anadolstudio.homehub.core.websocket.bus.WebSocketEvent
import com.anadolstudio.homehub.core.websocket.connection.HaWebSocketConnectionController
import com.anadolstudio.homehub.core.websocket.connection.WebSocketAuthRefresher
import com.anadolstudio.homehub.core.websocket.connection.WebSocketConnectionState
import com.anadolstudio.homehub.core.websocket.message.HaWebSocketMessageController
import com.anadolstudio.homehub.core.websocket.message.WsAuthInvalidMessage
import com.anadolstudio.homehub.core.websocket.message.WsMessage
import com.anadolstudio.homehub.core.websocket.message.WsRequest
import com.anadolstudio.homehub.core.websocket.message.WsResultMessage
import com.anadolstudio.homehub.core.websocket.message.parser.WebSocketMessageParser
import com.anadolstudio.homehub.feature.common.data.PreferencesStorage
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.json.Json
import okhttp3.WebSocket

class WebSocketCoreImpl @Inject constructor(
        private val json: Json,
        private val logger: WebSocketLogger,
        private val authRefresher: WebSocketAuthRefresher,
        private val preferencesStorage: PreferencesStorage,
        parser: WebSocketMessageParser,
) : WebSocketCore {

    private var scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val connectionMutex = Mutex()
    private var reconnectJob: Job? = null
    private val isPause = AtomicBoolean(false)

    private val incomingMessagesFlow = MutableSharedFlow<WsMessage>(replay = 0, extraBufferCapacity = 256)
    private val _connectionState = MutableStateFlow<WebSocketConnectionState>(WebSocketConnectionState.Disconnected)
    override val connectionState: StateFlow<WebSocketConnectionState> = _connectionState.asStateFlow()

    private var webSocket: WebSocket? = null
    private var config: WebSocketConfig = WebSocketConfig()

    private val dependencies = WebSocketBaseDependencies(
            json = json,
            config = config,
            logger = logger,
    )
    private val connectionController = HaWebSocketConnectionController(
            incomingMessagesFlow = incomingMessagesFlow,
            connectionState = _connectionState,
            preferencesStorage = preferencesStorage,
            dependencies = dependencies
    )

    private val messageController = HaWebSocketMessageController(
            incomingMessagesFlow = incomingMessagesFlow,
            connectionState = connectionState,
            dependencies = dependencies
    )

    private val eventBus: HaWebSocketEventBus = HaWebSocketEventBus(
            parser = parser,
            logger = logger,
            onStaleCondition = { webSocket -> isStale(webSocket) },
            onNewEvent = this::onNewEvent,
    )

    private fun onNewEvent(event: WebSocketEvent) {
        when {
            event is WebSocketEvent.Connection.WithError && !isPause.get() -> reconnect()
            event is WebSocketEvent.Data.Message && event.message is WsAuthInvalidMessage -> updateTokenAndReconnect()
            else -> Unit
        }

        connectionController.onNewEvent(event)
        messageController.onNewEvent(event)
    }

    private fun updateTokenAndReconnect() {
        releaseScope()
        scope.launch {
            val newToken = authRefresher.refresh()
            if (newToken == null) {
                disconnect()
                return@launch // Должно разлогинить
            }
            reconnect()
        }
    }

    private fun releaseScope() {
        scope.cancel()
        messageController.resetSubscriptions()
        scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    }

    /**
     * Возвращает true, если колбэк пришёл от отвергнутого (старого) сокета.
     * Нужно после close() + пересоздания WS при full reconnect'е (например,
     * после refresh токена), чтобы stale-колбэки не ломали новое соединение.
     */
    private fun isStale(ws: WebSocket): Boolean = ws !== webSocket && webSocket != null

    override suspend fun connect() {
        webSocket = connectionMutex.withLock {
            tryConnect()
        }

        if (webSocket == null) {
            reconnect()
        }
    }

    private suspend fun tryConnect(): WebSocket? = runCatching { connectionController.connect(eventBus) }
            .onSuccess { messageController.startPingLoop(it, scope) }
            .onFailure { closeWebSocket("Connect Failed") }
            .getOrNull()

    override suspend fun disconnect() = connectionMutex.withLock {
        close(closeMessage = "Disconnected by client")
    }

    override fun onStopWebsocket() {
        if (!isPause.compareAndSet(false, true)) return
        close("App backgrounded")
    }

    override fun onStartWebsocket() {
        isPause.set(false)
        if (webSocket != null || reconnectJob?.isActive == true) return

        scope.launch {
            connect()
        }
    }

    private fun close(closeMessage: String) {
        reconnectJob?.cancel()
        reconnectJob = null
        releaseScope()
        closeWebSocket(closeMessage)
    }

    private fun closeWebSocket(closeMessage: String) {
        webSocket?.close(NORMAL_CLOSURE_CODE, closeMessage)
        webSocket = null

        val exception = WebSocketCoreException.ConnectionError(closeMessage)
        messageController.failAllPending(exception)
        connectionController.disconnect()
    }

    override suspend fun sendCommand(request: WsRequest): WsResultMessage {
        return messageController.sendCommand(ensureWebSocketConnected(), request)
    }

    override suspend fun <T> sendCommandForResult(
            request: WsRequest,
            deserializer: DeserializationStrategy<T>,
    ): T = messageController.sendCommandForResult(
            webSocket = ensureWebSocketConnected(),
            request = request,
            deserializer = deserializer
    )

    override fun <T> subscribe(
            request: WsRequest,
            deserializer: DeserializationStrategy<T>,
    ): Flow<T> = flow {
        emitAll(
                messageController.subscribe(
                        webSocket = ensureWebSocketConnected(),
                        scope = scope,
                        subscriptionRequest = request,
                        deserializer = deserializer,
                ),
        )
    }

    private suspend fun ensureWebSocketConnected(): WebSocket {
        webSocket?.let { return it }

        val totalTimeout = with(dependencies.config) { connectTimeoutMs + authTimeoutMs }

        withTimeout(totalTimeout) {
            connect()
            // Дожидаемся аутентифицированного состояния или терминальной ошибки.
            val terminal = connectionState.first { state ->
                state is WebSocketConnectionState.ConnectedAuthenticated ||
                        state is WebSocketConnectionState.Failed
            }
            if (terminal is WebSocketConnectionState.Failed) throw terminal.reason
        }

        return webSocket
                ?: throw WebSocketCoreException.ConnectionError("WebSocket не инициализирован после connect()")
    }

    private fun reconnect() {
        if (reconnectJob?.isActive == true || isPause.get()) return

        reconnectJob = scope.launch {
            var delayMs = dependencies.config.reconnect.initialDelayMs

            while (isActive) {
                _connectionState.value = WebSocketConnectionState.Reconnecting

                delay(delayMs)

                connectionMutex
                        .withLock { tryConnect() }
                        ?.let {
                            webSocket = it
                            return@launch
                        }

                delayMs = (delayMs * dependencies.config.reconnect.backoffMultiplier)
                        .toLong()
                        .coerceAtMost(dependencies.config.reconnect.maxDelayMs)
            }
        }
    }

    companion object {
        private const val TAG = "WebSocketCore"
        const val NORMAL_CLOSURE_CODE = 1000
    }
}
