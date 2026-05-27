package com.anadolstudio.template.feature.sceneCreate.presentation

import com.anadolstudio.template.feature.home.domain.model.HomeAssistantDevice
import com.anadolstudio.template.feature.home.domain.model.states.HomeAssistantState

internal interface SceneCreateController {

    fun onNameChanged(value: String)

    fun onAddClicked()

    fun onDeviceEditClicked(deviceDraft: DeviceDraftCard)

    fun onDeviceRemoved(deviceDraft: DeviceDraftCard)

    fun onEntityRemoved(deviceId: String, state: HomeAssistantState<*>)

    fun onSaveClicked()

    fun onCloseClicked()

    fun onDeviceConfigured(selectedEntities: Set<String>)

    fun onSnapshotAdded(device: HomeAssistantDevice)

}
