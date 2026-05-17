package com.anadolstudio.template.feature.sceneCreate.presentation.picker

internal interface SceneDevicePickerController {

    fun onSearchQueryChanged(query: String)

    fun onAreaSelected(areaId: String?)

    fun onDeviceClicked(deviceId: String)

    fun onRetryClicked()

    fun onCloseClicked()
}
