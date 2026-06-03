package com.anadolstudio.homehub.feature.home.presentation

import com.anadolstudio.homehub.feature.home.domain.model.AllowedDomain
import com.anadolstudio.homehub.feature.home.domain.model.Area
import com.anadolstudio.homehub.feature.home.domain.model.HomeAssistantDevice
import com.anadolstudio.homehub.feature.home.domain.model.entity.HomeAssistantEntity
import com.anadolstudio.homehub.feature.home.domain.model.services.HomeAssistantService
import com.anadolstudio.homehub.feature.home.domain.model.states.HomeAssistantAttribute

interface HomeController {

    fun onEntityClicked(entity: HomeAssistantEntity<HomeAssistantAttribute>, service: HomeAssistantService<*>)
    fun onDeviceClicked(device: HomeAssistantDevice)
    fun onAutomationClicked()
    fun onAddClicked()
    fun onHistoryClicked()
    fun onAreaClicked()
    fun onAreaSelected(area: Area?)
    fun onDomainSelected(domain: AllowedDomain?)
}
