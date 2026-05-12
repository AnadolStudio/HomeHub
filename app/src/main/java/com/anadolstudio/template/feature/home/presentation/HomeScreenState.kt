package com.anadolstudio.template.feature.home.presentation

import androidx.compose.runtime.Immutable
import com.anadolstudio.template.core.websocket.connection.WebSocketConnectionState
import com.anadolstudio.template.feature.home.domain.model.HomeAssistantDevice
import com.anadolstudio.utils.states.ProgressState

@Immutable
internal data class HomeScreenState(
        val progressState: ProgressState = ProgressState.Content,
        val apiStatusMessage: String? = null,
        val connectionState: WebSocketConnectionState = WebSocketConnectionState.Disconnected,
        val deviceMap: Map<String, List<HomeAssistantDevice>> = emptyMap(),
)
