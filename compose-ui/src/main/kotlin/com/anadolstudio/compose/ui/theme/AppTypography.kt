package com.anadolstudio.compose.ui.theme

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anadolstudio.compose.ui.R
import com.anadolstudio.compose.ui.theme.AppTypography.Companion.Default
import com.anadolstudio.compose.ui.view.text.Text

private const val FUTURA_MEDIUM_FONT_WEIGHT = 450
private const val FUTURA_LIGHT_FONT_WEIGHT = 300
private const val FUTURA_BOLD_FONT_WEIGHT = 700

internal val FuturaMediumWeight = FontWeight(FUTURA_MEDIUM_FONT_WEIGHT)
internal val FuturaLightWeight = FontWeight(FUTURA_LIGHT_FONT_WEIGHT)
internal val FuturaBoldWeight = FontWeight(FUTURA_BOLD_FONT_WEIGHT)

private val FuturaPTFontFamily = FontFamily(
    Font(resId = R.font.futurapt_book, weight = FontWeight.Normal),
    Font(resId = R.font.futurapt_medium, weight = FuturaMediumWeight),
)

/**
 * Type scale used across the app.
 *
 * `compose-ui` is a shared module reused on multiple projects, therefore the typography is
 * parameterised: pass a custom [defaultStyle] (e.g. with another [FontFamily]) when constructing
 * the instance and provide it through [AppTheme] / [LocalAppTypography].
 *
 * The [Default] instance keeps the legacy Futura-based scale so the existing
 * `AppTypography.textBook18`-style call sites keep working unchanged.
 */
@Suppress("LongParameterList")
class AppTypography(
    val defaultStyle: TextStyle = DEFAULT_STYLE,
    val mediumWeight: FontWeight = FuturaMediumWeight,
    val lightWeight: FontWeight = FuturaLightWeight,
    @Suppress("UNUSED_PARAMETER") val boldWeight: FontWeight = FuturaBoldWeight,
) {

    val titleBook44: TextStyle = defaultStyle.copy(
        fontWeight = FontWeight.Normal,
        fontSize = 44.sp,
        lineHeight = 48.sp,
    )
    val titleBook34: TextStyle = defaultStyle.copy(
        fontWeight = FontWeight.Normal,
        fontSize = 34.sp,
        lineHeight = 40.sp,
    )
    val titleBook28: TextStyle = defaultStyle.copy(
        fontWeight = FontWeight.Normal,
        fontSize = 28.sp,
        lineHeight = 34.sp,
    )
    val titleBook28_32: TextStyle = defaultStyle.copy(
        fontWeight = FontWeight.Normal,
        fontSize = 28.sp,
        lineHeight = 32.sp,
    )
    val textBook23: TextStyle = defaultStyle.copy(
        fontWeight = FontWeight.Normal,
        fontSize = 23.sp,
        lineHeight = 28.sp,
    )
    val textBook24: TextStyle = defaultStyle.copy(
        fontWeight = FontWeight.Normal,
        fontSize = 24.sp,
        lineHeight = 30.sp,
    )
    val textBook22: TextStyle = defaultStyle.copy(
            fontWeight = FontWeight.Normal,
            fontSize = 22.sp,
            lineHeight = 36.sp,
    )
    val textBook14: TextStyle = defaultStyle.copy(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 30.sp,
    )
    val textLight18: TextStyle = defaultStyle.copy(
        fontWeight = lightWeight,
        fontSize = 18.sp,
        lineHeight = 22.sp,
    )
    val textBold18: TextStyle = defaultStyle.copy(
        fontWeight = boldWeight,
        fontSize = 18.sp,
        lineHeight = 22.sp,
    )
    val textMedium18: TextStyle = defaultStyle.copy(
        fontWeight = mediumWeight,
        fontSize = 18.sp,
        lineHeight = 24.sp,
    )
    val textBook18: TextStyle = defaultStyle.copy(
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp,
        lineHeight = 24.sp,
    )
    val textBook18Underlined: TextStyle = defaultStyle.copy(
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        textDecoration = TextDecoration.Underline,
    )
    val captionMedium16: TextStyle = defaultStyle.copy(
        fontWeight = mediumWeight,
        fontSize = 16.sp,
        lineHeight = 20.sp,
    )
    val captionBook16: TextStyle = defaultStyle.copy(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 22.sp,
    )
    val captionMedium14: TextStyle = defaultStyle.copy(
        fontWeight = mediumWeight,
        fontSize = 14.sp,
        lineHeight = 16.sp,
    )
    val captionBook14: TextStyle = defaultStyle.copy(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 16.sp,
    )
    val captionMedium12: TextStyle = defaultStyle.copy(
        fontWeight = mediumWeight,
        fontSize = 12.sp,
        lineHeight = 14.sp,
    )

    /**
     * Backwards-compatible static façade. All `AppTypography.textBook18`-style call sites continue
     * to read styles from the [Default] instance. Application modules that need a custom font
     * should construct their own [AppTypography] and pass it to [AppTheme].
     */
    @Suppress("UndocumentedPublicProperty")
    companion object {

        @Suppress("DEPRECATION")
        val DEFAULT_STYLE: TextStyle = TextStyle(
            fontFamily = FuturaPTFontFamily,
            platformStyle = PlatformTextStyle(
                includeFontPadding = false,
            ),
        )

        /**
         * Convenience factory for projects that just need to swap the [FontFamily] but want to keep
         * the rest of the default style (no font padding, etc.).
         */
        fun fromFontFamily(
            fontFamily: FontFamily,
            mediumWeight: FontWeight = FuturaMediumWeight,
            lightWeight: FontWeight = FuturaLightWeight,
            boldWeight: FontWeight = FuturaBoldWeight,
        ): AppTypography = AppTypography(
            defaultStyle = DEFAULT_STYLE.copy(fontFamily = fontFamily),
            mediumWeight = mediumWeight,
            lightWeight = lightWeight,
            boldWeight = boldWeight,
        )

        val Default: AppTypography = AppTypography()

        val titleBook44: TextStyle get() = Default.titleBook44
        val titleBook34: TextStyle get() = Default.titleBook34
        val titleBook28: TextStyle get() = Default.titleBook28
        val titleBook28_32: TextStyle get() = Default.titleBook28_32
        val textBook23: TextStyle get() = Default.textBook23
        val textBook24: TextStyle get() = Default.textBook24
        val textBook14: TextStyle get() = Default.textBook14
        val textLight18: TextStyle get() = Default.textLight18
        val textBold18: TextStyle get() = Default.textBold18
        val textMedium18: TextStyle get() = Default.textMedium18
        val textBook18: TextStyle get() = Default.textBook18
        val textBook18Underlined: TextStyle get() = Default.textBook18Underlined
        val captionMedium16: TextStyle get() = Default.captionMedium16
        val captionBook16: TextStyle get() = Default.captionBook16
        val captionMedium14: TextStyle get() = Default.captionMedium14
        val captionBook14: TextStyle get() = Default.captionBook14
        val captionMedium12: TextStyle get() = Default.captionMedium12
    }
}

fun materialTypographyOf(typography: AppTypography): Typography = Typography(
    h1 = typography.titleBook44,
    h2 = typography.titleBook34,
    h3 = typography.titleBook28,
    subtitle1 = typography.textBook18,
    body1 = typography.textMedium18,
    body2 = typography.textMedium18,
    button = typography.textMedium18,
    caption = typography.captionBook14,
)

@Deprecated(
    message = "Use materialTypographyOf(AppTypography.Default) or read AppTheme.typography",
    replaceWith = ReplaceWith("materialTypographyOf(AppTypography.Default)"),
)
val MaterialTypography: Typography
    get() = materialTypographyOf(AppTypography.Default)

@Preview(showBackground = true)
@Composable
private fun TypographyPreview() {
    Column(
        modifier = Modifier.padding(8.dp)
    ) {
        Text("TitleBook44", style = AppTypography.titleBook44)
        Text("TitleBook34", style = AppTypography.titleBook34)
        Text("TitleBook28", style = AppTypography.titleBook28)
        Text("TitleBook28_32", style = AppTypography.titleBook28)
        Text("TextBook24", style = AppTypography.textBook24)
        Text("TextMedium18", style = AppTypography.textMedium18)
        Text("TextBook18", style = AppTypography.textBook18)
        Text("CaptionMedium16", style = AppTypography.captionMedium16)
        Text("CaptionBook16", style = AppTypography.captionBook16)
        Text("CaptionMedium14", style = AppTypography.captionMedium14)
        Text("CaptionBook14", style = AppTypography.captionBook14)
        Text("CaptionMedium12", style = AppTypography.captionMedium12)
    }
}
