package com.anadolstudio.template.feature.home.domain.model.events

import com.anadolstudio.template.feature.home.domain.model.domain.DomainParser
import com.anadolstudio.template.feature.home.domain.model.states.AllowedState

data class HomeAssistantStateChangedEvent(
        override val entityId: String,
        val allowedState: AllowedState,
) : DomainParser
