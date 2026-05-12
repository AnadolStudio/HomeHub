package com.anadolstudio.template.feature.home.presentation

import androidx.lifecycle.viewModelScope
import com.anadolstudio.template.base.viewmodel.StatefulViewModel
import com.anadolstudio.template.core.websocket.connection.WebSocketConnectionState
import com.anadolstudio.template.event.showTodo
import com.anadolstudio.template.feature.common.data.PreferencesStorage
import com.anadolstudio.template.feature.home.domain.HomeAssistantRepository
import com.anadolstudio.template.feature.home.domain.model.HomeAssistantDevice
import com.anadolstudio.template.feature.home.domain.model.HomeAssistantEntity
import com.anadolstudio.template.feature.home.domain.model.events.HomeAssistantStateChangedEvent
import com.anadolstudio.template.feature.home.domain.model.services.HomeAssistantService
import com.anadolstudio.utils.states.ProgressState
import javax.inject.Inject
import kotlinx.coroutines.launch

internal class HomeViewModel @Inject constructor(
        private val haRepository: HomeAssistantRepository,
        private val preferencesStorage: PreferencesStorage,
) : StatefulViewModel<HomeScreenState>(HomeScreenState()), HomeController {

    init {
        observeConnectionState()
    }

    private fun observeConnectionState() {
        viewModelScope.launch {
            haRepository.webSocketConnectionState.collect { connectionState ->
                updateState { copy(connectionState = connectionState) }
                if (connectionState is WebSocketConnectionState.ConnectedAuthenticated) {
                    loadDevices()
                }
            }
        }
    }

    private fun loadDevices() {
        updateState { copy(progressState = ProgressState.Loading) }

        viewModelScope.launch {
            runCatching { haRepository.getDeviceList() }
                    .map { deviceList ->
                        deviceList
                                .filter { device -> device.isBindToArea }
                                .toSortedSet(
                                        Comparator.comparing { it.name }
                                )
                    }
                    .onSuccess { devicesSet ->
                        val deviceState = HomeScreenDeviceState(deviceSet = devicesSet)
                        updateState { copy(progressState = ProgressState.Content, deviceState = deviceState) }

                        subscribeToStateChangedEvents()
                    }
                    .onFailure { error ->
                        updateState { copy(progressState = ProgressState.Error(error)) }
                    }
        }
    }

    override fun onStart() {
        super.onStart()
        haRepository.startWebSocketConnection()
    }

    override fun onStop() {
        super.onStop()
        haRepository.stopWebSocketConnection()
    }

    override fun onTestButtonClicked() {
    }

    private fun subscribeToStateChangedEvents() {
        updateState { copy(progressState = ProgressState.Loading) }

        viewModelScope.launch {
            haRepository.subscribeToStateChangedEvents().collect { stateChangedEvent ->
                updateEntity(stateChangedEvent)
            }
        }
    }

    private fun updateEntity(stateChangedEvent: HomeAssistantStateChangedEvent) {
        val entityId = stateChangedEvent.entityId
        val newAllowedState = stateChangedEvent.allowedState

        val changedDevice = state.deviceState.entityToDeviceMap[entityId] ?: return
        val newEntityList = changedDevice.entitySet.map { entity ->
            if (entity.entityId == entityId) {
                entity.copy(allowedState = newAllowedState)
            } else {
                entity
            }
        }

        val newDevice = changedDevice.copy(entitySet = newEntityList)
        val newDeviceSet = state.deviceState.deviceSet.toMutableSet().apply {
            remove(changedDevice)
            add(newDevice)
        }
        updateState {
            copy(
                    progressState = ProgressState.Content,
                    deviceState = deviceState.copy(deviceSet = newDeviceSet),
            )
        }
    }

    override fun onEntityClicked(entity: HomeAssistantEntity, service: HomeAssistantService) {
        viewModelScope.launch {
            runCatching {
                haRepository.callService(
                        entityId = entity.entityId,
                        domain = entity.domain,
                        service = service.toStringService(),
                )
            }
        }
    }

    override fun onDeviceClicked(device: HomeAssistantDevice) {
        showTodo()
    }
}
