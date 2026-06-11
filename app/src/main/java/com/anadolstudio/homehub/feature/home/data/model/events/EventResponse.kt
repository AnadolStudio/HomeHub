package com.anadolstudio.homehub.feature.home.data.model.events

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class EventResponse(
        @SerialName("event_type") val type: String,
        @SerialName("data") val data: JsonObject? = null,
)
