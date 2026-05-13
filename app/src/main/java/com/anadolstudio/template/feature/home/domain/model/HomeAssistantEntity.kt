package com.anadolstudio.template.feature.home.domain.model

import androidx.compose.runtime.Immutable
import com.anadolstudio.template.feature.home.domain.model.states.AllowedState
import com.anadolstudio.template.feature.home.domain.model.states.HomeAssistantState
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class HomeAssistantEntity(
        val entityId: String,
        val services: Set<String>,
        val allowedState: AllowedState,
        val stateData: HomeAssistantState, // TODO неприятно обновлять внутренние данные
) {
    val domain: String = entityId.split(".").first()
    val componentType: AllowedComponent? get() = AllowedComponent.getByName(domain)
}
