package com.anadolstudio.template.feature.home.presentation

import androidx.lifecycle.viewModelScope
import com.anadolstudio.template.base.viewmodel.StatefulViewModel
import com.anadolstudio.template.core.websocket.connection.WebSocketConnectionState
import com.anadolstudio.template.event.showTodo
import com.anadolstudio.template.feature.home.domain.HARestRepository
import com.anadolstudio.template.feature.home.domain.HAWebsocketRepository
import com.anadolstudio.template.feature.home.domain.model.HomeAssistantDevice
import com.anadolstudio.template.feature.home.domain.model.HomeAssistantEntity
import com.anadolstudio.template.feature.home.domain.model.events.HomeAssistantStateChangedEvent
import com.anadolstudio.template.feature.home.domain.model.services.HomeAssistantService
import com.anadolstudio.template.feature.main.MainGraph.navigateToAddDevice
import com.anadolstudio.template.feature.main.MainGraph.navigateToHistory
import com.anadolstudio.template.feature.main.MainGraph.navigateToSceneList
import com.anadolstudio.utils.states.LoadingContext
import com.anadolstudio.utils.states.ProgressState
import com.anadolstudio.utils.states.lce.lceFlow
import com.anadolstudio.utils.states.lce.lceStateFlow
import com.anadolstudio.utils.states.lce.mapContent
import com.anadolstudio.utils.states.lce.mapToLce
import com.anadolstudio.utils.states.lce.onEachContent
import com.anadolstudio.utils.states.lce.onEachProgressState
import javax.inject.Inject
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch

internal class HomeViewModel @Inject constructor(
        private val restRepository: HARestRepository,
        private val websocketRepository: HAWebsocketRepository,
) : StatefulViewModel<HomeScreenState>(HomeScreenState()), HomeController {

    init {
        loadHomeName(loadingContext = LoadingContext.INIT_LOADING)
        observeConnectionState()
    }

    private fun observeConnectionState() {
        this@HomeViewModel.websocketRepository.webSocketConnectionState.mapToLce()
                .onEachContent { connectionState ->
                    updateState { copy(connectionState = connectionState) }

                    if (connectionState !is WebSocketConnectionState.ConnectedAuthenticated) return@onEachContent

                    val isContent = state.progressState is ProgressState.Content
                    val hasData = state.deviceState.deviceSet.isNotEmpty()

                    when {
                        isContent && hasData -> loadDevices(LoadingContext.REFRESH)
                        isContent && !hasData -> loadDevices(LoadingContext.RETRY)
                        else -> loadDevices(LoadingContext.INIT_LOADING)
                    }
                }
                .launchIn(viewModelScope)
    }

    private fun loadDevices(loadingContext: LoadingContext) {
        lceFlow { websocketRepository.getDeviceList() }
                .onEachProgressState(
                        previousState = state.progressState,
                        loadingContext = loadingContext,
                        onNewProgressState = {
                            updateState { copy(deviceState = deviceState.copy(progressState = it)) }
                        }
                )
                .mapContent { deviceList ->
                    deviceList
                            .filter { device -> device.isBindToArea }
                            .sortedBy { it.name }
                            .toCollection(LinkedHashSet())
                }
                .onEachContent { deviceSet ->
                    updateState { copy(deviceState = deviceState.copy(deviceSet = deviceSet)) }

                    subscribeToStateChangedEvents()
                }
                .launchIn(viewModelScope)
    }

    private fun loadHomeName(loadingContext: LoadingContext) {
        lceFlow { restRepository.getHomeOverview() }
                .onEachProgressState(
                        previousState = state.progressState,
                        loadingContext = loadingContext,
                        onNewProgressState = {
                            updateState { copy(homeOverviewState = homeOverviewState.copy(progressState = it)) }
                        }
                )
                .onEachContent { homeOverview ->
                    updateState { copy(homeOverviewState = homeOverviewState.copy(homeState = homeOverview)) }
                }
                .launchIn(viewModelScope)
    }

    private fun subscribeToStateChangedEvents() {
        viewModelScope.launch {
            websocketRepository.subscribeToStateChangedEvents()
                    .collect { stateChangedEvent -> updateEntity(stateChangedEvent) }
        }
    }

    private fun updateEntity(stateChangedEvent: HomeAssistantStateChangedEvent) {
        val entityId = stateChangedEvent.entityId
        val newAllowedState = stateChangedEvent.allowedState

        val changedDevice = state.deviceState.entityToDeviceMap[entityId] ?: return
        val newEntityList = changedDevice.entityList.map { entity ->
            if (entity.entityId == entityId) {
                entity.copy(allowedState = newAllowedState)
            } else {
                entity
            }
        }

        val newDevice = changedDevice.copy(entityList = newEntityList)
        val newDeviceSet = state.deviceState.deviceSet.toMutableSet()
                .apply {
                    remove(changedDevice)
                    add(newDevice)
                }
                .sortedBy { it.name }
                .toCollection(LinkedHashSet())
        updateState { copy(deviceState = deviceState.copy(deviceSet = newDeviceSet)) }
    }

    override fun onEntityClicked(entity: HomeAssistantEntity, service: HomeAssistantService) {
        lceStateFlow {
            websocketRepository.callService(
                    entityId = entity.entityId,
                    domain = entity.domain,
                    service = service.toStringService(),
            )
        }.launchIn(viewModelScope)
    }

    override fun onDeviceClicked(device: HomeAssistantDevice) {
        showTodo()
    }

    override fun onSceneClicked() = navigateToSceneList()

    override fun onAddDeviceClicked() = navigateToAddDevice()

    override fun onHistoryClicked() = navigateToHistory()
}
