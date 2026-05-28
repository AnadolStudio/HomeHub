package com.anadolstudio.homehub.feature.splash

import androidx.lifecycle.viewModelScope
import com.anadolstudio.homehub.base.viewmodel.BaseViewModel
import com.anadolstudio.homehub.feature.common.data.PreferencesStorage
import com.anadolstudio.homehub.feature.main.MainGraph.navigateToAutoSetupHomeAssistantUrl
import com.anadolstudio.homehub.feature.main.MainGraph.navigateToHome
import com.anadolstudio.homehub.feature.main.MainGraph.navigateToRegisterUser
import javax.inject.Inject
import kotlinx.coroutines.launch

internal class SplashViewModel @Inject constructor(
        private val preferencesStorage: PreferencesStorage,
) : BaseViewModel() {

    init {
        navigateToStartDestination()
    }

    private fun navigateToStartDestination() {
        viewModelScope.launch {
            val isAuthenticated = preferencesStorage.accessToken != null
            val isRegisterUserShown = preferencesStorage.isRegisterUserShown
            when {
                isAuthenticated && isRegisterUserShown -> navigateToHome()
                isAuthenticated -> navigateToRegisterUser()
                else -> navigateToAutoSetupHomeAssistantUrl()
            }
        }
    }
}
