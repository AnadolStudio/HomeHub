package com.anadolstudio.template.feature.home.data.model.events

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Элемент ответа GET /api/events */
@Serializable
data class EventListenerResponse(
        @SerialName("event") val eventName: String,
        @SerialName("listener_count") val listenerCount: Int,
)
