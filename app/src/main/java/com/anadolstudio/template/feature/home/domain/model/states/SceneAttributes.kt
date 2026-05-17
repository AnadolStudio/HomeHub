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
        // У scene.* сущностей, созданных через `scene.create` (runtime-сцены) либо у некоторых
        // state_changed event'ов поле id отсутствует — допускаем null, чтобы не падать на десериализации.
        // Для постоянных scene.* (созданных через /api/config/scene/config/{id}) id всегда есть.
        @SerialName("id") val id: String? = null,
        @SerialName("icon") override val icon: HaIcon? = null,
        @SerialName("friendly_name") override val friendlyName: String = "",
        @SerialName("entity_id") val includeEntityIdList: List<String> = emptyList(),
        @Transient override val jsonAttributes: JsonObject = JsonObject(emptyMap()),
) : HomeAssistantAttribute, Iconable

fun JsonObject.toScene(json: Json): SceneAttributes = json
        .decodeFromJsonElement<SceneAttributes>(this)
        .copy(jsonAttributes = this)
