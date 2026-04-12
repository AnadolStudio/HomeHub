package com.anadolstudio.template.feature.splash

import androidx.compose.runtime.Composable
import com.anadolstudio.compose.ui.view.snackbar.SnackbarHostState
import com.anadolstudio.template.di.viewmodel.daggerViewModel
import com.anadolstudio.template.event.ObserveEvents
import com.anadolstudio.template.feature.main.NavigationController

@Composable
internal fun SplashScreen(
        navigator: NavigationController,
        snackbarHostState: SnackbarHostState,
        viewModel: SplashViewModel = daggerViewModel(),
) {
    ObserveEvents(viewModel.events, snackbarHostState, navigator)
}
