package com.anadolstudio.template.feature.home.domain.model.states

import java.time.OffsetDateTime
import kotlinx.serialization.json.JsonObject

sealed interface HomeAssistantState {
    val entityId: String
    val state: AllowedState
    val jsonAttributes: JsonObject
    val lastChanged: OffsetDateTime
    val lastUpdated: OffsetDateTime?
}

