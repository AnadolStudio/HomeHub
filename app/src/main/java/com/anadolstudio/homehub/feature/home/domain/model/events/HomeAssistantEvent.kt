package com.anadolstudio.homehub.feature.home.domain.model.events

import kotlinx.serialization.json.JsonObject

data class HomeAssistantEvent(
        val eventType: HomeAssistantEventType,
        val data: JsonObject,
)
