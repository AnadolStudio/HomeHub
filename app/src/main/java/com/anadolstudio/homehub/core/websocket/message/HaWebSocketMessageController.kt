package com.anadolstudio.homehub.core.websocket.message

import com.anadolstudio.homehub.core.websocket.WebSocketBaseDependencies
import com.anadolstudio.homehub.core.websocket.WebSocketCoreException
import com.anadolstudio.homehub.core.websocket.bus.WebSocketEvent
import com.anadolstudio.homehub.core.websocket.bus.WebSocketEventable
import com.anadolstudio.homehub.core.websocket.connection.WebSocketConnectionState
import com.anadolstudio.homehub.core.websocket.message.parser.WebSocketMessageParser.Companion.TYPE_PING
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import okhttp3.WebSocket

class HaWebSocketMessageController(
        private val incomingMessagesFlow: MutableSharedFlow<WsMessage>,
        private val connectionState: StateFlow<WebSocketConnectionState>,
        private val dependencies: WebSocketBaseDependencies,
) : WebSocketEventable {
    private companion object {
        const val TAG = "HaWebSocketMessageController"
    }

    private val pendingRequests = ConcurrentHashMap<Long, CompletableDeferred<WsMessage>>()
    private var pingJob: Job? = null
    private val messageIdCounter = AtomicLong(1)

    private val subscriptionsFlowMap = mutableMapOf<WsRequest, Flow<*>>()

    override fun onNewEvent(event: WebSocketEvent) {
        when (event) {
            is WebSocketEvent.Connection.WithError -> failAllPending(event.throwable)
            is WebSocketEvent.Data.Message -> processMessage(event)
            else -> Unit
        }
    }

    private fun processMessage(event: WebSocketEvent.Data.Message) {
        when (val message = event.message) {
            is WsResultMessage -> {
                val deferred = pendingRequests.remove(message.id)
                deferred?.complete(message)
                        ?: dependencies.logger.warning(TAG, "No pending request for id=${message.id}")
            }

            is WsUnknownMessage -> {
                dependencies.logger.warning(TAG, "Unknown message: ${message.rawText.take(200)}")
            }

            else -> {
                if (!incomingMessagesFlow.tryEmit(message)) {
                    dependencies.logger.warning(TAG, "Incoming messages buffer overflow, $message dropped")
                }
            }
        }
    }

    suspend fun sendCommand(webSocket: WebSocket, request: WsRequest): WsResultMessage {
        ensureAuthenticated()
        val id = nextMessageId()
        val requestWithId = request.copy(id = id)
        val deferred = CompletableDeferred<WsMessage>()
        pendingRequests[id] = deferred

        val sent = webSocket.send(requestWithId.toJsonString(dependencies.json))
        if (!sent) {
            pendingRequests.remove(id)
            throw WebSocketCoreException.SendError("Failed to send command: ${request.type}")
        }

        return try {
            val result = withTimeout(dependencies.config.commandTimeoutMs) { deferred.await() }
            result as? WsResultMessage
                    ?: throw WebSocketCoreException.ProtocolError("Expected WsResultMessage, got ${result::class.simpleName}")
        } catch (_: kotlinx.coroutines.TimeoutCancellationException) {
            pendingRequests.remove(id)
            throw WebSocketCoreException.TimeoutError("Command '${request.type}' timed out after ${dependencies.config.commandTimeoutMs}ms")
        }
    }

    suspend fun <T> sendCommandForResult(
            webSocket: WebSocket,
            request: WsRequest,
            deserializer: DeserializationStrategy<T>,
    ): T {
        val result = sendCommand(webSocket, request)

        if (!result.success) {
            val errorMsg = result.error?.let { "${it.code}: ${it.message}" } ?: "Unknown error"
            throw WebSocketCoreException.ProtocolError("Command failed: $errorMsg")
        }

        val resultJson = result.result
                ?: throw WebSocketCoreException.ProtocolError("Command succeeded but result is null")

        return try {
            dependencies.json.decodeFromJsonElement(deserializer, resultJson)
        } catch (e: Exception) {
            throw WebSocketCoreException.SerializationError("Failed to deserialize result: ${e.message}", e)
        }
    }

    fun <T> subscribe(
            webSocket: WebSocket,
            scope: CoroutineScope,
            subscriptionRequest: WsRequest,
            deserializer: DeserializationStrategy<T>,
    ): Flow<T> {
        val requestToMap = subscriptionRequest.copy(id = null)
        val existFlow = subscriptionsFlowMap[requestToMap] as? Flow<T>
        if (existFlow != null) {

            return existFlow
        }

        return callbackFlow {
            val subscriptionResult = sendCommand(webSocket, subscriptionRequest)
            val subscriptionId = subscriptionResult.id

            if (!subscriptionResult.success) {
                val errorMsg = subscriptionResult.error?.let { "${it.code}: ${it.message}" } ?: "Unknown error"
                close(WebSocketCoreException.ProtocolError("Subscription failed: $errorMsg"))
                return@callbackFlow
            }

            val collectJob = scope.launch {
                incomingMessagesFlow
                        .filterIsInstance<WsEventMessage>()
                        .collect { event ->
                            if (event.id == subscriptionId) {
                                val result = dependencies.json.decodeFromJsonElement(deserializer, event.eventData)
                                trySend(result)
                            }
                        }
            }

            awaitClose {
                collectJob.cancel()
                scope.launch {
                    runCatching {
                        sendCommand(
                                webSocket = webSocket,
                                request = WsRequest(
                                        command = Command.UNSUBSCRIBE_EVENTS,
                                        payload = buildJsonObject { put("subscription", subscriptionId) },
                                ),
                        )
                    }.onFailure { dependencies.logger.warning(TAG, "Unsubscribe failed: ${it.message}") }
                }
            }
        }
                .shareIn(scope, WhileSubscribed(5_000))
                .also { flow ->
                    subscriptionsFlowMap[requestToMap.copy(id = null)] = flow
                }
    }

    fun startPingLoop(webSocket: WebSocket, scope: CoroutineScope) {
        stopPingLoop()
        pingJob = scope.launch {
            while (isActive) {
                delay(dependencies.config.pingIntervalMs)
                try {
                    sendPing(webSocket)
                } catch (e: Exception) {
                    dependencies.logger.warning(TAG, "Ping failed: ${e.message}")
                }
            }
        }
    }

    fun stopPingLoop() {
        pingJob?.cancel()
        pingJob = null
    }

    fun resetSubscriptions() {
        stopPingLoop()
        subscriptionsFlowMap.clear()
    }

    private suspend fun sendPing(webSocket: WebSocket) = sendCommand(
            webSocket = webSocket,
            request = WsRequest(type = TYPE_PING)
    )

    fun failAllPending(exception: WebSocketCoreException) {
        resetSubscriptions()

        val entries = pendingRequests.entries.toList()
        pendingRequests.clear()
        entries.forEach { (_, deferred) ->
            deferred.completeExceptionally(exception)
        }
    }

    private fun ensureAuthenticated() {
        val state = connectionState.value

        if (state !is WebSocketConnectionState.ConnectedAuthenticated) {
            throw WebSocketCoreException.ConnectionError("Not authenticated. Current state: $state")

            // TODO Не нашел прямого обработчика
        }
    }

    private fun nextMessageId(): Long = messageIdCounter.getAndIncrement()
}
