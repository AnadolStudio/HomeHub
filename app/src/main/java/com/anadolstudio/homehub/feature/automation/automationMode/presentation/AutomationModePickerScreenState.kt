package com.anadolstudio.homehub.feature.automation.automationMode.presentation

import androidx.compose.runtime.Immutable
import com.anadolstudio.homehub.feature.automation.common.presentation.AutomationMode

@Immutable
internal data class AutomationModePickerScreenState(
        val selectedMode: AutomationMode,
        val modes: List<AutomationMode> = AutomationMode.entries,
)
