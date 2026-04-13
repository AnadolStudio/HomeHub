package com.anadolstudio.template.feature.main

import android.net.Uri
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.anadolstudio.compose.ui.view.snackbar.SnackbarHostState
import com.anadolstudio.template.event.navigateTo
import com.anadolstudio.template.feature.autoSetupHomeAssistantUrl.domain.model.HomeAssistantInstance
import com.anadolstudio.template.feature.autoSetupHomeAssistantUrl.presetnation.AutoSetupHomeAssistantUrlScreen
import com.anadolstudio.template.feature.autoSetupHomeAssistantUrl.presetnation.AutoSetupHomeAssistantUrlViewModel
import com.anadolstudio.template.feature.homeAssistantAuth.presetnation.HomeAssistantAuthScreen
import com.anadolstudio.template.feature.manualSetupHomeAssistantUrl.presetnation.ManualSetupHomeAssistantUrlScreen
import com.anadolstudio.template.feature.splash.SplashScreen
import com.anadolstudio.template.feature.splash.SplashViewModel
import com.anadolstudio.template.navigation.NavGraphContract
import com.anadolstudio.template.navigation.objectToString
import com.anadolstudio.template.navigation.requireObject
import com.anadolstudio.template.navigation.stringArgument

@Suppress("TooManyFunctions", "MemberNameEqualsClassName")
internal object MainGraph : NavGraphContract() {

    private val instanceArgument = stringArgument(name = "instance")

    private fun autoSetupHomeAssistantUrl() = route { "autoSetupHomeAssistantUrl" }

    private fun homeAssistantAuthRoute() = route { "homeAssistantAuth/{${instanceArgument.name}}" }

    private fun homeAssistantAuthRoute(instance: HomeAssistantInstance): String =
            route { "homeAssistantAuth/${Uri.encode(objectToString(instance))}" }

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
                arguments = listOf(instanceArgument),
        ) { entry ->
            val instance = entry.requireObject<HomeAssistantInstance>(instanceArgument)
            HomeAssistantAuthScreen(
                    navigator = navigator,
                    snackbarHostState = snackbarHostState,
                    instance = instance,
            )
        }
        composable(manualSetupHomeAssistantUrl()) {
            ManualSetupHomeAssistantUrlScreen(navigator = navigator, snackbarHostState = snackbarHostState)
        }
    }

    fun SplashViewModel.navigateToAutoSetupHomeAssistantUrl() = navigateFromRoot(autoSetupHomeAssistantUrl())

    fun AutoSetupHomeAssistantUrlViewModel.navigateToHomeAssistantAuth(instance: HomeAssistantInstance) =
            navigateTo(homeAssistantAuthRoute(instance))

    fun AutoSetupHomeAssistantUrlViewModel.navigateToManualSetupHomeAssistantUrl() =
            navigateTo(manualSetupHomeAssistantUrl())
}
