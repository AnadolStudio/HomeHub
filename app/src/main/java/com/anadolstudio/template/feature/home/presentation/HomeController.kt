package com.anadolstudio.template.feature.home.presentation

import com.anadolstudio.template.feature.home.domain.model.HomeAssistantDevice
import com.anadolstudio.template.feature.home.domain.model.HomeAssistantEntity
import com.anadolstudio.template.feature.home.domain.model.services.HomeAssistantService

interface HomeController {

    fun onEntityClicked(entity: HomeAssistantEntity, service: HomeAssistantService)
    fun onDeviceClicked(device: HomeAssistantDevice)
    fun onSceneClicked()
    fun onAddDeviceClicked()
    fun onHistoryClicked()
}
