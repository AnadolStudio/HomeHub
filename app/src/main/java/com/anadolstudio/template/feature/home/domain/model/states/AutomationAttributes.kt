package com.anadolstudio.template.feature.home.domain.model.states

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement

@Serializable
data class AutomationAttributes(
        @Transient override val jsonAttributes: JsonObject = JsonObject(emptyMap()),
        @SerialName("friendly_name") override val friendlyName: String,
        @SerialName("mode") val mode: String,
        @SerialName("last_triggered") val lastTriggeredDataTime: String? = null,
        @SerialName("icon") val icon: String? = null,
) : HomeAssistantAttribute

fun JsonObject.toAutomation(json: Json): AutomationAttributes = json
        .decodeFromJsonElement<AutomationAttributes>(this)
        .copy(jsonAttributes = this)

