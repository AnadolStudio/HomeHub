package com.anadolstudio.template.feature.deviceDetail.presentation

import com.anadolstudio.template.feature.home.domain.model.entity.HomeAssistantEntity
import com.anadolstudio.template.feature.home.domain.model.services.HomeAssistantService
import com.anadolstudio.template.feature.home.domain.model.states.HomeAssistantAttribute

internal interface DeviceDetailController {

    fun onSheetExpanded()

    fun onRetryClicked()

    fun onHistoryRetryClicked()

    fun onEntityChanged(entity: HomeAssistantEntity<HomeAssistantAttribute>, service: HomeAssistantService<*>)

    fun onShowError(message: String)

    fun onLightEntityClicked(entity: HomeAssistantEntity<HomeAssistantAttribute>)
}
