package com.anadolstudio.homehub.feature.automation.automationMode.presentation

import com.anadolstudio.homehub.base.viewmodel.StatefulViewModel
import com.anadolstudio.homehub.event.navigateUp
import com.anadolstudio.homehub.feature.automation.common.presentation.AutomationMode
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject

internal const val AUTOMATION_MODE_RESULT_KEY = "automation_mode_result"

internal class AutomationModePickerViewModel @AssistedInject constructor(
        @Assisted initialMode: AutomationMode,
) : StatefulViewModel<AutomationModePickerScreenState>(AutomationModePickerScreenState(selectedMode = initialMode)),
    AutomationModePickerController {

    override fun onModeSelected(mode: AutomationMode) = updateState { copy(selectedMode = mode) }

    override fun onConfirmClicked() = navigateUp(AUTOMATION_MODE_RESULT_KEY to state.selectedMode)

    override fun onCancelClicked() = navigateUp()

    @AssistedFactory
    interface Factory {
        fun create(initialMode: AutomationMode): AutomationModePickerViewModel
    }
}
