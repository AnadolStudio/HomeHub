package com.anadolstudio.template.feature.sceneCreate.presentation

import com.anadolstudio.template.feature.home.domain.model.entity.HomeAssistantEntity
import com.anadolstudio.template.feature.home.domain.model.states.HomeAssistantAttribute

internal interface SceneCreateController {

    fun onNameChanged(value: String)

    fun onSceneIdChanged(value: String)

    fun onAddDeviceClicked()

    fun onDeviceEditClicked(deviceId: String)

    fun onDeviceRemoved(deviceId: String)

    fun onPreviewToggled()

    fun onSaveClicked()

    fun onCloseClicked()

    fun onRunCreatedSceneClicked()

    /**
     * Колбэк из [com.anadolstudio.template.feature.deviceDetail.presentation.DeviceDetailResult]:
     * после закрытия DeviceDetail SceneCreate "переваривает" текущее состояние сущностей
     * и кладёт в draft (см. [com.anadolstudio.template.feature.sceneCreate.domain.toSceneEntityState]).
     *
     * Внутри ре-фетчит устройство с `useCache=false`, потому что local state DeviceDetail
     * может быть слегка устаревшим в момент onDispose (ws event с финальным состоянием ещё
     * не успел дойти, если юзер закрыл шит сразу после правки).
     */
    fun onDeviceConfigured(entities: List<HomeAssistantEntity<HomeAssistantAttribute>>)

    /**
     * Колбэк из picker'а: снимок текущего состояния устройства ДО открытия DeviceDetail.
     * VM хранит, на [onDeviceConfigured] сравнивает и берёт в draft только различающиеся сущности.
     */
    fun onSnapshotCaptured(entities: List<HomeAssistantEntity<HomeAssistantAttribute>>)

    /**
     * Открытие в режиме редактирования: VM тянет существующий конфиг сцены через GET endpoint
     * и заполняет state. После этого экран ведёт себя как редактор — Save POST'ит на тот же id.
     */
    fun onEditModeRequested(sceneConfigId: String)
}
