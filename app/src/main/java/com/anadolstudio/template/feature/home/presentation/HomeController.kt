package com.anadolstudio.template.feature.home.presentation

import com.anadolstudio.template.feature.home.domain.model.HomeAssistantDevice
import com.anadolstudio.template.feature.home.domain.model.entity.HomeAssistantEntity
import com.anadolstudio.template.feature.home.domain.model.services.HomeAssistantService
import com.anadolstudio.template.feature.home.domain.model.states.HomeAssistantAttribute

interface HomeController {

    fun onEntityClicked(entity: HomeAssistantEntity<HomeAssistantAttribute>, service: HomeAssistantService)
    fun onDeviceClicked(device: HomeAssistantDevice)
    fun onAutomationClicked()
    fun onAddDeviceClicked()
    fun onHistoryClicked()
    fun onAreaClicked()
}
