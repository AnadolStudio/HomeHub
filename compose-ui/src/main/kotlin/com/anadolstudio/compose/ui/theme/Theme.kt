package com.anadolstudio.compose.ui.theme

import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
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
        typography: AppTypography = AppTypography.Default,
        content: @Composable () -> Unit,
) {
    val colorPalette = remember(useDarkTheme) { if (useDarkTheme) AppColorDarkPalette else AppColorLightPalette }
    val materialTypography = remember(typography) { materialTypographyOf(typography) }
    val textSelectionColors = remember(colorPalette) {
        TextSelectionColors(
                handleColor = colorPalette.colorAccent,
                backgroundColor = colorPalette.colorAccent.copy(alpha = 0.4f),
        )
    }
    MaterialTheme(
            typography = materialTypography,
            shapes = Shapes,
    ) {
        CompositionLocalProvider(
                LocalAppColors provides colorPalette,
                LocalAppTypography provides typography,
                LocalTextStyle provides typography.textBook18,
                LocalContentColor provides colorPalette.textPrimary,
                LocalTextSelectionColors provides textSelectionColors,
                content = content,
        )
    }
}

internal val LocalAppColors = staticCompositionLocalOf<AppThemeColors> {
    error("No LocalAppColors provided")
}

internal val LocalAppTypography = staticCompositionLocalOf { AppTypography.Default }
