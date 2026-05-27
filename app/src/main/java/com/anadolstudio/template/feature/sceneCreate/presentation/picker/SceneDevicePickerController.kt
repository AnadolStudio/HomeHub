package com.anadolstudio.template.feature.sceneCreate.presentation.picker

import com.anadolstudio.template.feature.home.domain.model.Area
import com.anadolstudio.template.feature.home.domain.model.HomeAssistantDevice

internal interface SceneDevicePickerController {

    fun onSearchQueryChanged(query: String)

    fun onAreaSelected(area: Area?)

    fun onDeviceClicked(device: HomeAssistantDevice)

    fun onRetryClicked()

    fun onCloseClicked()
}
