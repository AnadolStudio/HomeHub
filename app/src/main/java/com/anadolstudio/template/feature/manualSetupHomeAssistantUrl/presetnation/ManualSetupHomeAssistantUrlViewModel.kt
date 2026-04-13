package com.anadolstudio.template.feature.manualSetupHomeAssistantUrl.presetnation

import com.anadolstudio.template.base.viewmodel.StatefulViewModel
import com.anadolstudio.template.event.navigateUp
import javax.inject.Inject

internal class ManualSetupHomeAssistantUrlViewModel @Inject constructor(
) : StatefulViewModel<ManualSetupHomeAssistantUrlState>(
        ManualSetupHomeAssistantUrlState(),
), ManualSetupHomeAssistantUrlController {

    override fun onBackClicked() {
        navigateUp()
    }
}
