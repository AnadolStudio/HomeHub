package com.anadolstudio.template.feature.main

import android.net.Uri
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.anadolstudio.compose.ui.view.snackbar.SnackbarHostState
import com.anadolstudio.template.event.navigateTo
import com.anadolstudio.template.feature.autoSetupHomeAssistantUrl.domain.model.HomeAssistantInstance
import com.anadolstudio.template.feature.autoSetupHomeAssistantUrl.presetnation.AutoSetupHomeAssistantUrlScreen
import com.anadolstudio.template.feature.autoSetupHomeAssistantUrl.presetnation.AutoSetupHomeAssistantUrlViewModel
import com.anadolstudio.template.feature.home.presentation.HomeScreen
import com.anadolstudio.template.feature.home.presentation.HomeViewModel
import com.anadolstudio.template.feature.homeAssistantAuth.presetnation.HomeAssistantAuthScreen
import com.anadolstudio.template.feature.manualSetupHomeAssistantUrl.presetnation.ManualSetupHomeAssistantUrlScreen
import com.anadolstudio.template.feature.splash.SplashScreen
import com.anadolstudio.template.feature.splash.SplashViewModel
import com.anadolstudio.template.navigation.NavGraphContract
import com.anadolstudio.template.navigation.stringArgument

@Suppress("TooManyFunctions", "MemberNameEqualsClassName")
internal object MainGraph : NavGraphContract() {

    private val urlArgument = stringArgument(name = "url")

    private fun autoSetupHomeAssistantUrl() = route { "autoSetupHomeAssistantUrl" }

    private fun homeAssistantAuthRoute() = route { "homeAssistantAuth/{${urlArgument.name}}" }

    private fun homeAssistantAuthRoute(url: String): String =
            route { "homeAssistantAuth/${Uri.encode(url)}" }

    private fun home() = route { "home" }

    private fun manualSetupHomeAssistantUrl() = route { "manualSetupHomeAssistantUrl" }

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
        composable(
                route = homeAssistantAuthRoute(),
                arguments = listOf(urlArgument),
        ) { entry ->
            val url = requireNotNull(entry.arguments?.getString(urlArgument.name))
            HomeAssistantAuthScreen(
                    navigator = navigator,
                    snackbarHostState = snackbarHostState,
                    url = url,
            )
        }
        composable(home()) {
            HomeScreen(navigator = navigator, snackbarHostState = snackbarHostState)
        }
        composable(manualSetupHomeAssistantUrl()) {
            ManualSetupHomeAssistantUrlScreen(navigator = navigator, snackbarHostState = snackbarHostState)
        }
    }

    fun SplashViewModel.navigateToAutoSetupHomeAssistantUrl() = navigateFromRoot(autoSetupHomeAssistantUrl())

    fun SplashViewModel.navigateToHome() = navigateFromRoot(home())

    fun HomeViewModel.navigateToHome() = navigateFromRoot(home())

    fun AutoSetupHomeAssistantUrlViewModel.navigateToHomeAssistantAuth(instance: HomeAssistantInstance) =
            navigateTo(homeAssistantAuthRoute(instance.internalUrl))

    fun AutoSetupHomeAssistantUrlViewModel.navigateToManualSetupHomeAssistantUrl() =
            navigateTo(manualSetupHomeAssistantUrl())

    fun navigateToHome(navigator: NavigationController) {
        navigator.navigate(home()) {
            popUpTo(0)
        }
    }

    fun navigateToAutoSetupHomeAssistantUrl(navigator: NavigationController) {
        navigator.navigate(autoSetupHomeAssistantUrl()) {
            popUpTo(0)
        }
    }
}
