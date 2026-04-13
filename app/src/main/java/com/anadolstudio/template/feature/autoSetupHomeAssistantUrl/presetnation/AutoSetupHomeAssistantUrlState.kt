package com.anadolstudio.template.feature.autoSetupHomeAssistantUrl.presetnation

import com.anadolstudio.template.feature.autoSetupHomeAssistantUrl.domain.model.HomeAssistantInstance
import com.anadolstudio.utils.states.ProgressState

data class AutoSetupHomeAssistantUrlState(
        val hasWifiConnect: Boolean,
        val progressState: ProgressState = if (hasWifiConnect) ProgressState.Loading else ProgressState.Error(),
        val instanceSet: Set<HomeAssistantInstance> = emptySet(),
)
