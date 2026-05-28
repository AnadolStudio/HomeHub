package com.anadolstudio.homehub.feature.add.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Devices
import androidx.compose.material.icons.outlined.Hub
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.SettingsRemote
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.anadolstudio.compose.ui.theme.AppTheme
import com.anadolstudio.compose.ui.theme.Dimmens
import com.anadolstudio.compose.ui.view.button.OutlineButtonLarge
import com.anadolstudio.compose.ui.view.snackbar.SnackbarHostState
import com.anadolstudio.homehub.R
import com.anadolstudio.homehub.di.viewmodel.daggerViewModel
import com.anadolstudio.homehub.event.ObserveEvents
import com.anadolstudio.homehub.feature.addMatter.gms.isMatterCommissioningSupported
import com.anadolstudio.homehub.feature.main.NavigationController

@Composable
internal fun AddScreen(
        navigator: NavigationController,
        snackbarHostState: SnackbarHostState,
        viewModel: AddViewModel = daggerViewModel(),
) {
    val state by viewModel.stateFlow.collectAsState()
    ObserveEvents(viewModel.events, snackbarHostState, navigator)

    AddLayout(state = state, controller = viewModel)
}

@Composable
private fun AddLayout(
        @Suppress("UNUSED_PARAMETER") state: AddScreenState,
        controller: AddController,
) {
    Column(
            modifier = Modifier
                    .fillMaxWidth()
                    .background(
                            color = AppTheme.colors.colorSecondary,
                            shape = RoundedCornerShape(topStart = Dimmens.mainMargin, topEnd = Dimmens.mainMargin),
                    )
                    .padding(horizontal = Dimmens.mainMargin)
                    .padding(top = 24.dp, bottom = 16.dp)
                    .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
                text = stringResource(R.string.add_title),
                style = AppTheme.typography.textMedium18,
                color = AppTheme.colors.colorAccent,
        )

        OutlineButtonLarge(
                text = stringResource(R.string.add_button_zigbee),
                onClick = controller::onZigbeeClicked,
                icon = rememberVectorPainter(Icons.Outlined.SettingsRemote),
        )

        // Google Home Mobile SDK требует API 27+: на Android 8.0 кнопка просто не показывается.
        if (isMatterCommissioningSupported()) {
            OutlineButtonLarge(
                    text = stringResource(R.string.add_button_matter),
                    onClick = controller::onMatterClicked,
                    icon = rememberVectorPainter(Icons.Outlined.Hub),
            )
        }

        OutlineButtonLarge(
                text = stringResource(R.string.add_button_person),
                onClick = controller::onPersonClicked,
                icon = rememberVectorPainter(Icons.Outlined.Person),
        )

        OutlineButtonLarge(
                text = stringResource(R.string.add_button_device_group),
                onClick = controller::onDeviceGroupClicked,
                icon = rememberVectorPainter(Icons.Outlined.Devices),
        )
    }
}
