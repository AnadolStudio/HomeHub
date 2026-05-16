package com.anadolstudio.template.feature.home.domain.model.events

import com.anadolstudio.template.feature.home.domain.model.domain.DomainParser
import com.anadolstudio.template.feature.home.domain.model.states.HomeAssistantAttribute
import com.anadolstudio.template.feature.home.domain.model.states.HomeAssistantState

data class HomeAssistantStateChangedEvent(
        val newState: HomeAssistantState<HomeAssistantAttribute>,
        override val entityId: String = newState.entityId,
) : DomainParser
