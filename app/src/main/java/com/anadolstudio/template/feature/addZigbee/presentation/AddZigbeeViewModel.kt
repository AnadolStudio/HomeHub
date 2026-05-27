package com.anadolstudio.template.feature.addZigbee.presentation

import androidx.lifecycle.viewModelScope
import com.anadolstudio.template.base.viewmodel.StatefulViewModel
import com.anadolstudio.template.core.websocket.connection.WebSocketConnectionState
import com.anadolstudio.template.event.showMessage
import com.anadolstudio.template.feature.addZigbee.presentation.error.ZigbeeNotFoundError
import com.anadolstudio.template.feature.home.domain.HAWebsocketRepository
import com.anadolstudio.template.feature.home.domain.model.AllowedDomain
import com.anadolstudio.template.feature.home.domain.model.HomeAssistantDevice
import com.anadolstudio.template.feature.home.domain.model.entity.HomeAssistantEntity
import com.anadolstudio.template.feature.home.domain.model.events.HomeAssistantStateChangedEvent
import com.anadolstudio.template.feature.home.domain.model.services.HomeAssistantService
import com.anadolstudio.template.feature.home.domain.model.services.SimpleToggleableService
import com.anadolstudio.template.feature.home.domain.model.states.AllowedState
import com.anadolstudio.template.feature.home.domain.model.states.HomeAssistantAttribute
import com.anadolstudio.template.feature.main.MainGraph.navigateToDeviceDetail
import com.anadolstudio.template.util.mapIfContains
import com.anadolstudio.utils.states.LoadingContext
import com.anadolstudio.utils.states.lce.lceFlow
import com.anadolstudio.utils.states.lce.lceStateFlow
import com.anadolstudio.utils.states.lce.mapToLce
import com.anadolstudio.utils.states.lce.onEachContent
import com.anadolstudio.utils.states.lce.onEachProgressState
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn

internal class AddZigbeeViewModel @Inject constructor(
        private val websocketRepository: HAWebsocketRepository,
) : StatefulViewModel<AddZigbeeScreenState>(AddZigbeeScreenState()),
    AddZigbeeController {

    private companion object {
        const val ZIGBEE_2_MQTT = "zigbee2mqtt"
    }

    private val detachedScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        findZigbeeBridge()
        observeConnectionState()
    }

    private fun observeConnectionState() {
        websocketRepository.webSocketConnectionState
                .filterIsInstance<WebSocketConnectionState.ConnectedAuthenticated>()
                .mapToLce()
                .onEachContent {
                    subscribeToStateChangedEvents()
                    subscribeRegistryNewDevicesEvents()
                }
                .launchIn(viewModelScope)
    }

    private fun findZigbeeBridge() {
        lceFlow {
            websocketRepository.getDeviceList()
                    .firstOrNull { it.manufacturer?.lowercase() == ZIGBEE_2_MQTT }
                    ?: throw ZigbeeNotFoundError()
        }
                .onEachProgressState(
                        previousState = state.progressState,
                        loadingContext = LoadingContext.INIT_LOADING,
                        onNewProgressState = { newProgress -> updateState { copy(progressState = newProgress) } },
                )
                .onEachContent { zigbeeBridgeDevice ->
                    updateState { copy(zigbeeBridgeDevice = zigbeeBridgeDevice) }
                }
                .launchIn(viewModelScope)
    }

    private fun subscribeRegistryNewDevicesEvents() {
        websocketRepository.subscribeToRegistryNewDeviceEvents().mapToLce()
                .onEachContent { event -> loadNewDevice(event.deviceId) }
                .launchIn(viewModelScope)
    }

    private fun loadNewDevice(id: String) {
        lceFlow { websocketRepository.getDevice(deviceId = id, useCache = false) }
                .onEachContent { newDevice ->
                    val newDeviceList = state.newDeviceList + newDevice
                    updateState { copy(newDeviceList = newDeviceList.filterNotNull().toSet()) }
                }
                .launchIn(viewModelScope)
    }

    private fun subscribeToStateChangedEvents() {
        websocketRepository.subscribeToStateChangedEvents()
                .filterIsInstance(HomeAssistantStateChangedEvent.Update::class)
                .mapToLce()
                .onEachContent { stateChangedEvent ->
                    updateBridgeEntity(stateChangedEvent)
                    updateEntity(stateChangedEvent)
                }
                .launchIn(viewModelScope)
    }

    private fun updateBridgeEntity(event: HomeAssistantStateChangedEvent.Update) {
        val zigbeeBridgeDevice = state.zigbeeBridgeDevice ?: return
        val entityId = event.entityId
        val belongsToBridge = zigbeeBridgeDevice.entityMap.values.any { list -> list.any { it.entityId == entityId } }
        if (!belongsToBridge) return

        val newState = event.newState
        if (newState.allowedDomain == AllowedDomain.SWITCH) {
            updateState { copy(isSearching = newState.allowedState is AllowedState.On) }
        }

        val newEntityMap = zigbeeBridgeDevice.entityMap.mapValues { (_, entityList) ->
            entityList.mapIfContains(
                    condition = { it.entityId == entityId },
                    provideNewElement = { entity -> entity.copy(state = event.newState) }
            )
        }
        updateState { copy(zigbeeBridgeDevice = zigbeeBridgeDevice.copy(entityMap = newEntityMap)) }
    }

    private fun updateEntity(stateChangedEvent: HomeAssistantStateChangedEvent.Update) {
        val entityId = stateChangedEvent.entityId
        val newState = stateChangedEvent.newState

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

    override fun onCleared() {
        turnOffZigbeeFinder()

        super.onCleared()
    }

    private fun turnOffZigbeeFinder() {
        val permitJoinEntity = state.zigbeeBridgeDevice
                ?.targetEntityList
                ?.firstOrNull { it.allowedDomain == AllowedDomain.SWITCH }
                ?: return

        lceStateFlow {
            websocketRepository.callService(
                    entityId = permitJoinEntity.entityId,
                    domain = permitJoinEntity.domain,
                    service = SimpleToggleableService.Off
            )
        }.launchIn(detachedScope)
    }
}
