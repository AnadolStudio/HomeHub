package com.anadolstudio.homehub.navigation

internal interface FlowEvent

internal interface FlowEventHandler {
    fun accept(event: FlowEvent)
}
