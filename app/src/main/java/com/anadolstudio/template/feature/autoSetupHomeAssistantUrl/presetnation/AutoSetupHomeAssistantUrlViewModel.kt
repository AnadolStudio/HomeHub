package com.anadolstudio.template.feature.autoSetupHomeAssistantUrl.presetnation

import android.content.res.Resources
import com.anadolstudio.template.base.viewmodel.StatefulViewModel
import com.anadolstudio.template.event.navigateUp
import com.anadolstudio.utils.states.ProgressState
import javax.inject.Inject
import timber.log.Timber

internal class AutoSetupHomeAssistantUrlViewModel @Inject constructor(
        private val resources: Resources,
) : StatefulViewModel<AutoSetupHomeAssistantUrlState>(
        AutoSetupHomeAssistantUrlState()
), AutoSetupHomeAssistantUrlController {

    override fun onBackClicked() {
        Timber.tag("DEBUG_TAG").d("onBackClicked:")
        navigateUp()
    }

    override fun onManualEnterClicked() {
        val progressState = if (state.progressState.isLoading) ProgressState.Content else ProgressState.Loading
        updateState { copy(progressState = progressState) }
    }
}
