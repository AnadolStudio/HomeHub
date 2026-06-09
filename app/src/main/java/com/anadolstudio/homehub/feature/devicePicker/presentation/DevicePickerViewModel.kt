package com.anadolstudio.homehub.feature.devicePicker.presentation

import androidx.lifecycle.viewModelScope
import com.anadolstudio.homehub.base.viewmodel.StatefulViewModel
import com.anadolstudio.homehub.event.navigateUp
import com.anadolstudio.homehub.event.showError
import com.anadolstudio.homehub.feature.home.domain.HAWebsocketRepository
import com.anadolstudio.homehub.feature.home.domain.model.Area
import com.anadolstudio.homehub.feature.home.domain.model.HomeAssistantDevice
import com.anadolstudio.homehub.feature.main.MainGraph.navigateToDemoDeviceDetailFromPicker
import com.anadolstudio.homehub.feature.main.MainGraph.navigateToEntityPicker
import com.anadolstudio.utils.states.LoadingContext
import com.anadolstudio.utils.states.lce.lceFlow
import com.anadolstudio.utils.states.lce.onEachContent
import com.anadolstudio.utils.states.lce.onEachError
import com.anadolstudio.utils.states.lce.onEachProgressState
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.launchIn

internal class DevicePickerViewModel @AssistedInject constructor(
        @Assisted private val excludedDeviceIds: Set<String>,
        @Assisted private val directResultKey: String?,
        @Assisted private val mode: DevicePickerMode,
        private val websocketRepository: HAWebsocketRepository,
) : StatefulViewModel<DevicePickerScreenState>(DevicePickerScreenState()),
    DevicePickerController {

    init {
        load(LoadingContext.INIT_LOADING)
    }

    private fun load(loadingContext: LoadingContext) {
        lceFlow {
            val devices = websocketRepository.getDeviceList()
                    .filterNot { device -> excludedDeviceIds.contains(device.id) }
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
        when (mode) {
            DevicePickerMode.AUTOMATION -> {
                if (directResultKey != null) navigateUp(directResultKey to device)
                navigateToEntityPicker(device = device, selectedEntitySet = emptySet())
            }

            DevicePickerMode.NORMAL ->
                if (directResultKey != null) {
                    navigateUp(directResultKey to device)
                } else {
                    navigateToDemoDeviceDetailFromPicker(device = device, selectedEntitySet = emptySet())
                }
        }
    }

    override fun onRetryClicked() = load(LoadingContext.RETRY)

    override fun onCloseClicked() = navigateUp()

    @AssistedFactory
    interface Factory {
        fun create(
                excludedDeviceIds: Set<String>,
                directResultKey: String?,
                mode: DevicePickerMode,
        ): DevicePickerViewModel
    }
}
