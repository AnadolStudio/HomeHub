package com.anadolstudio.template.feature.homeAssistantAuth.presetnation

import com.anadolstudio.template.base.viewmodel.StatefulViewModel
import com.anadolstudio.template.event.navigateUp
import javax.inject.Inject

internal class HomeAssistantAuthViewModel @Inject constructor(
) : StatefulViewModel<HomeAssistantAuthState>(
        HomeAssistantAuthState(),
), HomeAssistantAuthController {

    fun setInstance(instance: com.anadolstudio.template.feature.autoSetupHomeAssistantUrl.domain.model.HomeAssistantInstance) {
        if (state.instance != null) return
        updateState { copy(instance = instance) }
    }

    override fun onBackClicked() {
        navigateUp()
    }
}
