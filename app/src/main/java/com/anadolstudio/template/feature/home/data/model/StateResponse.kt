package com.anadolstudio.template.feature.home.data.model

import com.anadolstudio.template.feature.home.domain.model.states.AllowedState.Companion.getAllowedStateByName
import com.anadolstudio.template.feature.home.domain.model.states.HomeAssistantState
import com.anadolstudio.template.feature.home.domain.model.states.SimpleState
import java.time.OffsetDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/** GET /api/states, GET /api/states/{entity_id} */
@Serializable
data class StateResponse(
        @SerialName("entity_id") val entityId: String,
        @SerialName("state") val state: String,
        @SerialName("attributes") val attributes: JsonObject,
        @SerialName("last_changed") val lastChanged: String,
        @SerialName("last_updated") val lastUpdated: String? = null,
) {
    fun toDomain(): HomeAssistantState = SimpleState(
            entityId = entityId,
            state = getAllowedStateByName(state),
            jsonAttributes = attributes,
            lastChanged = OffsetDateTime.parse(lastChanged),
            lastUpdated = lastUpdated?.let { OffsetDateTime.parse(it) },
    )
}
