package com.anadolstudio.homehub.feature.add_device.common

import androidx.lifecycle.viewModelScope
import com.anadolstudio.homehub.base.viewmodel.StatefulViewModel
import com.anadolstudio.homehub.core.websocket.connection.WebSocketConnectionState
import com.anadolstudio.homehub.event.showMessage
import com.anadolstudio.homehub.feature.home.domain.HAWebsocketRepository
import com.anadolstudio.homehub.feature.home.domain.model.HomeAssistantDevice
import com.anadolstudio.homehub.feature.home.domain.model.entity.HomeAssistantEntity
import com.anadolstudio.homehub.feature.home.domain.model.events.HomeAssistantStateChangedEvent
import com.anadolstudio.homehub.feature.home.domain.model.services.HomeAssistantService
import com.anadolstudio.homehub.feature.home.domain.model.states.HomeAssistantAttribute
import com.anadolstudio.homehub.feature.main.MainGraph.navigateToDeviceDetail
import com.anadolstudio.homehub.util.mapIfContains
import com.anadolstudio.utils.states.lce.lceFlow
import com.anadolstudio.utils.states.lce.mapToLce
import com.anadolstudio.utils.states.lce.onEachContent
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn

internal abstract class BaseAddDeviceViewModel<S : ExtraAddDeviceState>(
        extraState: S,
        protected val websocketRepository: HAWebsocketRepository,
) : StatefulViewModel<BaseAddDeviceState<S>>(
        BaseAddDeviceState(extraState = extraState)
), BaseAddDeviceController {

    protected val extraState: S get() = state.extraState

    init {
        observeConnectionState()
    }

    private fun observeConnectionState() {
        websocketRepository.webSocketConnectionState
                .filterIsInstance<WebSocketConnectionState.ConnectedAuthenticated>()
                .mapToLce()
                .onEachContent { onWebSocketConnected() }
                .launchIn(viewModelScope)
    }

    protected open fun onWebSocketConnected() {
        subscribeToNewDevices()
        subscribeToStateChangedEvents()
    }

    private fun subscribeToNewDevices() {
        websocketRepository.subscribeToRegistryNewDeviceEvents()
                .filter { it.action == DEVICE_REGISTRY_ACTION_CREATE }
                .mapToLce()
                .onEachContent { event -> loadNewDevice(event.deviceId) }
                .launchIn(viewModelScope)
    }

    private fun subscribeToStateChangedEvents() {
        websocketRepository.subscribeToStateChangedEvents()
                .filterIsInstance(HomeAssistantStateChangedEvent.Update::class)
                .mapToLce()
                .onEachContent(this::onStateChanged)
                .launchIn(viewModelScope)
    }

    private fun loadNewDevice(id: String) {
        lceFlow { websocketRepository.getDevice(deviceId = id, useCache = false) }
                .onEachContent { newDevice ->
                    if (newDevice != null) onNewDeviceFound(newDevice)
                }
                .launchIn(viewModelScope)
    }

    protected open fun onStateChanged(event: HomeAssistantStateChangedEvent.Update) {
        val entityId = event.entityId
        val newState = event.newState

        val changedDevice = state.entityToDeviceMap[entityId] ?: return
        val newEntityList = changedDevice.entityMap.mapValues { (_, entityList) ->
            entityList.mapIfContains(
                    condition = { it.entityId == entityId },
                    provideNewElement = { entity -> entity.copy(state = newState) }
            )
        }

        val newDevice = changedDevice.copy(entityMap = newEntityList)
        val newDeviceSet = state.newDeviceList.toMutableSet().apply {
            remove(changedDevice)
            add(newDevice)
        }
        updateState { copy(newDeviceList = newDeviceSet) }
    }

    protected open fun onNewDeviceFound(device: HomeAssistantDevice) {
        updateState { copy(newDeviceList = newDeviceList + device) }
    }

    override fun onDeviceClicked(device: HomeAssistantDevice) = navigateToDeviceDetail(device)

    override fun onEntityClicked(
            entity: HomeAssistantEntity<HomeAssistantAttribute>,
            service: HomeAssistantService<*>,
    ) {
        lceFlow {
            websocketRepository.callService(entityId = entity.entityId, domain = entity.domain, service = service)
        }
                .onEachContent { isSuccess ->
                    if (!isSuccess) showMessage("не удалось выполнить ${entity.entityId}/${entity.domain}")
                }
                .launchIn(viewModelScope)
    }

    protected fun updateExtraState(transform: S.() -> S) {
        val extraState = transform.invoke(extraState)
        updateState { copy(extraState = extraState) }
    }
    private companion object {
        const val DEVICE_REGISTRY_ACTION_CREATE = "create"
    }
}
