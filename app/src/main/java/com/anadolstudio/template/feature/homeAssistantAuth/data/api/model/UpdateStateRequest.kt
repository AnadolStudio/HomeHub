package com.anadolstudio.template.feature.homeAssistantAuth.data.api.model

import com.anadolstudio.template.feature.homeAssistantAuth.domain.model.UpdateState
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
