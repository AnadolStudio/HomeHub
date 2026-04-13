package com.anadolstudio.template.feature.autoSetupHomeAssistantUrl.presetnation

import com.anadolstudio.utils.states.ProgressState

data class AutoSetupHomeAssistantUrlState(
        val progressState: ProgressState = ProgressState.Loading,
) {

}


