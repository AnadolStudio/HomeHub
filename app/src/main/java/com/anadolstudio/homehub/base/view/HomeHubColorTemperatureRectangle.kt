package com.anadolstudio.homehub.base.view

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp

/**
 * Полоса цветовой температуры.
 *
 * По умолчанию ([invert] = `false`): слева — тёплый край ([min]), справа — холодный ([max]).
 * Подходит для шкалы в Кельвинах (низкое значение = тёплый).
 *
 * При [invert] = `true` градиент переворачивается: слева — холодный край ([min]),
 * справа — тёплый ([max]). Подходит для шкалы в миредах Home Assistant
 * (низкое значение = холодный).
 *
 * Размер задаётся через [modifier].
 */
@Composable
internal fun HomeHubColorTemperatureRectangle(
        value: Int,
        min: Int,
        max: Int,
        onChanged: (Int) -> Unit,
        modifier: Modifier = Modifier,
        invert: Boolean = false,
        showIndicator: Boolean = true,
) {
    val range = (max - min).coerceAtLeast(1)

    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    val widthPx = canvasSize.width.toFloat()
    val heightPx = canvasSize.height.toFloat()

    // selectorX отвязан от внешнего value: иначе round-trip через VM сбрасывал бы позицию
    // на каждый commit. Инициализируется при появлении canvasSize, дальше — локальная правда.
    var selectorX by remember(canvasSize) {
        mutableFloatStateOf(
                if (canvasSize == IntSize.Zero) {
                    0f
                } else {
                    ((value - min).toFloat() / range).coerceIn(0f, 1f) * widthPx
                },
        )
    }
    var isInteracting by remember { mutableStateOf(false) }
    // Снимок последнего входящего value. Sync делаем только когда оно реально изменилось
    // относительно предыдущего входящего — иначе рекомпозиции с тем же value (когда entity
    // ещё не успело обновиться после нашего commit) сбрасывали бы selectorX.
    var lastIncomingValue by remember { mutableIntStateOf(value) }

    // External sync: подтягиваем входящий value, только если юзер не взаимодействует
    // и значение реально отличается от того, что соответствует текущему selectorX.
    LaunchedEffect(value, min, max, isInteracting, canvasSize) {
        if (isInteracting || canvasSize == IntSize.Zero) return@LaunchedEffect
        if (value == lastIncomingValue) return@LaunchedEffect
        lastIncomingValue = value

        val currentFraction = (selectorX / widthPx).coerceIn(0f, 1f)
        val currentValue = (min + currentFraction * range).toInt().coerceIn(min, max)
        if (currentValue == value) return@LaunchedEffect

        val newFraction = ((value - min).toFloat() / range).coerceIn(0f, 1f)
        selectorX = newFraction * widthPx
    }

    val onChangedLatest by rememberUpdatedState(onChanged)

    Box(
            modifier = modifier
                    .onSizeChanged { canvasSize = it }
                    .pointerInput(Unit) {
                        awaitEachGesture {
                            val down = awaitFirstDown()
                            isInteracting = true
                            offsetToValue(down.position, widthPx, min, max, range)?.let { (x, v) ->
                                selectorX = x
                                onChangedLatest(v)
                            }
                            down.consume()

                            var pressed = true
                            while (pressed) {
                                val event = awaitPointerEvent()
                                val change = event.changes.firstOrNull() ?: break
                                if (!change.pressed) {
                                    pressed = false
                                } else if (change.position != change.previousPosition) {
                                    offsetToValue(change.position, widthPx, min, max, range)?.let { (x, v) ->
                                        selectorX = x
                                        onChangedLatest(v)
                                    }
                                    change.consume()
                                }
                            }
                            isInteracting = false
                        }
                    },
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val warmToCool = listOf(
                    Color(0xFFFFB16D),
                    Color(0xFFFFE5C2),
                    Color.White,
            )
            val gradientColors = if (invert) warmToCool.asReversed() else warmToCool
            drawRect(
                    brush = Brush.horizontalGradient(colors = gradientColors),
                    size = size,
            )

            if (showIndicator) {
                val fraction = if (widthPx > 0f) (selectorX / widthPx) else 0f
                val localColor = colorAtFraction(gradientColors, fraction)
                val strokeColor = if (localColor.luminance() > LIGHT_LUMINANCE_THRESHOLD) Color.Black else Color.White

                val inset = 6.dp.toPx()
                val strokeWidth = 4.dp.toPx()
                val halfStroke = strokeWidth / 2f
                // Клампим X отрисовки, чтобы линия полностью укладывалась в канвас, а не уползала
                // за границу на halfStroke на крайних значениях selectorX.
                val drawX = selectorX.coerceIn(halfStroke, (widthPx - halfStroke).coerceAtLeast(halfStroke))
                drawLine(
                        color = strokeColor,
                        start = Offset(drawX, inset),
                        end = Offset(drawX, heightPx - inset),
                        strokeWidth = strokeWidth,
                        cap = StrokeCap.Round,
                )
            }
        }
    }
}

private const val LIGHT_LUMINANCE_THRESHOLD = 0.95f

private fun colorAtFraction(stops: List<Color>, fraction: Float): Color {
    if (stops.size == 1) return stops[0]
    val pos = fraction.coerceIn(0f, 1f) * (stops.size - 1)
    val i = pos.toInt().coerceAtMost(stops.size - 2)
    val t = pos - i
    return lerp(stops[i], stops[i + 1], t)
}

private fun offsetToValue(
        offset: Offset,
        widthPx: Float,
        min: Int,
        max: Int,
        range: Int,
): Pair<Float, Int>? {
    if (widthPx <= 0f) return null
    val clampedX = offset.x.coerceIn(0f, widthPx)
    val fraction = clampedX / widthPx
    val newValue = (min + fraction * range).toInt().coerceIn(min, max)
    return clampedX to newValue
}
