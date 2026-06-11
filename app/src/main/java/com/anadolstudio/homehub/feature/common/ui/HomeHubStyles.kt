package com.anadolstudio.homehub.feature.common.ui

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import com.anadolstudio.homehub.R

internal val HomeHubFontFamily = FontFamily(
        Font(resId = R.font.roboto_variable_font, weight = FontWeight.Normal),
        Font(resId = R.font.roboto_italic_variable_font, weight = FontWeight.Normal, style = FontStyle.Italic),
)
