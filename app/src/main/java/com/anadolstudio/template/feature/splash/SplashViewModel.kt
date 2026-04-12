package com.anadolstudio.template.feature.splash

import androidx.lifecycle.viewModelScope
import com.anadolstudio.template.base.viewmodel.BaseViewModel
import com.anadolstudio.template.feature.main.MainGraph.navigateToHome
import kotlinx.coroutines.launch
import javax.inject.Inject

internal class SplashViewModel @Inject constructor(
) : BaseViewModel() {

    init {
        navigateToStartDestination()
    }

    private fun navigateToStartDestination() {
        viewModelScope.launch {
            navigateToHome()
        }
    }
}
