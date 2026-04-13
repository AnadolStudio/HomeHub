package com.anadolstudio.template.feature.main

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.anadolstudio.compose.ui.view.snackbar.SnackbarHostState
import com.anadolstudio.template.feature.autoSetupHomeAssistantUrl.presetnation.AutoSetupHomeAssistantUrlScreen
import com.anadolstudio.template.feature.splash.SplashScreen
import com.anadolstudio.template.feature.splash.SplashViewModel
import com.anadolstudio.template.navigation.NavGraphContract

@Suppress("TooManyFunctions", "MemberNameEqualsClassName")
internal object MainGraph : NavGraphContract() {

    private fun autoSetupHomeAssistantUrl() = route { "autoSetupHomeAssistantUrl" }

    fun NavGraphBuilder.mainGraph(
            route: String,
            navigator: NavigationController,
            snackbarHostState: SnackbarHostState,
    ) = navigation(route = route) {
        composable(startDestination) {
            SplashScreen(navigator = navigator, snackbarHostState = snackbarHostState)
        }
        composable(autoSetupHomeAssistantUrl()) {
            AutoSetupHomeAssistantUrlScreen(navigator = navigator, snackbarHostState = snackbarHostState)
        }
    }

    fun SplashViewModel.navigateToAutoSetupHomeAssistantUrl() = navigateFromRoot(autoSetupHomeAssistantUrl())

}
