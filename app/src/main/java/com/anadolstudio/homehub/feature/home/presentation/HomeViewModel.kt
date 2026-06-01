package com.anadolstudio.homehub.feature.home.presentation

import androidx.lifecycle.viewModelScope
import com.anadolstudio.homehub.base.viewmodel.RestartableJob
import com.anadolstudio.homehub.base.viewmodel.StatefulViewModel
import com.anadolstudio.homehub.core.websocket.connection.WebSocketConnectionState
import com.anadolstudio.homehub.event.showMessage
import com.anadolstudio.homehub.event.showTodo
import com.anadolstudio.homehub.feature.home.domain.HARestRepository
import com.anadolstudio.homehub.feature.home.domain.HAWebsocketRepository
import com.anadolstudio.homehub.feature.home.domain.model.Area
import com.anadolstudio.homehub.feature.home.domain.model.HomeAssistantDevice
import com.anadolstudio.homehub.feature.home.domain.model.entity.HomeAssistantEntity
import com.anadolstudio.homehub.feature.home.domain.model.events.HomeAssistantStateChangedEvent
import com.anadolstudio.homehub.feature.home.domain.model.services.HomeAssistantService
import com.anadolstudio.homehub.feature.home.domain.model.states.HomeAssistantAttribute
import com.anadolstudio.homehub.feature.main.MainGraph.navigateToAdd
import com.anadolstudio.homehub.feature.main.MainGraph.navigateToAutomationList
import com.anadolstudio.homehub.feature.main.MainGraph.navigateToDeviceDetail
import com.anadolstudio.homehub.feature.main.MainGraph.navigateToHistory
import com.anadolstudio.homehub.util.mapIfContains
import com.anadolstudio.utils.states.LoadingContext
import com.anadolstudio.utils.states.ProgressState
import com.anadolstudio.utils.states.lce.lceFlow
import com.anadolstudio.utils.states.lce.mapContent
import com.anadolstudio.utils.states.lce.mapToLce
import com.anadolstudio.utils.states.lce.onEachContent
import com.anadolstudio.utils.states.lce.onEachProgressState
import javax.inject.Inject
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn

internal class HomeViewModel @Inject constructor(
        private val restRepository: HARestRepository,
        private val websocketRepository: HAWebsocketRepository,
) : StatefulViewModel<HomeScreenState>(HomeScreenState()), HomeController {

    private var stateChangedJob by RestartableJob()
    private var deviceChangesJob by RestartableJob()

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
        lceFlow {
            val devices = websocketRepository.getDeviceList()
            val areas = websocketRepository.getAreaList(useCache = true)

            return@lceFlow devices to areas
        }
                .onEachProgressState(
                        previousState = state.progressState,
                        loadingContext = loadingContext,
                        onNewProgressState = {
                            updateState { copy(deviceState = deviceState.copy(progressState = it)) }
                        }
                )
                .mapContent { (deviceList, areas) ->
                    val deviceSet = deviceList
                            .filter { device -> device.isBindToArea }
                            .toSet()

                    return@mapContent deviceSet to areas
                }
                .onEachContent { (deviceSet, areas) ->
                    val selectedAreaId = state.selectedAreaId
                            ?.takeIf { id -> areas.any { it.areaId == id } }

                    val newDeviceState = state.deviceState.copy(deviceSet = deviceSet, availableAreas = areas)
                    updateState { copy(deviceState = newDeviceState, selectedAreaId = selectedAreaId) }
                    subscribeChangedEvents()
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

    private fun subscribeChangedEvents() {
        stateChangedJob = websocketRepository.subscribeToStateChangedEvents()
                .filterIsInstance(HomeAssistantStateChangedEvent.Update::class)
                .mapToLce()
                .onEachContent { stateChangedEvent -> updateEntity(stateChangedEvent) }
                .launchIn(viewModelScope)

        deviceChangesJob = websocketRepository.subscribeToRegistryNewDeviceEvents()
                .mapToLce()
                .onEachContent { loadDevices(LoadingContext.REFRESH) }
                .launchIn(viewModelScope)
    }

    private fun updateEntity(stateChangedEvent: HomeAssistantStateChangedEvent.Update) {
        val entityId = stateChangedEvent.entityId
        val newState = stateChangedEvent.newState

        val changedDevice = state.deviceState.entityToDeviceMap[entityId] ?: return
        val newEntityList = changedDevice.entityMap.mapValues { (_, entityList) ->
            entityList.mapIfContains(
                    condition = { it.entityId == entityId },
                    provideNewElement = { entity -> entity.copy(state = newState) }
            )
        }

        val newDevice = changedDevice.copy(entityMap = newEntityList)
        val newDeviceSet = state.deviceState.deviceSet.toMutableSet()
                .apply {
                    remove(changedDevice)
                    add(newDevice)
                }
        updateState { copy(deviceState = deviceState.copy(deviceSet = newDeviceSet)) }
    }

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

    override fun onDeviceClicked(device: HomeAssistantDevice) = navigateToDeviceDetail(device)

    override fun onAutomationClicked() = navigateToAutomationList()

    override fun onAddClicked() = navigateToAdd()

    override fun onHistoryClicked() = navigateToHistory()

    override fun onAreaClicked() = showTodo()

    override fun onAreaSelected(area: Area?) = updateState { copy(selectedAreaId = area?.areaId) }
}
