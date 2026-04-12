package com.anadolstudio.template.navigation

internal interface FlowEvent

internal interface FlowEventHandler {
    fun accept(event: FlowEvent)
}
