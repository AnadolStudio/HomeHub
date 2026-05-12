package com.anadolstudio.template.feature.home.domain.model

import androidx.compose.runtime.Immutable
import com.anadolstudio.template.feature.home.domain.model.states.HomeAssistantState
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class HomeAssistantEntity(
        val id: String,
        val services: Set<String>,
        val stateData: HomeAssistantState
) {
    val domain: String = id.split(".").first()
    val componentType: AllowedComponent? = AllowedComponent.getByName(domain)
}
