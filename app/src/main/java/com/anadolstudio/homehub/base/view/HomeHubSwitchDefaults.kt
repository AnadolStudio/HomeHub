package com.anadolstudio.homehub.base.view

import androidx.compose.material3.SwitchColors
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import com.anadolstudio.compose.ui.theme.AppTheme

val homeHubSwitchDefaults: SwitchColors
    @Composable get() = SwitchDefaults.colors(
            checkedThumbColor = AppTheme.colors.colorPrimary,
            checkedTrackColor = AppTheme.colors.colorAccent,
            checkedBorderColor = AppTheme.colors.colorAccent,
            uncheckedThumbColor = AppTheme.colors.colorPrimary,
            uncheckedTrackColor = AppTheme.colors.disable,
            uncheckedBorderColor = AppTheme.colors.disable,
    )
