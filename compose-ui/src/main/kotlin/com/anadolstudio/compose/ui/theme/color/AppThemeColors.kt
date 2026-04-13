package com.anadolstudio.compose.ui.theme.color

import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color

@Stable
data class AppThemeColors(
    val isLight: Boolean,
    val colorPrimary: Color,
    val colorSecondary: Color,
    val colorAccent: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val buttonPrimaryRipple: Color,
    val divider: Color,
    val disable: Color,
    val template: Color = AppColor.template,
    val colorOverlay: Color = AppColor.colorOverlay,
    val shimmerGradient: GradientColor = GradientColor(
        colorStart = AppColor.shimmersStart,
        colorCenter = AppColor.shimmersCenter,
        colorEnd = AppColor.shimmersEnd,
    ),
)

data class GradientColor(
    val colorStart: Color,
    val colorCenter: Color,
    val colorEnd: Color,
)
