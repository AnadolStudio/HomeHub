package com.anadolstudio.template.core.websocket.bus

interface WebSocketEventable {

    fun onNewEvent(event: WebSocketEvent)
}
