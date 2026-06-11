package com.anadolstudio.homehub.feature.main

import androidx.lifecycle.viewModelScope
import com.anadolstudio.homehub.base.viewmodel.StatefulViewModel
import com.anadolstudio.homehub.feature.common.data.PreferencesStorage
import com.anadolstudio.homehub.feature.home.domain.HAWebsocketRepository
import com.anadolstudio.utils.states.lce.mapToLce
import com.anadolstudio.utils.states.lce.onEachContent
import javax.inject.Inject
import kotlinx.coroutines.flow.launchIn

internal class MainViewModel @Inject constructor(
        private val preferencesStorage: PreferencesStorage,
        private val webSocketRepository: HAWebsocketRepository,
) : StatefulViewModel<MainScreenState>(
        MainScreenState(preferencesStorage.accessToken != null)
), MainController {

    init {
        preferencesStorage.isAuthenticatedFlow.mapToLce()
                .onEachContent { newAuthenticatedState ->
                    startWebSocketIfNeed(newAuthenticatedState)
                    updateState { copy(isAuthenticated = newAuthenticatedState) }
                }
                .launchIn(viewModelScope)
    }

    override fun onBackClicked() = Unit

    private fun startWebSocketIfNeed(newAuthenticatedState: Boolean) {
        val oldAuthenticatedState = state.isAuthenticated
        if (newAuthenticatedState && !oldAuthenticatedState) {
            webSocketRepository.onStartWebsocket()
        }
    }

    override fun onStart() {
        super.onStart()
        ifAuthenticated { webSocketRepository.onStartWebsocket() }
    }

    override fun onDestroy() {
        super.onDestroy()
        ifAuthenticated { webSocketRepository.onStopWebsocket() }
    }

    private fun ifAuthenticated(block: () -> Unit) {
        if (state.isAuthenticated) {
            block.invoke()
        }
    }
}

internal data class MainScreenState(
        val isAuthenticated: Boolean,
)
