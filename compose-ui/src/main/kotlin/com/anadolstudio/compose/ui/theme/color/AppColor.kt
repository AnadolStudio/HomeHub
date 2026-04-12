@file:Suppress("MagicNumber")

package com.anadolstudio.compose.ui.theme.color

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anadolstudio.compose.ui.view.text.Text

internal object AppColor {
    internal val white: Color = Color(0xFFFFFFFF)
    internal val black: Color = Color(0xFF000000)
    internal val gray: Color = Color(0xFF7E7E7E)
    internal val template: Color = Color(0xFFEE00FF)

    internal val colorPrimaryLight: Color = Color(0xFFFAFAFA)
    internal val colorSecondaryLight: Color = Color(0xFFEBEBEB)
    internal val colorAccentLight: Color = Color(0xFF1B1B1B)

    internal val colorPrimaryDark: Color = Color(0xFF424242)
    internal val colorSecondaryDark: Color = Color(0xFF616161)
    internal val colorAccentDark: Color = Color(0xFFFAFAFA)

    internal val colorOverlay: Color = Color(0x801B1B1B)

    internal val shimmersStart: Color = Color(0xFFF2F2F2)
    internal val shimmersCenter: Color = Color(0xFFF8F8F8)
    internal val shimmersEnd: Color = Color(0xFFEBEBEB)
}

@Preview
@Composable
@Suppress("LongMethod", "StringLiteralDuplication")
private fun PalettePreview() = Column {
    Row {
        ColorPreview(AppColor.white, name = "Black")
        ColorPreview(AppColor.black, name = "White")
        ColorPreview(AppColor.gray, name = "Gray")
        ColorPreview(AppColor.template, name = "Template")
    }

    Header("Shimmers")
    Row {
        ColorPreview(AppColor.shimmersStart, name = "Start")
        ColorPreview(AppColor.shimmersCenter, name = "Center")
        ColorPreview(AppColor.shimmersEnd, name = "End")
    }

    Header("Light")
    Row {
        ColorPreview(AppColor.colorPrimaryLight, name = "Primary")
        ColorPreview(AppColor.colorSecondaryLight, name = "Secondary")
        ColorPreview(AppColor.colorAccentLight, name = "Accent")
    }

    Header("Dark")
    Row {
        ColorPreview(AppColor.colorPrimaryDark, name = "Primary")
        ColorPreview(AppColor.colorSecondaryDark, name = "Secondary")
        ColorPreview(AppColor.colorAccentDark, name = "Accent")
    }
}

@Composable
private fun Header(text: String) {
    Text(
        modifier = Modifier.padding(top = 16.dp, bottom = 4.dp),
        text = text,
        color = AppColor.white
    )
}

@Composable
private fun ColorPreview(color: Color, name: String) {
    Box(
        modifier = Modifier
            .size(60.dp)
            .background(color)
            .padding(4.dp)
    ) {
        val textColor = if (color.luminance() < 0.5) Color.White else Color.Black
        Text(name, fontSize = 10.sp, color = textColor)
    }
}
