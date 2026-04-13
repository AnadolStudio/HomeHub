package com.anadolstudio.compose.ui.theme

import androidx.compose.material.LocalContentColor
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.anadolstudio.compose.ui.R
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
    MaterialTheme(
            typography = materialTypography,
            shapes = Shapes,
    ) {
        CompositionLocalProvider(
                LocalAppColors provides colorPalette,
                LocalAppTypography provides typography,
                LocalTextStyle provides typography.textBook18,
                LocalContentColor provides colorPalette.textPrimary,
                content = content,
        )
    }
}

internal val LocalAppColors = staticCompositionLocalOf<AppThemeColors> {
    error("No LocalAppColors provided")
}

internal val LocalAppTypography = staticCompositionLocalOf { AppTypography.Default }
