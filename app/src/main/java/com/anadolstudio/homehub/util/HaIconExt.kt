package com.anadolstudio.homehub.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import com.anadolstudio.ha_resources.HaIcon

@Composable
fun HaIcon.toPainter(): Painter = painterResource(drawableRes)
