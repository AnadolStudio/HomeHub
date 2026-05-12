package com.anadolstudio.template.feature.home.domain.model.events

import com.anadolstudio.template.feature.home.domain.model.states.AllowedState

data class HomeAssistantStateChangedEvent(
        val entityId: String,
        val allowedState: AllowedState,
)
