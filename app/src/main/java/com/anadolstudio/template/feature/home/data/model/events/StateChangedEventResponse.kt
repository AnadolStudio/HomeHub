package com.anadolstudio.template.feature.home.data.model.events

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StateChangedEventResponse(
        @SerialName("entity_id") val entityId: String,
        @SerialName("new_state") val newState: StateChangedEventData,
        @SerialName("old_state") val oldState: StateChangedEventData? = null,
)

@Serializable
data class StateChangedEventData(
        @SerialName("state") val state: String,
)
