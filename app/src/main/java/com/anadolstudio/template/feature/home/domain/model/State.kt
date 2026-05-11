package com.anadolstudio.template.feature.home.domain.model

import java.time.OffsetDateTime
import kotlinx.serialization.json.JsonObject

data class State(
        val entityId: String,
        val state: String,
        val attributes: JsonObject,
        val lastChanged: OffsetDateTime,
        val lastUpdated: OffsetDateTime?,
)
