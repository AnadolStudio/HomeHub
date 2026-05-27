package com.anadolstudio.template.feature.home.data.model.scene

import com.anadolstudio.template.feature.home.data.model.StateResponse
import com.anadolstudio.template.feature.home.domain.model.scene.SceneConfig
import com.anadolstudio.template.feature.home.domain.model.states.HomeAssistantState
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

@Serializable
data class SceneConfigResponse(
        @SerialName("id") val id: String,
        @SerialName("name") val name: String,
        @SerialName("entities") val entities: Map<String, JsonObject>,
        @SerialName("icon") val icon: String? = null,
)

fun SceneConfigResponse.toDomain(json: Json): SceneConfig = SceneConfig(
        id = id,
        name = name,
        entityStates = entities.toStates(json),
        icon = icon,
)

private fun Map<String, JsonObject>.toStates(
        json: Json,
): List<HomeAssistantState<*>> = mapNotNull { (entityId, jsonObject) ->
    val state = (jsonObject["state"] as? JsonPrimitive)?.content.orEmpty()

    StateResponse(entityId = entityId, state = state, attributes = jsonObject).toDomain(json)
}
