package com.anadolstudio.template.feature.registerUser.presentation

import com.anadolstudio.template.base.viewmodel.StatefulViewModel
import com.anadolstudio.template.feature.common.data.PreferencesStorage
import com.anadolstudio.template.feature.main.MainGraph.navigateToHome
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
