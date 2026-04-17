package com.anadolstudio.template.feature.homeAssistantAuth.presetnation

import com.anadolstudio.template.base.viewmodel.StatefulViewModel
import com.anadolstudio.template.event.navigateUp
import com.anadolstudio.utils.states.ProgressState
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject

internal class HomeAssistantAuthViewModel @AssistedInject constructor(
        @Assisted private val url: String,
) : StatefulViewModel<HomeAssistantAuthState>(
        HomeAssistantAuthState(url = url),
), HomeAssistantAuthController {

    override fun onAuthCallback(authCode: String) {
        showEvent(
                HomeAssistantAuthEvent.Authenticated(
                        url = url,
                        authCode = authCode,
                        requiredMTLS = state.requiredMTLS,
                )
        )
    }

    override fun onExternalLink(url: String) = showEvent(HomeAssistantAuthEvent.OpenExternalLink(url))

    override fun onPageFinished() = updateState { copy(progressState = ProgressState.Content) }

    override fun onWebViewError(error: HomeAssistantAuthError) = updateState {
        copy(progressState = ProgressState.Error())
    }

    override fun onClientCertRequest() = updateState { copy(requiredMTLS = true) }

    override fun onRetryClicked() {
        updateState { copy(progressState = ProgressState.LoadingFromError, retryCount = retryCount + 1) }
    }

    override fun onBackClicked() = navigateUp()

    @AssistedFactory
    interface Factory {
        fun create(url: String): HomeAssistantAuthViewModel
    }

}
