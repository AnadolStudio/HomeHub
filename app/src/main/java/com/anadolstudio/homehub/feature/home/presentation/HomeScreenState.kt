package com.anadolstudio.homehub.feature.home.presentation

import androidx.compose.runtime.Immutable
import com.anadolstudio.homehub.core.websocket.connection.WebSocketConnectionState
import com.anadolstudio.homehub.feature.home.domain.model.Area
import com.anadolstudio.homehub.feature.home.domain.model.HomeAssistantDevice
import com.anadolstudio.homehub.feature.home.domain.model.states.HomeAssistantState
import com.anadolstudio.homehub.feature.home.domain.model.states.HomeAttributes
import com.anadolstudio.utils.states.ProgressState

@Immutable
internal data class HomeScreenState(
        val connectionState: WebSocketConnectionState = WebSocketConnectionState.Disconnected,
        val homeOverviewState: HomeOverviewState = HomeOverviewState(),
        val deviceState: DeviceState = DeviceState(),
        val selectedAreaId: String? = null,
) {

    val homeName: String? = homeOverviewState.homeState?.attributes?.friendlyName

    val filteredAreaToDeviceMap: Map<String, List<HomeAssistantDevice>>
        get() = deviceState.areaToDeviceMap(selectedAreaId = selectedAreaId)

    private val hasConnection: Boolean
        get() = connectionState == WebSocketConnectionState.ConnectedAuthenticated

    val progressState: ProgressState
        get() = if (!hasConnection) ProgressState.Loading else deviceState.progressState
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
