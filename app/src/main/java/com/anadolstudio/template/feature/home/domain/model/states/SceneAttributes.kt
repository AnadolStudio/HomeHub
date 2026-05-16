package com.anadolstudio.template.feature.home.domain.model.states

import com.anadolstudio.ha_resources.HaIcon
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement

@Serializable
data class SceneAttributes(
        @SerialName("id") val id: String,
        @SerialName("icon") override val icon: HaIcon? = null,
        @SerialName("friendly_name") override val friendlyName: String = "",
        @SerialName("entity_id") val includeEntityIdList: List<String> = emptyList(),
        @Transient override val jsonAttributes: JsonObject = JsonObject(emptyMap()),
) : HomeAssistantAttribute, Iconable

fun JsonObject.toScene(json: Json): SceneAttributes = json
        .decodeFromJsonElement<SceneAttributes>(this)
        .copy(jsonAttributes = this)
