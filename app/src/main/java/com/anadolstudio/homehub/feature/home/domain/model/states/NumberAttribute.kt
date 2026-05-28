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
data class NumberAttribute(
        @Transient override val jsonAttributes: JsonObject = JsonObject(emptyMap()),
        @SerialName("friendly_name") override val friendlyName: String = "",
        @SerialName("min") val min: Double? = null,
        @SerialName("max") val max: Double? = null,
        @SerialName("step") val step: Double? = null,
        @SerialName("mode") val mode: String? = null,
        @SerialName("unit_of_measurement") val unitOfMeasurement: String = "",
        @SerialName("device_class") val deviceClass: String = "",
        @SerialName("icon") override val icon: HaIcon? = null
) : HomeAssistantAttribute

fun JsonObject.toNumber(json: Json): NumberAttribute = json
        .decodeFromJsonElement<NumberAttribute>(this)
        .copy(jsonAttributes = this)
