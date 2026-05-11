package com.anadolstudio.template.feature.home.presentation

import androidx.lifecycle.viewModelScope
import com.anadolstudio.template.base.viewmodel.StatefulViewModel
import com.anadolstudio.template.core.websocket.WebSocketCore
import com.anadolstudio.template.core.websocket.connection.WebSocketConnectionState
import com.anadolstudio.template.core.websocket.message.WsRequest
import com.anadolstudio.template.feature.common.data.PreferencesStorage
import com.anadolstudio.template.feature.home.data.model.HomeAssistantDevice
import com.anadolstudio.template.feature.home.data.model.HomeAssistantEntity
import com.anadolstudio.template.feature.home.domain.HomeAssistantRepository
import com.anadolstudio.utils.states.ProgressState
import javax.inject.Inject
import kotlinx.coroutines.launch

internal class HomeViewModel @Inject constructor(
        private val homeAssistantRepository: HomeAssistantRepository,
        private val webSocketCore: WebSocketCore,
        private val preferencesStorage: PreferencesStorage,
) : StatefulViewModel<HomeState>(HomeState()), HomeController {

    init {
        observeConnectionState()
    }

    override fun onStart() {
        super.onStart()
        webSocketCore.resume()
    }

    override fun onStop() {
        super.onStop()
        webSocketCore.pause()
    }

    override fun onTestButtonClicked() {
        viewModelScope.launch {
            runCatching { webSocketCore.subscribe(WsRequest("s")) }
                    .onFailure { error ->
                updateState { copy(progressState = ProgressState.Error(error)) }
            }
        }
    }

    private fun onGetApiStatusClicked() {
        updateState { copy(progressState = ProgressState.Loading) }

        viewModelScope.launch {
            runCatching { homeAssistantRepository.getApiStatus() }
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

    private fun connectWebSocket() {
        viewModelScope.launch {
            runCatching { webSocketCore.connect() }
                    .onFailure { error ->
                        updateState { copy(progressState = ProgressState.Error(error)) }
                    }
        }
    }

    fun onToggleClicked(entity: HomeAssistantEntity) {
        viewModelScope.launch {
            runCatching {
                homeAssistantRepository.callService(
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
            webSocketCore.connectionState.collect { connectionState ->
                updateState { copy(connectionState = connectionState) }
                if (connectionState is WebSocketConnectionState.ConnectedAuthenticated &&
                        state.devices.isEmpty()
                ) {
                    loadSwitches()
                }
            }
        }
    }

    /**
     * Загружает MQTT-свитчи: дёргает entity registry, фильтрует по prefix `switch.` + `pl == "mqtt"`,
     * параллельно дёргает services и берёт services["switch"]. Группирует сущности по deviceId
     * и складывает в [HomeAssistantDevice]. Для устройств без `deviceId` (если такие есть)
     * группа складывается по entityId как fallback.
     */
    private fun loadSwitches() {
        updateState { copy(progressState = ProgressState.Loading) }

        viewModelScope.launch {
            runCatching {
                val entities = homeAssistantRepository.getEntities()
                        .filter { it.entityId.startsWith(SWITCH_PREFIX) && it.platform == PLATFORM_MQTT }
                val services = homeAssistantRepository.getServiceList()
                val switchServices = services[SWITCH_DOMAIN]?.keys.orEmpty().toSet()

                entities
                        .groupBy { it.deviceId ?: it.entityId }
                        .map { (deviceId, deviceEntities) ->
                            HomeAssistantDevice(
                                    id = deviceId,
                                    name = deviceEntities.firstOrNull()?.displayName,
                                    list = deviceEntities.map { entity ->
                                        HomeAssistantEntity(
                                                id = entity.entityId,
                                                domain = SWITCH_DOMAIN,
                                                services = switchServices,
                                        )
                                    },
                            )
                        }
                        .sortedBy { it.id }
            }
                    .onSuccess { devices ->
                        updateState {
                            copy(progressState = ProgressState.Content, devices = devices)
                        }
                    }
                    .onFailure { error ->
                        updateState { copy(progressState = ProgressState.Error(error)) }
                    }
        }
    }

    private companion object {
        const val SWITCH_PREFIX = "switch."
        const val SWITCH_DOMAIN = "switch"
        const val PLATFORM_MQTT = "mqtt"
        const val SERVICE_TOGGLE = "toggle"
    }
}
