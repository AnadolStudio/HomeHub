package com.anadolstudio.template.feature.home.domain.model.states

import com.anadolstudio.ha_resources.HaIcon
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement

@Serializable
data class SwitchAttribute(
        @Transient override val jsonAttributes: JsonObject = JsonObject(emptyMap()),
        @SerialName("friendly_name") override val friendlyName: String = "",
        @SerialName("icon") override val icon: HaIcon? = null
) : HomeAssistantAttribute

fun JsonObject.toSwitch(json: Json): SwitchAttribute = json
        .decodeFromJsonElement<SwitchAttribute>(this)
        .copy(jsonAttributes = this)
