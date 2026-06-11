package com.anadolstudio.homehub.feature.deviceDetail.base

import com.anadolstudio.homehub.feature.home.domain.model.entity.HomeAssistantEntity
import com.anadolstudio.homehub.feature.home.domain.model.services.HomeAssistantService
import com.anadolstudio.homehub.feature.home.domain.model.states.HomeAssistantAttribute
import com.anadolstudio.homehub.feature.home.domain.model.states.NumberAttribute

internal interface BaseDeviceDetailController {

    fun onBackClicked()

    fun onSheetExpanded()

    fun onSheetHidden()

    fun onEntityChanged(entity: HomeAssistantEntity<HomeAssistantAttribute>, service: HomeAssistantService<*>)

    fun onNumericEntityChanged(value: String, entity: HomeAssistantEntity<NumberAttribute>)

    fun onNumericEntityFocusLost(entity: HomeAssistantEntity<NumberAttribute>)
}
