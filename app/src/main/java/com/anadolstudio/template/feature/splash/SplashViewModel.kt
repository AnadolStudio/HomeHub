package com.anadolstudio.template.feature.splash

import androidx.lifecycle.viewModelScope
import com.anadolstudio.template.base.viewmodel.BaseViewModel
import com.anadolstudio.template.feature.common.data.PreferencesStorage
import com.anadolstudio.template.feature.main.MainGraph.navigateToAutoSetupHomeAssistantUrl
import com.anadolstudio.template.feature.main.MainGraph.navigateToHome
import com.anadolstudio.template.feature.main.MainGraph.navigateToRegisterUser
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
