package com.anadolstudio.homehub.feature.splash

import androidx.compose.runtime.Composable
import com.anadolstudio.compose.ui.view.snackbar.SnackbarHostState
import com.anadolstudio.homehub.di.viewmodel.daggerViewModel
import com.anadolstudio.homehub.event.ObserveEvents
import com.anadolstudio.homehub.feature.main.NavigationController

@Composable
internal fun SplashScreen(
        navigator: NavigationController,
        snackbarHostState: SnackbarHostState,
        viewModel: SplashViewModel = daggerViewModel(),
) {
    ObserveEvents(viewModel.events, snackbarHostState, navigator)
}
