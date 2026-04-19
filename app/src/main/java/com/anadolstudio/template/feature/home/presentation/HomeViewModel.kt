package com.anadolstudio.template.feature.home.presentation

import androidx.lifecycle.viewModelScope
import com.anadolstudio.template.base.viewmodel.StatefulViewModel
import com.anadolstudio.template.core.websocket.WebSocketCore
import com.anadolstudio.template.feature.common.data.PreferencesStorage
import com.anadolstudio.template.feature.home.data.model.EntityDomain
import com.anadolstudio.template.feature.home.domain.HomeAssistantRepository
import com.anadolstudio.utils.states.ProgressState
import javax.inject.Inject
import kotlinx.coroutines.launch

internal class HomeViewModel @Inject constructor(
        private val homeAssistantRepository: HomeAssistantRepository,
        private val webSocketCore: WebSocketCore,
        private val preferencesStorage: PreferencesStorage,
) : StatefulViewModel<HomeState>(HomeState()) {

    init {
        observeConnectionState()
        connectWebSocket()
    }

    fun onGetApiStatusClicked() {
        updateState { copy(progressState = ProgressState.Loading) }

        viewModelScope.launch {
            runCatching { homeAssistantRepository.getApiStatus() }
                    .onSuccess { apiStatus ->
                        updateState {
                            copy(
                                    progressState = ProgressState.Content,
                                    apiStatusMessage = apiStatus.message,
                            )
                        }
                    }
                    .onFailure { error ->
                        updateState { copy(progressState = ProgressState.Error(error)) }
                    }
        }
    }

    fun onGetEntitiesClicked() {
        updateState { copy(progressState = ProgressState.Loading) }

        viewModelScope.launch {
            runCatching {
                homeAssistantRepository.callService()
                homeAssistantRepository.getEntities()
            }
                    .onSuccess { entities ->
                        updateState {
                            copy(
                                    progressState = ProgressState.Content,
                                    allEntities = entities,
                            )
                        }
                    }
                    .onFailure { error ->
                        updateState { copy(progressState = ProgressState.Error(error)) }
                    }
        }
    }

    private fun connectWebSocket() {
        val baseUrl = preferencesStorage.baseUrl ?: return
        val accessToken = preferencesStorage.accessToken ?: return

        val wsUrl = baseUrl
                .replace("https://", "wss://")
                .replace("http://", "ws://")
                .trimEnd('/') + "/api/websocket"

        viewModelScope.launch {
            runCatching { webSocketCore.connect(wsUrl, accessToken) }
                    .onFailure { error ->
                        updateState { copy(progressState = ProgressState.Error(error)) }
                    }
        }
    }

    private fun observeConnectionState() {
        viewModelScope.launch {
            webSocketCore.connectionState.collect { connectionState ->
                updateState { copy(connectionState = connectionState) }
            }
        }
    }

    fun onDomainFilterClicked(domain: EntityDomain?) {
        updateState { copy(selectedDomain = domain) }
    }

    override fun onCleared() {
        super.onCleared()
        webSocketCore.close()
    }
}
