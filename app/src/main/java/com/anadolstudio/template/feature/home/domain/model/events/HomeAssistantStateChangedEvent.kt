package com.anadolstudio.template.feature.home.domain.model.events

import com.anadolstudio.template.feature.home.domain.model.domain.DomainParser
import com.anadolstudio.template.feature.home.domain.model.states.HomeAssistantAttribute
import com.anadolstudio.template.feature.home.domain.model.states.HomeAssistantState

sealed interface HomeAssistantStateChangedEvent : DomainParser {

    data class Update(
            override val entityId: String,
            val newState: HomeAssistantState<HomeAssistantAttribute>,
    ) : HomeAssistantStateChangedEvent {

        constructor(
                newState: HomeAssistantState<HomeAssistantAttribute>,
        ) : this(entityId = newState.entityId, newState = newState)
    }

    data class Remove(override val entityId: String) : HomeAssistantStateChangedEvent
}
