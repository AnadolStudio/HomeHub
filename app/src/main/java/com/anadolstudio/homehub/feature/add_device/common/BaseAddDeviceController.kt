package com.anadolstudio.homehub.feature.add_device.common

import com.anadolstudio.homehub.feature.home.domain.model.HomeAssistantDevice
import com.anadolstudio.homehub.feature.home.domain.model.entity.HomeAssistantEntity
import com.anadolstudio.homehub.feature.home.domain.model.services.HomeAssistantService
import com.anadolstudio.homehub.feature.home.domain.model.states.HomeAssistantAttribute

internal interface BaseAddDeviceController {

    fun onDeviceClicked(device: HomeAssistantDevice)

    fun onEntityClicked(
            entity: HomeAssistantEntity<HomeAssistantAttribute>,
            service: HomeAssistantService<*>,
    )
}
