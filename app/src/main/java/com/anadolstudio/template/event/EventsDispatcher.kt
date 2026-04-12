package com.anadolstudio.template.event

internal interface EventsDispatcher {

    val events: EventQueue

    fun showEvent(event: Event) {
        events.offerEvent(event)
    }
}
