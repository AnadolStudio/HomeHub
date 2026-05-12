package com.anadolstudio.template.feature.home.presentation

import androidx.compose.runtime.Immutable
import com.anadolstudio.template.core.websocket.connection.WebSocketConnectionState
import com.anadolstudio.template.feature.home.domain.model.HomeAssistantDevice
import com.anadolstudio.utils.states.ProgressState

@Immutable
internal data class HomeScreenState(
        val progressState: ProgressState = ProgressState.Content,
        val connectionState: WebSocketConnectionState = WebSocketConnectionState.Disconnected,
        val deviceState: HomeScreenDeviceState = HomeScreenDeviceState(),
) {

}

@Immutable
internal data class HomeScreenDeviceState(
        val deviceSet: Set<HomeAssistantDevice> = emptySet(),
) {
    val areaToDeviceMap: Map<String, List<HomeAssistantDevice>> get() = deviceSet
            .groupBy { device -> requireNotNull(device.area).name }

    val entityToDeviceMap: Map<String, HomeAssistantDevice> get() = deviceSet
            .flatMap { device -> device.entitySet.map { entity -> entity.entityId to device } }
            .toMap()
}
