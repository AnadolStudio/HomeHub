package com.anadolstudio.template.feature.homeAssistantAuth.presetnation

import androidx.lifecycle.viewModelScope
import com.anadolstudio.template.base.viewmodel.StatefulViewModel
import com.anadolstudio.template.event.navigateUp
import com.anadolstudio.template.feature.common.data.PreferencesStorage
import com.anadolstudio.template.feature.homeAssistantAuth.data.CheckInternalUrlUseCase
import com.anadolstudio.template.feature.homeAssistantAuth.domain.NonAuthRepository
import com.anadolstudio.utils.states.ProgressState
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.launch

internal class HomeAssistantAuthViewModel @AssistedInject constructor(
        @Assisted private val url: String,
        private val checkInternalUrlUseCase: CheckInternalUrlUseCase,
        private val nonAuthRepository: NonAuthRepository,
        private val preferencesStorage: PreferencesStorage,
) : StatefulViewModel<HomeAssistantAuthState>(
        HomeAssistantAuthState(url = url),
), HomeAssistantAuthController {

    init {
        viewModelScope.launch {
            val isInternal = checkInternalUrlUseCase.isInternalUrl(url)
            updateState { copy(isInternalUrl = isInternal) }
        }
    }

    override fun onAuthCallback(authCode: String) {
        updateState { copy(progressState = ProgressState.Loading) }
        preferencesStorage.baseUrl = url

        viewModelScope.launch {
            runCatching { nonAuthRepository.exchangeAuthCode(url, authCode) }
                    .onSuccess { tokenResponse ->
                        preferencesStorage.accessToken = tokenResponse.accessToken
                        preferencesStorage.refreshToken = tokenResponse.refreshToken
                        preferencesStorage.tokenType = tokenResponse.tokenType
                        preferencesStorage.accessTokenExpiresIn = tokenResponse.expiresIn

                        
                        
                        showEvent(
                                HomeAssistantAuthEvent.Authenticated(
                                        url = url,
                                        authCode = authCode,
                                        requiredMTLS = state.requiredMTLS,
                                )
                        )
                    }
                    .onFailure { error ->
                        updateState { copy(progressState = ProgressState.Error(error)) }
                    }
        }
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
