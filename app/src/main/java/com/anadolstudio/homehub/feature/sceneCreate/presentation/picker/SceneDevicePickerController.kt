package com.anadolstudio.homehub.feature.sceneCreate.presentation.picker

import com.anadolstudio.homehub.feature.home.domain.model.Area
import com.anadolstudio.homehub.feature.home.domain.model.HomeAssistantDevice

internal interface SceneDevicePickerController {

    fun onSearchQueryChanged(query: String)

    fun onAreaSelected(area: Area?)

    fun onDeviceClicked(device: HomeAssistantDevice)

    fun onRetryClicked()

    fun onCloseClicked()
}
