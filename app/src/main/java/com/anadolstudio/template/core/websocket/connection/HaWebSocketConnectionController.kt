package com.anadolstudio.template.core.websocket.connection

import com.anadolstudio.template.core.websocket.WebSocketBaseDependencies
import com.anadolstudio.template.core.websocket.WebSocketCoreException
import com.anadolstudio.template.core.websocket.WebSocketCoreImpl.Companion.NORMAL_CLOSURE_CODE
import com.anadolstudio.template.core.websocket.bus.WebSocketEvent
import com.anadolstudio.template.core.websocket.bus.WebSocketEventable
import com.anadolstudio.template.core.websocket.message.WsAuthOkMessage
import com.anadolstudio.template.core.websocket.message.WsAuthRequiredMessage
import com.anadolstudio.template.core.websocket.message.WsMessage
import com.anadolstudio.template.core.websocket.message.WsRequest
import com.anadolstudio.template.core.websocket.message.parser.WebSocketMessageParser.Companion.TYPE_AUTH
import com.anadolstudio.template.feature.common.data.PreferencesStorage
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener

class HaWebSocketConnectionController(
        private val incomingMessagesFlow: SharedFlow<WsMessage>,
        private val connectionState: MutableStateFlow<WebSocketConnectionState>,
        private val preferencesStorage: PreferencesStorage,
        private val dependencies: WebSocketBaseDependencies,
) : WebSocketEventable {

    private companion object {
        const val TAG = "HaWebSocketConnectionController"
    }

    private var currentUrl: String? = null
    private var currentAccessToken: String? = null

    override fun onNewEvent(event: WebSocketEvent) {
        when (event) {
            WebSocketEvent.Connection.Open -> {
                connectionState.value = WebSocketConnectionState.ConnectedUnauthenticated
            }

            is WebSocketEvent.Connection.WithError -> {
                connectionState.value = WebSocketConnectionState.Failed(event.throwable)
            }

            else -> Unit
        }
    }

    suspend fun connect(listener: WebSocketListener): WebSocket {
        val accessToken = preferencesStorage.accessToken
        val baseUrl = preferencesStorage.baseUrl

        if (accessToken == null || baseUrl == null) {
            throw WebSocketCoreException.ConnectionError("BaseUrl and accessToken must be not null")
        }

        val wsUrl = baseUrl
                .replace("https://", "wss://")
                .replace("http://", "ws://")
                .trimEnd('/') + "/api/websocket"

        currentUrl = wsUrl
        currentAccessToken = accessToken

        val request = Request.Builder().url(wsUrl).build()

        val webSocket = OkHttpClient.Builder()
                .connectTimeout(dependencies.config.connectTimeoutMs, TimeUnit.MILLISECONDS)
                .readTimeout(0, TimeUnit.MILLISECONDS)
                .writeTimeout(dependencies.config.commandTimeoutMs, TimeUnit.MILLISECONDS)
                .pingInterval(dependencies.config.pingIntervalMs, TimeUnit.MILLISECONDS)
                .retryOnConnectionFailure(true)
                .build()
                .newWebSocket(request, listener)

        connectionState.value = WebSocketConnectionState.Connecting

        waitingAuthRequiredMessageAndAuth(webSocket, accessToken)

        return webSocket
    }

    private suspend fun waitingAuthRequiredMessageAndAuth(webSocket: WebSocket, accessToken: String) {
        try {
            withTimeout(dependencies.config.connectTimeoutMs) {
                incomingMessagesFlow.first { it is WsAuthRequiredMessage }
                tryAuth(webSocket, accessToken)
            }
        } catch (_: Throwable) {
            webSocket.close(NORMAL_CLOSURE_CODE, "Connect timeout")
            val exception = WebSocketCoreException.TimeoutError("Timed out waiting for auth_required")
            connectionState.value = WebSocketConnectionState.Failed(exception)
            throw exception
        }
    }

    fun disconnect() {
        connectionState.value = WebSocketConnectionState.Disconnected
    }

    private suspend fun tryAuth(webSocket: WebSocket, accessToken: String) {
        connectionState.value = WebSocketConnectionState.Authenticating

        val request = WsRequest(
                type = TYPE_AUTH,
                payload = buildJsonObject { put("access_token", accessToken) }
        )
        val sent = webSocket.send(request.toJsonString(dependencies.json))

        if (!sent) {
            val exception = WebSocketCoreException.SendError("Failed to send auth message")
            connectionState.value = WebSocketConnectionState.Failed(exception)
            throw exception
        }

        try {
            val response = withTimeout(dependencies.config.authTimeoutMs) {
                incomingMessagesFlow.first { it is WsAuthOkMessage }
            }

            if (response is WsAuthOkMessage) {
                dependencies.logger.info(TAG, "Authentication successful")
                connectionState.value = WebSocketConnectionState.ConnectedAuthenticated
            } else {
                val exception =
                        WebSocketCoreException.ProtocolError("Unexpected auth response: ${response::class.simpleName}")
                connectionState.value = WebSocketConnectionState.Failed(exception)
                throw exception
            }
        } catch (_: kotlinx.coroutines.TimeoutCancellationException) {
            val exception = WebSocketCoreException.TimeoutError("Auth response timed out")
            connectionState.value = WebSocketConnectionState.Failed(exception)
            webSocket.close(NORMAL_CLOSURE_CODE, "Auth timeout")

            throw exception
        }
    }
}
