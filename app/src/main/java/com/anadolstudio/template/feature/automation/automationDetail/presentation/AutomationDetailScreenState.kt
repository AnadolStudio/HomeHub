package com.anadolstudio.template.feature.automation.automationDetail.presentation

import androidx.compose.runtime.Immutable
import com.anadolstudio.utils.states.ProgressState

@Immutable
internal data class AutomationDetailScreenState(
        val progressState: ProgressState = ProgressState.Content,
)
