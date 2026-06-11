package com.anadolstudio.homehub.feature.common.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import com.anadolstudio.homehub.R

object HomeHubIcons {
    val Back: Painter @Composable get() = painterResource(R.drawable.icon_back)
    val Close: Painter @Composable get() = painterResource(R.drawable.icon_close)
    val VerticalMore: Painter @Composable get() = painterResource(R.drawable.icon_vertical_more)
    val Search: Painter @Composable get() = painterResource(R.drawable.icon_search)
    val Host: Painter @Composable get() = painterResource(R.drawable.ic_host)
    val History: Painter @Composable get() = painterResource(R.drawable.ic_host)
}
