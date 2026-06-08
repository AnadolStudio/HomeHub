package com.anadolstudio.homehub.feature.main

import android.net.Uri
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.anadolstudio.compose.ui.view.snackbar.SnackbarHostState
import com.anadolstudio.homehub.event.navigateTo
import com.anadolstudio.homehub.event.navigateUp
import com.anadolstudio.homehub.feature.add.presentation.AddScreen
import com.anadolstudio.homehub.feature.add.presentation.AddViewModel
import com.anadolstudio.homehub.feature.addDeviceGroup.presentation.AddDeviceGroupScreen
import com.anadolstudio.homehub.feature.addPerson.presentation.AddPersonScreen
import com.anadolstudio.homehub.feature.add_device.addMatter.presentation.AddMatterScreen
import com.anadolstudio.homehub.feature.add_device.addZigbee.presentation.AddZigbeeScreen
import com.anadolstudio.homehub.feature.add_device.common.BaseAddDeviceViewModel
import com.anadolstudio.homehub.feature.autoSetupHomeAssistantUrl.domain.model.HomeAssistantInstance
import com.anadolstudio.homehub.feature.autoSetupHomeAssistantUrl.presetnation.AutoSetupHomeAssistantUrlScreen
import com.anadolstudio.homehub.feature.autoSetupHomeAssistantUrl.presetnation.AutoSetupHomeAssistantUrlViewModel
import com.anadolstudio.homehub.feature.automation.automationDetail.presentation.AutomationDetailScreen
import com.anadolstudio.homehub.feature.automation.automationDetail.presentation.AutomationDetailViewModel
import com.anadolstudio.homehub.feature.automation.automationList.presentation.AutomationListScreen
import com.anadolstudio.homehub.feature.automation.automationList.presentation.AutomationListViewModel
import com.anadolstudio.homehub.feature.automation.automationMode.presentation.AutomationModePickerScreen
import com.anadolstudio.homehub.feature.automation.common.presentation.AutomationMode
import com.anadolstudio.homehub.feature.deviceDetail.demo.DemoDeviceDetailScreen
import com.anadolstudio.homehub.feature.deviceDetail.ordinary.presentation.DeviceDetailScreen
import com.anadolstudio.homehub.feature.history.presentation.HistoryScreen
import com.anadolstudio.homehub.feature.home.domain.model.HomeAssistantDevice
import com.anadolstudio.homehub.feature.home.presentation.HomeScreen
import com.anadolstudio.homehub.feature.home.presentation.HomeViewModel
import com.anadolstudio.homehub.feature.homeAssistantAuth.presetnation.HomeAssistantAuthScreen
import com.anadolstudio.homehub.feature.manualSetupHomeAssistantUrl.presetnation.ManualSetupHomeAssistantUrlScreen
import com.anadolstudio.homehub.feature.registerUser.presentation.RegisterUserScreen
import com.anadolstudio.homehub.feature.registerUser.presentation.RegisterUserViewModel
import com.anadolstudio.homehub.feature.sceneCreate.presentation.SCENE_DEVICE_SNAPSHOT_KEY
import com.anadolstudio.homehub.feature.sceneCreate.presentation.SceneCreateScreen
import com.anadolstudio.homehub.feature.sceneCreate.presentation.SceneCreateViewModel
import com.anadolstudio.homehub.feature.sceneCreate.presentation.picker.SceneDevicePickerScreen
import com.anadolstudio.homehub.feature.sceneCreate.presentation.picker.SceneDevicePickerViewModel
import com.anadolstudio.homehub.feature.splash.SplashScreen
import com.anadolstudio.homehub.feature.splash.SplashViewModel
import com.anadolstudio.homehub.navigation.NavGraphContract
import com.anadolstudio.homehub.navigation.enumArgument
import com.anadolstudio.homehub.navigation.objectToString
import com.anadolstudio.homehub.navigation.requireEnumArgument
import com.anadolstudio.homehub.navigation.requireObject
import com.anadolstudio.homehub.navigation.requireStringArgument
import com.anadolstudio.homehub.navigation.stringArgument
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.google.accompanist.navigation.material.bottomSheet

@OptIn(ExperimentalMaterialNavigationApi::class)
@Suppress("TooManyFunctions", "MemberNameEqualsClassName")
internal object MainGraph : NavGraphContract() {

    private val urlArgument = stringArgument(name = "url")

    private val deviceArgument = stringArgument(name = "device")

    private val selectedEntitySetArgument = stringArgument(name = "selectedEntitySet")

    private val lightDetailArgsArgument = stringArgument(name = "lightDetailArgs")

    private val sceneConfigIdArgument = stringArgument(name = "sceneConfigId")

    private val automationModeArgument = enumArgument<AutomationMode>(name = "mode")

    private val excludedDeviceIdsArgument = stringArgument(name = "excludedDeviceIds")

    private fun autoSetupHomeAssistantUrl() = route { "autoSetupHomeAssistantUrl" }

    private fun homeAssistantAuthRoute() = route { "homeAssistantAuth/{${urlArgument.name}}" }

    private fun homeAssistantAuthRoute(url: String): String =
            route { "homeAssistantAuth/${Uri.encode(url)}" }

    private fun home() = route { "home" }

    private fun manualSetupHomeAssistantUrl() = route { "manualSetupHomeAssistantUrl" }

    private fun add() = route { "add" }

    private fun addZigbee() = route { "addZigbee" }

    private fun addMatter() = route { "addMatter" }

    private fun addPerson() = route { "addPerson" }

    private fun addDeviceGroup() = route { "addDeviceGroup" }

    private fun history() = route { "history" }

    private fun automationList() = route { "automationList" }

    private fun automationDetail() = route { "automationDetail" }

    private fun automationModePicker() = route { "automationModePicker/{${automationModeArgument.name}}" }

    private fun automationModePicker(mode: AutomationMode): String =
            route { "automationModePicker/${mode.name}" }

    private fun sceneDetail() = route { "sceneDetail" }

    private fun sceneCreate() = route { "sceneCreate" }

    private fun sceneEdit() = route { "sceneEdit/{${sceneConfigIdArgument.name}}" }

    private fun sceneEdit(sceneConfigId: String): String =
            route { "sceneEdit/${Uri.encode(sceneConfigId)}" }

    private fun sceneDevicePicker() = route { "sceneDevicePicker/{${excludedDeviceIdsArgument.name}}" }

    private fun sceneDevicePicker(excludedDeviceIds: Set<String>): String =
            route { "sceneDevicePicker/${Uri.encode(objectToString(excludedDeviceIds))}" }

    private fun deviceDetail() = route { "deviceDetail/{${deviceArgument.name}}" }

    private fun deviceDetail(device: HomeAssistantDevice): String =
            route { "deviceDetail/${Uri.encode(objectToString(device))}" }

    private fun demoDeviceDetail() = route {
        "demoDeviceDetail/{${deviceArgument.name}}/{${selectedEntitySetArgument.name}}"
    }

    private fun demoDeviceDetail(device: HomeAssistantDevice, selectedEntitySet: Set<String>): String =
            route {
                "demoDeviceDetail/" +
                        "${Uri.encode(objectToString(device))}/" +
                        Uri.encode(objectToString(selectedEntitySet))
            }

    private fun lightDetail() = route { "lightDetail/{${lightDetailArgsArgument.name}}" }

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
        bottomSheet(add()) { entry ->
            if (entry.lifecycle.currentState == Lifecycle.State.DESTROYED) return@bottomSheet
            AddScreen(navigator = navigator, snackbarHostState = snackbarHostState)
        }
        composable(addZigbee()) {
            AddZigbeeScreen(navigator = navigator, snackbarHostState = snackbarHostState)
        }
        composable(addMatter()) {
            AddMatterScreen(navigator = navigator, snackbarHostState = snackbarHostState)
        }
        composable(addPerson()) {
            AddPersonScreen(navigator = navigator, snackbarHostState = snackbarHostState)
        }
        composable(addDeviceGroup()) {
            AddDeviceGroupScreen(navigator = navigator, snackbarHostState = snackbarHostState)
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
        bottomSheet(
                route = automationModePicker(),
                arguments = listOf(automationModeArgument),
        ) { entry ->
            if (entry.lifecycle.currentState == Lifecycle.State.DESTROYED) return@bottomSheet
            val mode = entry.requireEnumArgument<AutomationMode>(automationModeArgument)
            AutomationModePickerScreen(
                    navigator = navigator,
                    snackbarHostState = snackbarHostState,
                    currentMode = mode,
            )
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
        composable(
                route = sceneDevicePicker(),
                arguments = listOf(excludedDeviceIdsArgument),
        ) { entry ->
            val excludedDeviceIds = entry.requireObject<Set<String>>(excludedDeviceIdsArgument)
            SceneDevicePickerScreen(
                    navigator = navigator,
                    snackbarHostState = snackbarHostState,
                    excludedDeviceIds = excludedDeviceIds,
            )
        }
        bottomSheet(
                route = deviceDetail(),
                arguments = listOf(deviceArgument),
        ) { entry ->
            // Workaround for accompanist navigation-material bug: при переходе между bot-sheet'ами
            // sheetContent может пересоставиться с уже-DESTROYED NavBackStackEntry,
            // и viewModel(...) падает с IllegalStateException.
            if (entry.lifecycle.currentState == Lifecycle.State.DESTROYED) return@bottomSheet
            val device = entry.requireObject<HomeAssistantDevice>(deviceArgument)
            DeviceDetailScreen(
                    navigator = navigator,
                    snackbarHostState = snackbarHostState,
                    device = device,
            )
        }
        bottomSheet(
                route = demoDeviceDetail(),
                arguments = listOf(deviceArgument, selectedEntitySetArgument),
        ) { entry ->
            if (entry.lifecycle.currentState == Lifecycle.State.DESTROYED) return@bottomSheet
            val device = entry.requireObject<HomeAssistantDevice>(deviceArgument)
            val selectedEntitySet = entry.requireObject<Set<String>>(selectedEntitySetArgument)
            DemoDeviceDetailScreen(
                    navigator = navigator,
                    snackbarHostState = snackbarHostState,
                    device = device,
                    selectedEntitySet = selectedEntitySet,
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

    fun HomeViewModel.navigateToAdd() = navigateTo(add())

    fun AddViewModel.navigateToAddZigbee() {
        navigateUp()
        navigateTo(addZigbee())
    }

    fun AddViewModel.navigateToAddMatter() {
        navigateUp()
        navigateTo(addMatter())
    }

    fun AddViewModel.navigateToAddPerson() {
        navigateUp()
        navigateTo(addPerson())
    }

    fun AddViewModel.navigateToAddDeviceGroup() {
        navigateUp()
        navigateTo(addDeviceGroup())
    }

    fun HomeViewModel.navigateToHistory() = navigateTo(history())

    fun HomeViewModel.navigateToAutomationList() = navigateTo(automationList())

    fun AutomationListViewModel.navigateToAutomationDetail() = navigateTo(automationDetail())

    fun AutomationDetailViewModel.navigateToAutomationModePicker(mode: AutomationMode) =
            navigateTo(automationModePicker(mode))

    fun AutomationListViewModel.navigateToSceneDetail() = navigateTo(sceneDetail())

    fun AutomationListViewModel.navigateToSceneCreate() = navigateTo(sceneCreate())

    fun AutomationListViewModel.navigateToSceneEdit(sceneConfigId: String) =
            navigateTo(sceneEdit(sceneConfigId))

    fun SceneCreateViewModel.navigateToSceneDevicePicker(excludedDeviceIds: Set<String>) =
            navigateTo(sceneDevicePicker(excludedDeviceIds))

    fun SceneCreateViewModel.navigateToDemoDeviceDetailFromSceneCreate(
            device: HomeAssistantDevice,
            selectedEntitySet: Set<String>,
    ) = navigateTo(demoDeviceDetail(device, selectedEntitySet))

    fun SceneDevicePickerViewModel.navigateToDemoDeviceDetailFromPicker(
            device: HomeAssistantDevice,
            selectedEntitySet: Set<String>,
    ) {
        navigateUp(SCENE_DEVICE_SNAPSHOT_KEY to device)
        navigateTo(demoDeviceDetail(device, selectedEntitySet))
    }

    fun HomeViewModel.navigateToDeviceDetail(device: HomeAssistantDevice) =
            navigateTo(deviceDetail(device))

    fun BaseAddDeviceViewModel<*>.navigateToDeviceDetail(device: HomeAssistantDevice) =
            navigateTo(deviceDetail(device))

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

