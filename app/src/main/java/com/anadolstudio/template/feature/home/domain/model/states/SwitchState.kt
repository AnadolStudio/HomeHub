package com.anadolstudio.template.feature.home.domain.model.states

import java.time.OffsetDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement

data class SwitchState(
        override val entityId: String,
        override val state: AllowedState,
        override val jsonAttributes: JsonObject,
        override val lastChanged: OffsetDateTime,
        override val lastUpdated: OffsetDateTime?,
) : HomeAssistantState {
    val attributes: SwitchAttributes = SwitchAttributesJson.decodeFromJsonElement(jsonAttributes)
}

@Serializable
data class SwitchAttributes(
        @SerialName("friendly_name") val friendlyName: String,
)

private val SwitchAttributesJson = Json { ignoreUnknownKeys = true }
