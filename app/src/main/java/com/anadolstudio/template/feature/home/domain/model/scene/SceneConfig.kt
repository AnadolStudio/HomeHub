package com.anadolstudio.template.feature.home.domain.model.scene

import com.anadolstudio.template.feature.home.domain.model.states.HomeAssistantState
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SceneConfig(
        @SerialName("id") val id: String,
        @SerialName("name") val name: String,
        @SerialName("entities") val entityStates: List<HomeAssistantState<*>>,
        @SerialName("icon") val icon: String? = null,
)
