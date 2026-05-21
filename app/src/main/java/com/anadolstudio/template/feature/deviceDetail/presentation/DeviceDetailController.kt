package com.anadolstudio.template.feature.deviceDetail.presentation

import com.anadolstudio.template.feature.home.domain.model.entity.HomeAssistantEntity
import com.anadolstudio.template.feature.home.domain.model.services.HomeAssistantService
import com.anadolstudio.template.feature.home.domain.model.states.HomeAssistantAttribute
import com.anadolstudio.template.feature.home.domain.model.states.NumberAttribute

internal interface DeviceDetailController {

    fun onSheetExpanded()

    fun onRetryClicked()

    fun onHistoryRetryClicked()

    fun onEntityChanged(entity: HomeAssistantEntity<HomeAssistantAttribute>, service: HomeAssistantService<*>)

    fun onNumericEntityChanged(value: String, entity: HomeAssistantEntity<NumberAttribute>)

    fun onNumericEntityFocusLost(entity: HomeAssistantEntity<NumberAttribute>)
}
