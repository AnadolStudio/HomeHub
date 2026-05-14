package com.anadolstudio.template.feature.automationList.presentation

import androidx.compose.runtime.Immutable
import com.anadolstudio.utils.states.ProgressState

@Immutable
internal data class AutomationListScreenState(
        val progressState: ProgressState = ProgressState.Content,
)
