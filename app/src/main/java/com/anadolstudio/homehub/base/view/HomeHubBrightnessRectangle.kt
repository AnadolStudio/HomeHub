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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp

/**
 * Полоса яркости для заданного [color] в стиле progress-bar.
 *
 * Фон полосы — [color] с alpha = 0.5 (вылеженный, для незаполненной части).
 * Слева от селектора (`0..drawX`) — [color] с alpha = 1 (полная заливка).
 * Селектор — вертикальная линия, контраст подбирается по luminance цвета.
 *
 * [value] — процент от 0 до 100. Размер задаётся через [modifier].
 */
@Composable
internal fun HomeHubBrightnessRectangle(
        value: Int,
        color: Color,
        onChanged: (Int) -> Unit,
        modifier: Modifier = Modifier,
) {
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    val widthPx = canvasSize.width.toFloat()
    val heightPx = canvasSize.height.toFloat()

    // selectorX отвязан от внешнего value — синком занимается LaunchedEffect ниже.
    var selectorX by remember(canvasSize) {
        mutableFloatStateOf(
                if (canvasSize == IntSize.Zero) {
                    0f
                } else {
                    (value.toFloat() / PERCENT_MAX).coerceIn(0f, 1f) * widthPx
                },
        )
    }
    var isInteracting by remember { mutableStateOf(false) }
    // Снимок последнего входящего value. Sync делаем только когда оно реально изменилось
    // относительно предыдущего входящего — иначе рекомпозиции с тем же value (когда entity
    // ещё не успело обновиться после нашего commit) сбрасывали бы selectorX.
    var lastIncomingValue by remember { mutableIntStateOf(value) }

    LaunchedEffect(value, isInteracting, canvasSize) {
        if (isInteracting || canvasSize == IntSize.Zero) return@LaunchedEffect
        if (value == lastIncomingValue) return@LaunchedEffect
        lastIncomingValue = value

        val currentFraction = (selectorX / widthPx).coerceIn(0f, 1f)
        val currentValue = (currentFraction * PERCENT_MAX).toInt().coerceIn(0, PERCENT_MAX.toInt())
        if (currentValue == value) return@LaunchedEffect

        val newFraction = (value.toFloat() / PERCENT_MAX).coerceIn(0f, 1f)
        selectorX = newFraction * widthPx
    }

    // Пробрасываем не чаще одного раза в 300 мс, чтобы не спамить устройство во время свайпа.
    val throttledOnChanged = rememberThrottled<Int>(action = onChanged)

    Box(
            modifier = modifier
                    .onSizeChanged { canvasSize = it }
                    .pointerInput(Unit) {
                        awaitEachGesture {
                            val down = awaitFirstDown()
                            isInteracting = true
                            offsetToPercent(down.position, widthPx)?.let { (x, v) ->
                                selectorX = x
                                throttledOnChanged(v)
                            }
                            down.consume()

                            var pressed = true
                            while (pressed) {
                                val event = awaitPointerEvent()
                                val change = event.changes.firstOrNull() ?: break
                                if (!change.pressed) {
                                    pressed = false
                                } else if (change.position != change.previousPosition) {
                                    offsetToPercent(change.position, widthPx)?.let { (x, v) ->
                                        selectorX = x
                                        throttledOnChanged(v)
                                    }
                                    change.consume()
                                }
                            }
                            isInteracting = false
                        }
                    },
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Фон: цвет с полупрозрачностью на всю ширину.
            drawRect(
                    color = color.copy(alpha = 0.7f),
                    size = size,
            )
            // Заливка: непрозрачный цвет от 0 до позиции селектора.
            if (selectorX > 0f) {
                drawRect(
                        color = color.copy(alpha = 1f),
                        size = Size(width = selectorX, height = heightPx),
                )
            }

            val strokeColor = if (color.luminance() > LIGHT_LUMINANCE_THRESHOLD) Color.Black else Color.White

            val inset = 6.dp.toPx()
            val strokeWidth = 4.dp.toPx()
            val halfStroke = strokeWidth / 2f
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

private const val LIGHT_LUMINANCE_THRESHOLD = 0.85f
private const val PERCENT_MAX = 100f

private fun offsetToPercent(offset: Offset, widthPx: Float): Pair<Float, Int>? {
    if (widthPx <= 0f) return null
    val clampedX = offset.x.coerceIn(0f, widthPx)
    val fraction = clampedX / widthPx
    val percent = (fraction * PERCENT_MAX).toInt().coerceIn(0, PERCENT_MAX.toInt())
    return clampedX to percent
}

