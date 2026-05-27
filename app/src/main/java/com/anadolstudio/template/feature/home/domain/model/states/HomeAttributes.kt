package com.anadolstudio.template.feature.home.domain.model.states

import com.anadolstudio.ha_resources.HaIcon
import com.anadolstudio.template.util.parcel.JsonObjectParceler
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
data class HomeAttributes(
        @SerialName("friendly_name") override val friendlyName: String = "",
        @Transient override val jsonAttributes: JsonObject = JsonObject(emptyMap()),
        @SerialName("latitude") val latitude: Double,
        @SerialName("longitude") val longitude: Double,
        @SerialName("radius") val radius: Int,
        @SerialName("passive") val passive: Boolean,
        @SerialName("editable") val editable: Boolean,
        @SerialName("persons") val personsEntityId: List<String>,
        @SerialName("icon") override val icon: HaIcon? = null
) : HomeAssistantAttribute

fun JsonObject.toHome(json: Json): HomeAttributes = json
        .decodeFromJsonElement<HomeAttributes>(this)
        .copy(jsonAttributes = this)

