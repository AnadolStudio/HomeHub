package com.anadolstudio.template.feature.main

import androidx.lifecycle.viewModelScope
import com.anadolstudio.template.base.viewmodel.StatefulViewModel
import com.anadolstudio.template.core.websocket.WebSocketCore
import com.anadolstudio.template.feature.common.data.PreferencesStorage
import com.anadolstudio.utils.states.lce.mapToLce
import com.anadolstudio.utils.states.lce.onEachContent
import javax.inject.Inject
import kotlinx.coroutines.flow.launchIn

internal class MainViewModel @Inject constructor(
        private val preferencesStorage: PreferencesStorage,
        private val webSocketCore: WebSocketCore,
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
            webSocketCore.resume()
        }
    }

    override fun onStart() {
        super.onStart()
        ifAuthenticated { webSocketCore.resume() }
    }

    override fun onStop() {
        super.onStop()
        ifAuthenticated { webSocketCore.pause() }
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
