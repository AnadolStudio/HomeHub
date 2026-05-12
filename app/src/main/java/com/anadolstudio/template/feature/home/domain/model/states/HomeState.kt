package com.anadolstudio.template.feature.home.domain.model.states

import java.time.OffsetDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement

data class HomeState(
        override val entityId: String,
        override val state: AllowedState,
        override val jsonAttributes: JsonObject,
        override val lastChanged: OffsetDateTime,
        override val lastUpdated: OffsetDateTime?,
) : HomeAssistantState {
    val attributes: HomeAttributes = HomeAttributesJson.decodeFromJsonElement(jsonAttributes)
}

@Serializable
data class HomeAttributes(
        @SerialName("latitude") val latitude: Double,
        @SerialName("longitude") val longitude: Double,
        @SerialName("radius") val radius: Int,
        @SerialName("passive") val passive: Boolean,
        @SerialName("editable") val editable: Boolean,
        @SerialName("friendly_name") val friendlyName: String,
        @SerialName("persons") val personsEntityId: List<String>,
)

private val HomeAttributesJson = Json { ignoreUnknownKeys = true }

fun HomeAssistantState.toHomeState(): HomeState = HomeState(
        entityId = entityId,
        state = state,
        jsonAttributes = jsonAttributes,
        lastChanged = lastChanged,
        lastUpdated = lastUpdated,
)
