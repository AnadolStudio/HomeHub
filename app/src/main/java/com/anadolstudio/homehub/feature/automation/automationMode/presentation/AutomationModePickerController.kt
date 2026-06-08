package com.anadolstudio.homehub.feature.automation.automationMode.presentation

import com.anadolstudio.homehub.feature.automation.common.presentation.AutomationMode

internal interface AutomationModePickerController {

    fun onModeSelected(mode: AutomationMode)

    fun onConfirmClicked()

    fun onCancelClicked()
}
