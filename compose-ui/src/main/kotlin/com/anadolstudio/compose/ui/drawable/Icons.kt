package com.anadolstudio.compose.ui.drawable

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import com.anadolstudio.compose.ui.R.drawable.icon_back
import com.anadolstudio.compose.ui.R.drawable.icon_close
import com.anadolstudio.compose.ui.R.drawable.icon_search
import com.anadolstudio.compose.ui.R.drawable.icon_vertical_more

object Icons {
    val Back: Painter @Composable get() = painterResource(icon_back)
    val Close: Painter @Composable get() = painterResource(icon_close)
    val VerticalMore: Painter @Composable get() = painterResource(icon_vertical_more)
    val Search: Painter @Composable get() = painterResource(icon_search)
}
