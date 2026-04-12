package com.anadolstudio.compose.ui.theme

import androidx.compose.material.LocalContentColor
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import com.anadolstudio.compose.ui.theme.color.AppColorDarkPalette
import com.anadolstudio.compose.ui.theme.color.AppColorLightPalette
import com.anadolstudio.compose.ui.theme.color.AppThemeColors

@Composable
fun AppTheme(
    useDarkTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorPalette = remember(useDarkTheme) { if (useDarkTheme) AppColorDarkPalette else AppColorLightPalette }
    MaterialTheme(
        typography = MaterialTypography,
        shapes = Shapes,
    ) {
        CompositionLocalProvider(
            LocalAppColors provides colorPalette,
            LocalTextStyle provides AppTypography.textBook18,
            LocalContentColor provides colorPalette.textPrimary,
            content = content,
        )
    }
}

internal val LocalAppColors = staticCompositionLocalOf<AppThemeColors> {
    error("No LocalAppColors provided")
}
