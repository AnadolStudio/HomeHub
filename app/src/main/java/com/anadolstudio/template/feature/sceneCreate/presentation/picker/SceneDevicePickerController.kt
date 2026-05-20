package com.anadolstudio.template.feature.sceneCreate.presentation.picker

import com.anadolstudio.template.feature.home.domain.model.Area

internal interface SceneDevicePickerController {

    fun onSearchQueryChanged(query: String)

    fun onAreaSelected(area: Area?)

    fun onDeviceClicked(deviceId: String)

    fun onRetryClicked()

    fun onCloseClicked()
}
