package com.anadolstudio.template.feature.addZigbee.presentation

import com.anadolstudio.template.feature.home.domain.model.HomeAssistantDevice
import com.anadolstudio.template.feature.home.domain.model.entity.HomeAssistantEntity
import com.anadolstudio.template.feature.home.domain.model.services.HomeAssistantService
import com.anadolstudio.template.feature.home.domain.model.states.HomeAssistantAttribute

internal interface AddZigbeeController {

    fun onDeviceClicked(device: HomeAssistantDevice)

    fun onEntityClicked(entity: HomeAssistantEntity<HomeAssistantAttribute>, service: HomeAssistantService<*>)
}
