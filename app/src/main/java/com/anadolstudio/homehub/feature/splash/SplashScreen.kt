package com.anadolstudio.homehub.feature.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.anadolstudio.compose.ui.theme.AppTheme
import com.anadolstudio.compose.ui.view.snackbar.SnackbarHostState
import com.anadolstudio.homehub.R
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

    Box(
            modifier = Modifier
                    .fillMaxSize()
                    .background(AppTheme.colors.colorPrimary),
            contentAlignment = Alignment.Center,
    ) {
        Icon(
                painter = painterResource(R.drawable.ic_brand_logo),
                contentDescription = null,
                modifier = Modifier.size(120.dp),
                tint = AppTheme.colors.colorAccent,
        )
    }
}
