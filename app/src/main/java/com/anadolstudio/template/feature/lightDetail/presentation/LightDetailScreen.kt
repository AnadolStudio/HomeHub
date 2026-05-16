package com.anadolstudio.template.feature.lightDetail.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.anadolstudio.compose.ui.theme.AppTheme
import com.anadolstudio.compose.ui.theme.Dimension
import com.anadolstudio.compose.ui.view.snackbar.SnackbarHostState
import com.anadolstudio.template.base.view.homeHubSwitchDefaults
import com.anadolstudio.template.di.viewmodel.assistedViewModel
import com.anadolstudio.template.di.viewmodel.rememberViewModelFactory
import com.anadolstudio.template.event.ObserveEvents
import com.anadolstudio.template.feature.main.NavigationController
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
internal fun LightDetailScreen(
        navigator: NavigationController,
        snackbarHostState: SnackbarHostState,
        args: LightDetailArgs,
) {
    // См. комментарий в DeviceDetailScreen.
    if (LocalLifecycleOwner.current.lifecycle.currentState == Lifecycle.State.DESTROYED) return

    val factory = rememberViewModelFactory<LightDetailViewModel.Factory>()
    val viewModel = assistedViewModel { factory.create(args) }

    val state by viewModel.stateFlow.collectAsState()
    ObserveEvents(viewModel.events, snackbarHostState, navigator)

    BackHandler { viewModel.onCloseClicked() }

    LightDetailLayout(state = state, controller = viewModel)
}

@Composable
private fun LightDetailLayout(
        state: LightDetailScreenState,
        controller: LightDetailController,
) {
    Column(
            modifier = Modifier
                    .fillMaxWidth()
                    .background(
                            color = AppTheme.colors.colorSecondary,
                            shape = RoundedCornerShape(
                                    topStart = Dimension.mainMargin,
                                    topEnd = Dimension.mainMargin,
                            ),
                    )
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
    ) {
        Spacer(modifier = Modifier.statusBarsPadding())

        Header(state = state, onCloseClicked = controller::onCloseClicked)

        Spacer(modifier = Modifier.height(16.dp))

        ColorPreview(red = state.red, green = state.green, blue = state.blue)

        Spacer(modifier = Modifier.height(24.dp))

        PowerToggleRow(isOn = state.isOn, onToggleClicked = controller::onToggleClicked)

        Spacer(modifier = Modifier.height(16.dp))

        BrightnessSlider(
                percent = state.brightnessPercent,
                onChanged = controller::onBrightnessChanged,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
                text = "Цвет",
                style = AppTheme.typography.textBook14,
                color = AppTheme.colors.colorAccent,
        )

        Spacer(modifier = Modifier.height(12.dp))

        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            ColorWheel(
                    red = state.red,
                    green = state.green,
                    blue = state.blue,
                    onChanged = controller::onRgbChanged,
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun Header(state: LightDetailScreenState, onCloseClicked: () -> Unit) {
    Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onCloseClicked) {
            Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = null,
                    tint = AppTheme.colors.colorAccent,
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            state.areaName?.let {
                Text(
                        text = it,
                        style = AppTheme.typography.captionBook14,
                        color = AppTheme.colors.textSecondary,
                )
            }
            Text(
                    text = state.friendlyName,
                    style = AppTheme.typography.textMedium18,
                    color = AppTheme.colors.colorAccent,
            )
        }
    }
}

@Composable
private fun ColorPreview(red: Int, green: Int, blue: Int) {
    Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
    ) {
        Box(
                modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(Color(red = red, green = green, blue = blue)),
        )
    }
}

@Composable
private fun PowerToggleRow(isOn: Boolean, onToggleClicked: (Boolean) -> Unit) {
    Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
                modifier = Modifier.weight(1f),
                text = if (isOn) "Включено" else "Выключено",
                style = AppTheme.typography.textBook18,
                color = AppTheme.colors.colorAccent,
        )
        Switch(
                checked = isOn,
                onCheckedChange = onToggleClicked,
                colors = homeHubSwitchDefaults,
        )
    }
}

@Composable
private fun BrightnessSlider(percent: Int, onChanged: (Int) -> Unit) {
    var local by remember(percent) { mutableFloatStateOf(percent.toFloat()) }
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                    modifier = Modifier.weight(1f),
                    text = "Яркость",
                    style = AppTheme.typography.textBook14,
                    color = AppTheme.colors.colorAccent,
            )
            Text(
                    text = "${local.toInt()}%",
                    style = AppTheme.typography.textBook14,
                    color = AppTheme.colors.colorAccent,
            )
        }
        Slider(
                value = local,
                valueRange = 0f..100f,
                onValueChange = { local = it },
                onValueChangeFinished = { onChanged(local.toInt()) },
                colors = SliderDefaults.colors(
                        thumbColor = AppTheme.colors.colorAccent,
                        activeTrackColor = AppTheme.colors.colorAccent,
                        inactiveTrackColor = AppTheme.colors.textSecondary,
                ),
        )
    }
}

@Composable
private fun ColorWheel(
        red: Int,
        green: Int,
        blue: Int,
        onChanged: (Int, Int, Int) -> Unit,
) {
    val density = LocalDensity.current
    val sizeDp = 240.dp
    val sizePx = with(density) { sizeDp.toPx() }
    val radius = sizePx / 2f
    val center = Offset(radius, radius)

    val initialHsv = remember(red, green, blue) {
        FloatArray(3).also { android.graphics.Color.RGBToHSV(red, green, blue, it) }
    }

    var selectorOffset by remember(red, green, blue) {
        val angleRad = Math.toRadians(initialHsv[0].toDouble())
        val dist = initialHsv[1] * radius
        mutableStateOf(
                Offset(
                        x = center.x + (dist * cos(angleRad)).toFloat(),
                        y = center.y + (dist * sin(angleRad)).toFloat(),
                ),
        )
    }
    var localColor by remember(red, green, blue) {
        mutableStateOf(Color(red, green, blue))
    }

    val onChangedLatest by rememberUpdatedState(onChanged)

    fun commit(color: Color) {
        val argb = color.toArgb()
        onChangedLatest(
                android.graphics.Color.red(argb),
                android.graphics.Color.green(argb),
                android.graphics.Color.blue(argb),
        )
    }

    fun offsetToColor(offset: Offset): Pair<Offset, Color> {
        val dx = offset.x - center.x
        val dy = offset.y - center.y
        val dist = sqrt(dx * dx + dy * dy)
        val clamped = if (dist > radius && dist > 0f) {
            Offset(center.x + dx / dist * radius, center.y + dy / dist * radius)
        } else {
            offset
        }
        var angle = Math.toDegrees(
                atan2((clamped.y - center.y).toDouble(), (clamped.x - center.x).toDouble())
        ).toFloat()
        if (angle < 0f) angle += 360f
        val sat = (dist / radius).coerceIn(0f, 1f)
        val argb = android.graphics.Color.HSVToColor(floatArrayOf(angle, sat, 1f))
        return clamped to Color(argb)
    }

    Box(
            modifier = Modifier
                    .size(sizeDp)
                    .pointerInput(Unit) {
                        awaitEachGesture {
                            val down = awaitFirstDown()
                            val (downOff, downColor) = offsetToColor(down.position)
                            selectorOffset = downOff
                            localColor = downColor
                            commit(downColor)
                            down.consume()

                            var pressed = true
                            while (pressed) {
                                val event = awaitPointerEvent()
                                val change = event.changes.firstOrNull() ?: break
                                if (!change.pressed) {
                                    pressed = false
                                } else if (change.position != change.previousPosition) {
                                    val (off, color) = offsetToColor(change.position)
                                    selectorOffset = off
                                    localColor = color
                                    commit(color)
                                    change.consume()
                                }
                            }
                        }
                    },
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                    brush = Brush.sweepGradient(
                            colors = listOf(
                                    Color.Red,
                                    Color.Yellow,
                                    Color.Green,
                                    Color.Cyan,
                                    Color.Blue,
                                    Color.Magenta,
                                    Color.Red,
                            ),
                            center = center,
                    ),
                    radius = radius,
                    center = center,
            )
            drawCircle(
                    brush = Brush.radialGradient(
                            colors = listOf(Color.White, Color.Transparent),
                            center = center,
                            radius = radius,
                    ),
                    radius = radius,
                    center = center,
            )
            drawCircle(
                    color = Color.White,
                    radius = 9.dp.toPx(),
                    center = selectorOffset,
                    style = Stroke(width = 3.dp.toPx()),
            )
            drawCircle(
                    color = localColor,
                    radius = 7.dp.toPx(),
                    center = selectorOffset,
            )
        }
    }
}
