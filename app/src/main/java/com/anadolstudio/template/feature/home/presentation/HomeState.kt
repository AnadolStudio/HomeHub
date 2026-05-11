package com.anadolstudio.template.feature.home.presentation

import androidx.compose.runtime.Immutable
import com.anadolstudio.template.core.websocket.connection.WebSocketConnectionState
import com.anadolstudio.template.feature.home.data.model.EntityDomain
import com.anadolstudio.template.feature.home.domain.model.Device
import com.anadolstudio.utils.states.ProgressState

@Immutable
internal data class HomeState(
        val progressState: ProgressState = ProgressState.Content,
        val apiStatusMessage: String? = null,
        val connectionState: WebSocketConnectionState = WebSocketConnectionState.Disconnected,
        val selectedDomain: EntityDomain? = null,
        val deviceMap: Map<String, List<Device>> = emptyMap(),
)
