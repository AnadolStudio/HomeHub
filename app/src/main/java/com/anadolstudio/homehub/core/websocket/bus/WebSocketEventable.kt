package com.anadolstudio.homehub.core.websocket.bus

interface WebSocketEventable {

    fun onNewEvent(event: WebSocketEvent)
}
