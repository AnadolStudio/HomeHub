package com.anadolstudio.template.feature.homeAssistantAuth.data.api.model

import com.anadolstudio.template.feature.homeAssistantAuth.domain.model.Event
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Элемент ответа GET /api/events */
@Serializable
data class EventResponse(
        val event: String,
        @SerialName("listener_count") val listenerCount: Int,
) {
    fun toDomain(): Event = Event(event = event, listenerCount = listenerCount)
}
