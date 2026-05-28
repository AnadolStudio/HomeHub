package com.anadolstudio.homehub.feature.registerUser.presentation

import com.anadolstudio.homehub.base.viewmodel.StatefulViewModel
import com.anadolstudio.homehub.feature.common.data.PreferencesStorage
import com.anadolstudio.homehub.feature.main.MainGraph.navigateToHome
import javax.inject.Inject

internal class RegisterUserViewModel @Inject constructor(
        private val preferencesStorage: PreferencesStorage,
) : StatefulViewModel<RegisterUserScreenState>(RegisterUserScreenState()),
        RegisterUserController {

    override fun onContinueClicked() {
        preferencesStorage.isRegisterUserShown = true
        navigateToHome()
    }
}
