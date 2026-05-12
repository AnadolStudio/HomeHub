package com.anadolstudio.template.feature.home.presentation

import androidx.lifecycle.viewModelScope
import com.anadolstudio.template.base.viewmodel.StatefulViewModel
import com.anadolstudio.template.core.websocket.connection.WebSocketConnectionState
import com.anadolstudio.template.event.showTodo
import com.anadolstudio.template.feature.common.data.PreferencesStorage
import com.anadolstudio.template.feature.home.domain.HomeAssistantRepository
import com.anadolstudio.template.feature.home.domain.model.HomeAssistantDevice
import com.anadolstudio.template.feature.home.domain.model.HomeAssistantEntity
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

    private fun onGetApiStatusClicked() {
        updateState { copy(progressState = ProgressState.Loading) }

        viewModelScope.launch {
            runCatching { haRepository.getApiStatus() }
                    .onSuccess { apiStatus ->
                        updateState {
                            copy(
                                    progressState = ProgressState.Content,
                                    apiStatusMessage = apiStatus.message,
                            )
                        }
                    }
                    .onFailure { error ->
                        updateState { copy(progressState = ProgressState.Error(error)) }
                    }
        }
    }

    override fun onEntityClicked(entity: HomeAssistantEntity, service: HomeAssistantService) {
        viewModelScope.launch {
            runCatching {
                haRepository.callService(
                        entityId = entity.id,
                        domain = entity.domain,
                        service = service.toStringService(),
                )
            }
        }
    }

    override fun onDeviceClicked(device: HomeAssistantDevice) {
        showTodo()
    }

    fun onGetEntitiesClicked() {
        updateState { copy(progressState = ProgressState.Loading) }

        viewModelScope.launch {
            runCatching {
            }
                    .onSuccess { entities ->
                    }
                    .onFailure { error ->
                        updateState { copy(progressState = ProgressState.Error(error)) }
                    }
        }
    }

    fun onToggleClicked(entity: HomeAssistantEntity) {
        viewModelScope.launch {
            runCatching {
                haRepository.callService(
                        entityId = entity.id,
                        domain = entity.domain,
                        service = SERVICE_TOGGLE,
                )
            }.onFailure { error ->
                updateState { copy(progressState = ProgressState.Error(error)) }
            }
        }
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
                                .groupBy { device -> requireNotNull(device.area).name }
                    }
                    .onSuccess { devices ->
                        updateState {
                            copy(progressState = ProgressState.Content, deviceMap = devices)
                        }
                    }
                    .onFailure { error ->
                        updateState { copy(progressState = ProgressState.Error(error)) }
                    }
        }
    }

    private companion object {
        const val SERVICE_TOGGLE = "toggle"
    }
}
