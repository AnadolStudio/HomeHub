package com.anadolstudio.template.feature.home.domain.model.states

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement

@Serializable
data class SceneAttributes(
        @SerialName("id") val id: String,
        @SerialName("friendly_name") override val friendlyName: String = "",
        @SerialName("entity_id") val includeEntityIdList: List<String> = emptyList(),
        @Transient override val jsonAttributes: JsonObject = JsonObject(emptyMap()),
) : HomeAssistantAttribute

fun JsonObject.toScene(json: Json): SceneAttributes = json
        .decodeFromJsonElement<SceneAttributes>(this)
        .copy(jsonAttributes = this)
