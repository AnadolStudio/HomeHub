package com.anadolstudio.homehub.feature.home.data.model.events

import com.anadolstudio.homehub.feature.home.data.model.StateResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StateChangedEventResponse(
        @SerialName("entity_id") val entityId: String,
        @SerialName("new_state") val newState: StateResponse? = null,
        @SerialName("old_state") val oldState: StateResponse? = null,
)
