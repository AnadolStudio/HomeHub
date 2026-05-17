package com.anadolstudio.template.feature.main

import android.net.Uri
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.anadolstudio.compose.ui.view.snackbar.SnackbarHostState
import com.anadolstudio.template.event.navigateTo
import com.anadolstudio.template.feature.addDevice.presentation.AddDeviceScreen
import com.anadolstudio.template.feature.autoSetupHomeAssistantUrl.domain.model.HomeAssistantInstance
import com.anadolstudio.template.feature.autoSetupHomeAssistantUrl.presetnation.AutoSetupHomeAssistantUrlScreen
import com.anadolstudio.template.feature.autoSetupHomeAssistantUrl.presetnation.AutoSetupHomeAssistantUrlViewModel
import com.anadolstudio.template.feature.automation.automationDetail.presentation.AutomationDetailScreen
import com.anadolstudio.template.feature.automation.automationList.presentation.AutomationListScreen
import com.anadolstudio.template.feature.automation.automationList.presentation.AutomationListViewModel
import com.anadolstudio.template.feature.automation.sceneDetail.presentation.SceneDetailScreen
import com.anadolstudio.template.feature.deviceDetail.presentation.DeviceDetailScreen
import com.anadolstudio.template.feature.deviceDetail.presentation.DeviceDetailViewModel
import com.anadolstudio.template.feature.history.presentation.HistoryScreen
import com.anadolstudio.template.feature.home.domain.model.HomeAssistantDevice
import com.anadolstudio.template.feature.home.presentation.HomeScreen
import com.anadolstudio.template.feature.home.presentation.HomeViewModel
import com.anadolstudio.template.feature.homeAssistantAuth.presetnation.HomeAssistantAuthScreen
import com.anadolstudio.template.feature.lightDetail.presentation.LightDetailArgs
import com.anadolstudio.template.feature.lightDetail.presentation.LightDetailScreen
import com.anadolstudio.template.feature.manualSetupHomeAssistantUrl.presetnation.ManualSetupHomeAssistantUrlScreen
import com.anadolstudio.template.feature.registerUser.presentation.RegisterUserScreen
import com.anadolstudio.template.feature.registerUser.presentation.RegisterUserViewModel
import com.anadolstudio.template.feature.sceneCreate.presentation.SceneCreateScreen
import com.anadolstudio.template.feature.sceneCreate.presentation.SceneCreateViewModel
import com.anadolstudio.template.feature.sceneCreate.presentation.picker.SceneDevicePickerScreen
import com.anadolstudio.template.feature.sceneCreate.presentation.picker.SceneDevicePickerViewModel
import com.anadolstudio.template.feature.splash.SplashScreen
import com.anadolstudio.template.feature.splash.SplashViewModel
import com.anadolstudio.template.navigation.NavGraphContract
import com.anadolstudio.template.navigation.objectToString
import com.anadolstudio.template.navigation.requireObject
import com.anadolstudio.template.navigation.requireStringArgument
import com.anadolstudio.template.navigation.stringArgument
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.google.accompanist.navigation.material.bottomSheet

@OptIn(ExperimentalMaterialNavigationApi::class)
@Suppress("TooManyFunctions", "MemberNameEqualsClassName")
internal object MainGraph : NavGraphContract() {

    private val urlArgument = stringArgument(name = "url")

    private val deviceIdArgument = stringArgument(name = "deviceId")

    private val lightDetailArgsArgument = stringArgument(name = "lightDetailArgs")

    private val sceneConfigIdArgument = stringArgument(name = "sceneConfigId")

    private fun autoSetupHomeAssistantUrl() = route { "autoSetupHomeAssistantUrl" }

    private fun homeAssistantAuthRoute() = route { "homeAssistantAuth/{${urlArgument.name}}" }

    private fun homeAssistantAuthRoute(url: String): String =
            route { "homeAssistantAuth/${Uri.encode(url)}" }

    private fun home() = route { "home" }

    private fun manualSetupHomeAssistantUrl() = route { "manualSetupHomeAssistantUrl" }

    private fun addDevice() = route { "addDevice" }

    private fun history() = route { "history" }

    private fun automationList() = route { "automationList" }

    private fun automationDetail() = route { "automationDetail" }

    private fun sceneDetail() = route { "sceneDetail" }

    private fun sceneCreate() = route { "sceneCreate" }

    private fun sceneEdit() = route { "sceneEdit/{${sceneConfigIdArgument.name}}" }

    private fun sceneEdit(sceneConfigId: String): String =
            route { "sceneEdit/${Uri.encode(sceneConfigId)}" }

    private fun sceneDevicePicker() = route { "sceneDevicePicker" }

    private fun deviceDetail() = route { "deviceDetail/{${deviceIdArgument.name}}" }

    private fun deviceDetail(deviceId: String): String = route { "deviceDetail/${Uri.encode(deviceId)}" }

    private fun lightDetail() = route { "lightDetail/{${lightDetailArgsArgument.name}}" }

    private fun lightDetail(args: LightDetailArgs): String =
            route { "lightDetail/${Uri.encode(objectToString(args))}" }

    private fun registerUser() = route { "registerUser" }

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
        composable(addDevice()) {
            AddDeviceScreen(navigator = navigator, snackbarHostState = snackbarHostState)
        }
        composable(history()) {
            HistoryScreen(navigator = navigator, snackbarHostState = snackbarHostState)
        }
        composable(automationList()) {
            AutomationListScreen(navigator = navigator, snackbarHostState = snackbarHostState)
        }
        composable(automationDetail()) {
            AutomationDetailScreen(navigator = navigator, snackbarHostState = snackbarHostState)
        }
        composable(sceneDetail()) {
            SceneDetailScreen(navigator = navigator, snackbarHostState = snackbarHostState)
        }
        composable(sceneCreate()) {
            SceneCreateScreen(navigator = navigator, snackbarHostState = snackbarHostState)
        }
        composable(
                route = sceneEdit(),
                arguments = listOf(sceneConfigIdArgument),
        ) { entry ->
            val sceneConfigId = entry.requireStringArgument(sceneConfigIdArgument)
            SceneCreateScreen(
                    navigator = navigator,
                    snackbarHostState = snackbarHostState,
                    editSceneConfigId = sceneConfigId,
            )
        }
        composable(sceneDevicePicker()) {
            SceneDevicePickerScreen(navigator = navigator, snackbarHostState = snackbarHostState)
        }
        bottomSheet(
                route = deviceDetail(),
                arguments = listOf(deviceIdArgument),
        ) { entry ->
            // Workaround for accompanist navigation-material bug: при переходе между bot-sheet'ами
            // sheetContent может пересоставиться с уже-DESTROYED NavBackStackEntry,
            // и viewModel(...) падает с IllegalStateException.
            if (entry.lifecycle.currentState == Lifecycle.State.DESTROYED) return@bottomSheet
            val deviceId = entry.requireStringArgument(deviceIdArgument)
            DeviceDetailScreen(
                    navigator = navigator,
                    snackbarHostState = snackbarHostState,
                    deviceId = deviceId,
            )
        }
        bottomSheet(
                route = lightDetail(),
                arguments = listOf(lightDetailArgsArgument),
        ) { entry ->
            if (entry.lifecycle.currentState == Lifecycle.State.DESTROYED) return@bottomSheet
            val args = entry.requireObject<LightDetailArgs>(lightDetailArgsArgument)
            LightDetailScreen(
                    navigator = navigator,
                    snackbarHostState = snackbarHostState,
                    args = args,
            )
        }
        composable(registerUser()) {
            RegisterUserScreen(navigator = navigator, snackbarHostState = snackbarHostState)
        }
    }

    fun SplashViewModel.navigateToAutoSetupHomeAssistantUrl() = navigateFromRoot(autoSetupHomeAssistantUrl())

    fun SplashViewModel.navigateToHome() = navigateFromRoot(home())

    fun HomeViewModel.navigateToHome() = navigateFromRoot(home())

    fun AutoSetupHomeAssistantUrlViewModel.navigateToHomeAssistantAuth(instance: HomeAssistantInstance) =
            navigateTo(homeAssistantAuthRoute(instance.internalUrl))

    fun AutoSetupHomeAssistantUrlViewModel.navigateToManualSetupHomeAssistantUrl() =
            navigateTo(manualSetupHomeAssistantUrl())

    fun HomeViewModel.navigateToAddDevice() = navigateTo(addDevice())

    fun HomeViewModel.navigateToHistory() = navigateTo(history())

    fun HomeViewModel.navigateToAutomationList() = navigateTo(automationList())

    fun AutomationListViewModel.navigateToAutomationDetail() = navigateTo(automationDetail())

    fun AutomationListViewModel.navigateToSceneDetail() = navigateTo(sceneDetail())

    fun AutomationListViewModel.navigateToSceneCreate() = navigateTo(sceneCreate())

    fun AutomationListViewModel.navigateToSceneEdit(sceneConfigId: String) =
            navigateTo(sceneEdit(sceneConfigId))

    fun SceneCreateViewModel.navigateToSceneDevicePicker() = navigateTo(sceneDevicePicker())

    /** Изменение уже добавленного устройства: открываем DeviceDetail напрямую из SceneCreate. */
    fun SceneCreateViewModel.navigateToDeviceDetailFromSceneCreate(deviceId: String) =
            navigateTo(deviceDetail(deviceId))

    /**
     * Из picker'а попадаем в DeviceDetail с popUpTo picker (inclusive): после закрытия
     * DeviceDetail юзер возвращается на SceneCreate, а не на picker.
     */
    fun SceneDevicePickerViewModel.navigateToDeviceDetailFromPicker(deviceId: String) =
            navigateTo(deviceDetail(deviceId)) {
                popUpTo(sceneDevicePicker()) { inclusive = true }
            }

    fun DeviceDetailViewModel.navigateToLightDetail(args: LightDetailArgs) =
            navigateTo(lightDetail(args))

    fun HomeViewModel.navigateToDeviceDetail(device: HomeAssistantDevice) =
            navigateTo(deviceDetail(device.id))

    fun RegisterUserViewModel.navigateToHome() = navigateFromRoot(home())

    fun SplashViewModel.navigateToRegisterUser() = navigateFromRoot(registerUser())

    fun navigateToHome(navigator: NavigationController) {
        navigator.navigate(home()) {
            popUpTo(0)
        }
    }

    fun navigateToRegisterUser(navigator: NavigationController) {
        navigator.navigate(registerUser()) {
            popUpTo(0)
        }
    }

    fun navigateToAutoSetupHomeAssistantUrl(navigator: NavigationController) {
        navigator.navigate(autoSetupHomeAssistantUrl()) {
            popUpTo(0)
        }
    }
}
