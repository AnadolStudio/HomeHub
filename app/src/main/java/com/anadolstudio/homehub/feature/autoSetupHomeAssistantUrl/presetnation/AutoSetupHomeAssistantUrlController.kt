package com.anadolstudio.homehub.feature.autoSetupHomeAssistantUrl.presetnation

import com.anadolstudio.homehub.base.viewmodel.BaseController
import com.anadolstudio.homehub.feature.autoSetupHomeAssistantUrl.domain.model.HomeAssistantInstance

interface AutoSetupHomeAssistantUrlController : BaseController {

    fun onManualEnterClicked()

    fun onRefreshSwiped()

    fun onRetryClicked()

    fun onInstanceClicked(instance: HomeAssistantInstance)
}
