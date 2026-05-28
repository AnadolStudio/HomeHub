package com.anadolstudio.homehub.feature.home.domain.model.states

import com.anadolstudio.ha_resources.HaIcon
import com.anadolstudio.homehub.util.parcel.JsonObjectParceler
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.TypeParceler
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement

@Parcelize
@TypeParceler<JsonObject, JsonObjectParceler>()
@Serializable
data class AutomationAttributes(
        @Transient override val jsonAttributes: JsonObject = JsonObject(emptyMap()),
        @SerialName("friendly_name") override val friendlyName: String = "",
        @SerialName("mode") val mode: String,
        @SerialName("last_triggered") val lastTriggeredDataTime: String? = null,
        @SerialName("icon") override val icon: HaIcon? = null,
) : HomeAssistantAttribute, Iconable

fun JsonObject.toAutomation(json: Json): AutomationAttributes = json
        .decodeFromJsonElement<AutomationAttributes>(this)
        .copy(jsonAttributes = this)

