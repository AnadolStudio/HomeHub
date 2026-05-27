package com.anadolstudio.template.feature.home.domain.model.events

import kotlinx.serialization.json.JsonObject

data class HomeAssistantEvent(
        val eventType: HomeAssistantEventType,
        val data: JsonObject,
)
