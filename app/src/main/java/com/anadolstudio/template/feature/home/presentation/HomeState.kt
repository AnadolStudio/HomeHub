package com.anadolstudio.template.feature.home.presentation

import com.anadolstudio.template.core.websocket.WebSocketConnectionState
import com.anadolstudio.template.feature.home.data.model.EntityDomain
import com.anadolstudio.template.feature.home.data.model.EntityRegistryEntry
import com.anadolstudio.utils.states.ProgressState

internal data class HomeState(
        val progressState: ProgressState = ProgressState.Content,
        val apiStatusMessage: String? = null,
        val connectionState: WebSocketConnectionState = WebSocketConnectionState.Disconnected,
        val allEntities: List<EntityRegistryEntry> = emptyList(),
        val selectedDomain: EntityDomain? = null,
) {
    val filteredEntities: List<EntityRegistryEntry>
        get() {
            val domain = selectedDomain ?: return allEntities
            return allEntities.filter { it.domain == domain.prefix }
        }
}
