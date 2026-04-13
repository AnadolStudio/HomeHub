package com.anadolstudio.template.feature.autoSetupHomeAssistantUrl.presetnation

import android.content.res.Resources
import com.anadolstudio.template.base.viewmodel.StatefulViewModel
import com.anadolstudio.template.event.navigateUp
import com.anadolstudio.template.event.showTodo
import timber.log.Timber
import javax.inject.Inject

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
        TODO("Not yet implemented")
    }
}
