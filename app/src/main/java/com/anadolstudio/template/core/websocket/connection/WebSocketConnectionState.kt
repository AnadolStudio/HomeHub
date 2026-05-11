package com.anadolstudio.template.core.websocket.connection

import com.anadolstudio.template.core.websocket.WebSocketCoreException

sealed interface WebSocketConnectionState {
    data object Disconnected : WebSocketConnectionState
    data object Connecting : WebSocketConnectionState
    data object ConnectedUnauthenticated : WebSocketConnectionState
    data object Authenticating : WebSocketConnectionState
    data object ConnectedAuthenticated : WebSocketConnectionState
    data object Reconnecting : WebSocketConnectionState
    data class Failed(val reason: WebSocketCoreException) : WebSocketConnectionState
}
