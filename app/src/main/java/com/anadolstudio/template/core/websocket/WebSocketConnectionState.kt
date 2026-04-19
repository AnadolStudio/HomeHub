package com.anadolstudio.template.core.websocket

sealed interface WebSocketConnectionState {
    data object Disconnected : WebSocketConnectionState
    data object Connecting : WebSocketConnectionState
    data object ConnectedUnauthenticated : WebSocketConnectionState
    data object Authenticating : WebSocketConnectionState
    data object ConnectedAuthenticated : WebSocketConnectionState
    data class Reconnecting(val attempt: Int) : WebSocketConnectionState
    data class Failed(val reason: WebSocketCoreException) : WebSocketConnectionState
}
