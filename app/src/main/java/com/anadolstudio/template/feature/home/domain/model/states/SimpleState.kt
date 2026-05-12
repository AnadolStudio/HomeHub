package com.anadolstudio.template.feature.home.domain.model.states

import java.time.OffsetDateTime
import kotlinx.serialization.json.JsonObject

data class SimpleState(
        override val entityId: String,
        override val state: AllowedState,
        override val jsonAttributes: JsonObject,
        override val lastChanged: OffsetDateTime,
        override val lastUpdated: OffsetDateTime?,
) : HomeAssistantState


