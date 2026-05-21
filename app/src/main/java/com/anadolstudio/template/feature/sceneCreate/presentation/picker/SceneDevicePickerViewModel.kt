package com.anadolstudio.template.feature.sceneCreate.presentation.picker

import androidx.lifecycle.viewModelScope
import com.anadolstudio.template.base.viewmodel.StatefulViewModel
import com.anadolstudio.template.event.navigateUp
import com.anadolstudio.template.event.showError
import com.anadolstudio.template.feature.home.domain.HAWebsocketRepository
import com.anadolstudio.template.feature.home.domain.model.Area
import com.anadolstudio.template.feature.home.domain.model.HomeAssistantDevice
import com.anadolstudio.template.feature.main.MainGraph.navigateToDeviceDetailFromPicker
import com.anadolstudio.utils.states.LoadingContext
import com.anadolstudio.utils.states.lce.lceFlow
import com.anadolstudio.utils.states.lce.onEachContent
import com.anadolstudio.utils.states.lce.onEachError
import com.anadolstudio.utils.states.lce.onEachProgressState
import javax.inject.Inject
import kotlinx.coroutines.flow.launchIn

internal class SceneDevicePickerViewModel @Inject constructor(
        private val websocketRepository: HAWebsocketRepository,
) : StatefulViewModel<SceneDevicePickerScreenState>(SceneDevicePickerScreenState()),
    SceneDevicePickerController {

    init {
        load(LoadingContext.INIT_LOADING)
    }

    private fun load(loadingContext: LoadingContext) {
        lceFlow {
            val devices = websocketRepository.getDeviceList()
            val areas = websocketRepository.getAreaList()
            devices to areas
        }
                .onEachProgressState(
                        previousState = state.progressState,
                        loadingContext = loadingContext,
                        onNewProgressState = { newProgress -> updateState { copy(progressState = newProgress) } },
                )
                .onEachContent { (devices, areas) ->
                    updateState {
                        copy(
                                allDevices = devices,
                                availableAreas = areas.sortedBy { it.name },
                        )
                    }
                }
                .onEachError { showError(it) }
                .launchIn(viewModelScope)
    }

    override fun onSearchQueryChanged(query: String) {
        updateState { copy(searchQuery = query) }
    }

    override fun onAreaSelected(area: Area?) = updateState { copy(selectedAreaId = area?.areaId) }

    override fun onDeviceClicked(device: HomeAssistantDevice) {
        // Сразу попаем picker — после возврата из DeviceDetail юзер окажется на SceneCreate.
        navigateToDeviceDetailFromPicker(device)
    }

    override fun onRetryClicked() = load(LoadingContext.RETRY)

    override fun onCloseClicked() = navigateUp()
}
