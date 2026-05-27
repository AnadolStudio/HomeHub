package com.anadolstudio.template.feature.autoSetupHomeAssistantUrl.presetnation

import com.anadolstudio.template.base.viewmodel.BaseController
import com.anadolstudio.template.feature.autoSetupHomeAssistantUrl.domain.model.HomeAssistantInstance

interface AutoSetupHomeAssistantUrlController : BaseController {

    fun onManualEnterClicked()

    fun onRefreshSwiped()

    fun onRetryClicked()

    fun onInstanceClicked(instance: HomeAssistantInstance)
}
