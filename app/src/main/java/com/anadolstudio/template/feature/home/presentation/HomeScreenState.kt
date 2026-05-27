package com.anadolstudio.template.feature.home.presentation

import androidx.compose.runtime.Immutable
import com.anadolstudio.template.core.websocket.connection.WebSocketConnectionState
import com.anadolstudio.template.feature.home.domain.model.Area
import com.anadolstudio.template.feature.home.domain.model.HomeAssistantDevice
import com.anadolstudio.template.feature.home.domain.model.states.HomeAssistantState
import com.anadolstudio.template.feature.home.domain.model.states.HomeAttributes
import com.anadolstudio.utils.states.ProgressState

@Immutable
internal data class HomeScreenState(
        val connectionState: WebSocketConnectionState = WebSocketConnectionState.Disconnected,
        val homeOverviewState: HomeOverviewState = HomeOverviewState(),
        val deviceState: DeviceState = DeviceState(),
        val selectedAreaId: String? = null,
) {

    val filteredAreaToDeviceMap: Map<String, List<HomeAssistantDevice>>
        get() = deviceState.areaToDeviceMap(selectedAreaId = selectedAreaId)

    private val progressStateList get() = listOf(
            homeOverviewState.progressState,
            deviceState.progressState,
    )

    private val isLoading: Boolean
        get() = connectionState != WebSocketConnectionState.ConnectedAuthenticated

    val progressState: ProgressState
        get() = when {
            isLoading || progressStateList.any { it is ProgressState.Loading } -> ProgressState.Loading
            progressStateList.all { it is ProgressState.Content } -> ProgressState.Content
            progressStateList.any { it is ProgressState.Error } -> {
                val errorProgressState = progressStateList
                        .firstOrNull { it is ProgressState.Error }
                        as? ProgressState.Error

                ProgressState.Error(errorProgressState?.error)
            }

            else -> ProgressState.Loading
        }
}

@Immutable
internal data class DeviceState(
        val progressState: ProgressState = ProgressState.Loading,
        val deviceSet: Set<HomeAssistantDevice> = emptySet(),
        val availableAreas: List<Area> = emptyList(),
) {
    fun areaToDeviceMap(selectedAreaId: String? = null): Map<String, List<HomeAssistantDevice>> = deviceSet
            .asSequence()
            .filter { device -> selectedAreaId == null || device.area?.areaId == selectedAreaId }
            .groupBy { device -> device.area?.name.toString() }
            .mapValues { (_, devices) -> devices.sortedDevice().toList() }
            .toSortedMap()

    val entityToDeviceMap: Map<String, HomeAssistantDevice>
        get() = deviceSet
                .flatMap { device -> device.allEntityList.map { entity -> entity.entityId to device } }
                .toMap()

    private fun Collection<HomeAssistantDevice>.sortedDevice(): Set<HomeAssistantDevice> = this
            .sortedBy { it.model + it.name + it.id }
            .toCollection(LinkedHashSet())
}

@Immutable
internal data class HomeOverviewState(
        val progressState: ProgressState = ProgressState.Loading,
        val homeState: HomeAssistantState<HomeAttributes>? = null,
)
