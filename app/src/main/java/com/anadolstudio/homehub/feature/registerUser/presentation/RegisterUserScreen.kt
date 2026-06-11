package com.anadolstudio.homehub.feature.registerUser.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.anadolstudio.compose.ui.theme.AppTheme
import com.anadolstudio.compose.ui.theme.Dimmens
import com.anadolstudio.compose.ui.view.snackbar.SnackbarHostState
import com.anadolstudio.homehub.di.viewmodel.daggerViewModel
import com.anadolstudio.homehub.event.ObserveEvents
import com.anadolstudio.homehub.feature.main.NavigationController

@Composable
internal fun RegisterUserScreen(
        navigator: NavigationController,
        snackbarHostState: SnackbarHostState,
        viewModel: RegisterUserViewModel = daggerViewModel(),
) {
    val state by viewModel.stateFlow.collectAsState()
    ObserveEvents(viewModel.events, snackbarHostState, navigator)

    RegisterUserLayout(state = state, controller = viewModel)
}

@Composable
private fun RegisterUserLayout(
        @Suppress("UNUSED_PARAMETER") state: RegisterUserScreenState,
        controller: RegisterUserController,
) {
    Column(
            modifier = Modifier
                    .fillMaxSize()
                    .background(AppTheme.colors.colorSecondary)
                    .systemBarsPadding()
                    .padding(Dimmens.mainMargin),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
    ) {
        Text(
                text = "Регистрация пользователя",
                style = AppTheme.typography.textBook18,
                fontWeight = FontWeight.Bold,
                color = AppTheme.colors.colorAccent,
        )

        Text(
                text = "Заглушка экрана RegisterUser. Показывается один раз после авторизации.",
                style = AppTheme.typography.textBook14,
                color = AppTheme.colors.textSecondary,
                modifier = Modifier.padding(top = Dimmens.mediumMargin),
        )

        Button(
                onClick = { controller.onContinueClicked() },
                modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = Dimmens.largeMargin),
        ) {
            Text(text = "Продолжить")
        }
    }
}
