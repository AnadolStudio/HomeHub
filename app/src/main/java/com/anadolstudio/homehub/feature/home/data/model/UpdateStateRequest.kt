package com.anadolstudio.homehub.feature.home.data.model

import com.anadolstudio.homehub.feature.home.domain.model.UpdateState
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/** POST /api/states/{entity_id} — тело запроса. */
@Serializable
data class UpdateStateRequest(
        val state: String,
        val attributes: JsonObject? = null,
) {
    companion object {
        fun from(domain: UpdateState): UpdateStateRequest = UpdateStateRequest(
                state = domain.state,
                attributes = domain.attributes,
        )
    }
}
