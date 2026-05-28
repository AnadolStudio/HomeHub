package com.anadolstudio.homehub.feature.manualSetupHomeAssistantUrl.presetnation

import com.anadolstudio.homehub.base.viewmodel.StatefulViewModel
import com.anadolstudio.homehub.event.navigateUp
import javax.inject.Inject

internal class ManualSetupHomeAssistantUrlViewModel @Inject constructor(
) : StatefulViewModel<ManualSetupHomeAssistantUrlState>(
        ManualSetupHomeAssistantUrlState(),
), ManualSetupHomeAssistantUrlController {

    override fun onBackClicked() {
        navigateUp()
    }
}
