package com.anadolstudio.template.feature.home.presentation

import androidx.compose.runtime.Immutable
import com.anadolstudio.template.core.websocket.connection.WebSocketConnectionState
import com.anadolstudio.template.feature.home.domain.model.HomeAssistantDevice
import com.anadolstudio.template.feature.home.domain.model.states.HomeState
import com.anadolstudio.utils.states.ProgressState

@Immutable
internal data class HomeScreenState(
        val connectionState: WebSocketConnectionState = WebSocketConnectionState.Disconnected,
        val homeOverviewState: HomeOverviewState = HomeOverviewState(),
        val deviceState: DeviceState = DeviceState(),
) {
    private val progressStateList get() = listOf(
            homeOverviewState.progressState,
            deviceState.progressState,
    )

    private val isLoading: Boolean
        get() = connectionState != WebSocketConnectionState.ConnectedAuthenticated ||
                progressStateList.any { it is ProgressState.Loading }

    val progressState: ProgressState
        get() = when {
            progressStateList.any { it is ProgressState.Loading } -> ProgressState.Loading
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

        ) {
    val areaToDeviceMap: Map<String, List<HomeAssistantDevice>>
        get() = deviceSet
                .groupBy { device -> requireNotNull(device.area).name }

    val entityToDeviceMap: Map<String, HomeAssistantDevice>
        get() = deviceSet
                .flatMap { device -> device.allEntityList.map { entity -> entity.entityId to device } }
                .toMap()
}

@Immutable
internal data class HomeOverviewState(
        val progressState: ProgressState = ProgressState.Loading,
        val homeState: HomeState? = null,
)
