package com.anadolstudio.template.base.view

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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp

/**
 * HSV color picker. X — hue (0..360), Y — saturation (0 сверху → 1 снизу), V зафиксирован = 1.
 * Конвенция HS совпадает с Home Assistant `hs_color`: `hue 0..360`, `saturation 0..100`.
 *
 * Размер задаётся через [modifier].
 */
@Composable
internal fun HomeHubColorRectangle(
        hue: Float,
        saturation: Float,
        onChanged: (Float, Float) -> Unit,
        modifier: Modifier = Modifier,
        showIndicator: Boolean = true,
) {
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    val widthPx = canvasSize.width.toFloat()
    val heightPx = canvasSize.height.toFloat()

    val initialHue = remember { hue }
    val initialSat = remember { saturation }

    var selectorOffset by remember(canvasSize) {
        mutableStateOf(
                if (canvasSize == IntSize.Zero) {
                    Offset.Zero
                } else {
                    Offset(
                            x = (initialHue / HUE_MAX).coerceIn(0f, 1f) * widthPx,
                            y = (initialSat / SAT_MAX).coerceIn(0f, 1f) * heightPx,
                    )
                },
        )
    }
    var localHue by remember { mutableFloatStateOf(hue) }
    var localSat by remember { mutableFloatStateOf(saturation) }
    var isInteracting by remember { mutableStateOf(false) }
    // Снимок последнего входящего ARGB. Sync делаем только если он реально изменился
    // относительно предыдущего входящего — рекомпозиции с теми же hue/saturation (когда
    // entity ещё не успело обновиться после нашего commit) не должны сбрасывать селектор.
    var lastIncomingArgb by remember {
        mutableIntStateOf(android.graphics.Color.HSVToColor(floatArrayOf(hue, saturation / SAT_MAX, 1f)))
    }

    LaunchedEffect(hue, saturation, isInteracting, canvasSize) {
        if (isInteracting || canvasSize == IntSize.Zero) return@LaunchedEffect

        val incomingArgb = android.graphics.Color.HSVToColor(floatArrayOf(hue, saturation / SAT_MAX, 1f))
        if (incomingArgb == lastIncomingArgb) return@LaunchedEffect
        lastIncomingArgb = incomingArgb

        // Дополнительная защита: HSV → RGB → HSV не сохраняет hue при sat=0 (любой hue → белый)
        // и на правом крае (hue=360 ≡ hue=0). Если визуально цвета local'а и incoming'а
        // совпадают — это наш собственный round-trip, селектор двигать не надо.
        val localArgb = android.graphics.Color.HSVToColor(floatArrayOf(localHue, localSat / SAT_MAX, 1f))
        if (incomingArgb == localArgb) return@LaunchedEffect

        selectorOffset = Offset(
                x = (hue / HUE_MAX).coerceIn(0f, 1f) * widthPx,
                y = (saturation / SAT_MAX).coerceIn(0f, 1f) * heightPx,
        )
        localHue = hue
        localSat = saturation
    }

    val onChangedLatest by rememberUpdatedState(onChanged)

    Box(
            modifier = modifier
                    .onSizeChanged { canvasSize = it }
                    .pointerInput(Unit) {
                        awaitEachGesture {
                            val down = awaitFirstDown()
                            isInteracting = true
                            offsetToHs(down.position, widthPx, heightPx)?.let { (off, h, s) ->
                                selectorOffset = off
                                localHue = h
                                localSat = s
                                onChangedLatest(h, s)
                            }
                            down.consume()

                            var pressed = true
                            while (pressed) {
                                val event = awaitPointerEvent()
                                val change = event.changes.firstOrNull() ?: break
                                if (!change.pressed) {
                                    pressed = false
                                } else if (change.position != change.previousPosition) {
                                    offsetToHs(change.position, widthPx, heightPx)?.let { (off, h, s) ->
                                        selectorOffset = off
                                        localHue = h
                                        localSat = s
                                        onChangedLatest(h, s)
                                    }
                                    change.consume()
                                }
                            }
                            isInteracting = false
                        }
                    },
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(
                    brush = Brush.horizontalGradient(
                            colors = listOf(
                                    Color.Red,
                                    Color.Yellow,
                                    Color.Green,
                                    Color.Cyan,
                                    Color.Blue,
                                    Color.Magenta,
                                    Color.Red,
                            ),
                    ),
                    size = size,
            )
            drawRect(
                    brush = Brush.verticalGradient(
                            colors = listOf(Color.White, Color.Transparent),
                    ),
                    size = size,
            )
            if (showIndicator) {
                val currentColor = Color(
                        android.graphics.Color.HSVToColor(floatArrayOf(localHue, localSat / SAT_MAX, 1f)),
                )
                val strokeColor = if (currentColor.luminance() > LIGHT_LUMINANCE_THRESHOLD) Color.Black else Color.White
                drawCircle(
                        color = strokeColor,
                        radius = 9.dp.toPx(),
                        center = selectorOffset,
                        style = Stroke(width = 3.dp.toPx()),
                )
                drawCircle(
                        color = currentColor,
                        radius = 7.dp.toPx(),
                        center = selectorOffset,
                )
            }
        }
    }
}

/**
 * RGB-перегрузка — тонкая обёртка: переводит RGB ↔ HSV и делегирует в HS-версию.
 * Защита от round-trip (hue=360 ≡ 0, sat=0 теряет hue) живёт в HS-версии — она сравнивает
 * входящие и локальные значения по их ARGB-эквиваленту.
 */
@Composable
internal fun HomeHubColorRectangle(
        red: Int,
        green: Int,
        blue: Int,
        onChanged: (Int, Int, Int) -> Unit,
        modifier: Modifier = Modifier,
        showIndicator: Boolean = true,
) {
    val hsv = remember(red, green, blue) {
        FloatArray(3).also { android.graphics.Color.RGBToHSV(red, green, blue, it) }
    }
    HomeHubColorRectangle(
            hue = hsv[0],
            saturation = hsv[1] * SAT_MAX,
            onChanged = { h, s ->
                val argb = android.graphics.Color.HSVToColor(floatArrayOf(h, s / SAT_MAX, 1f))
                onChanged(
                        android.graphics.Color.red(argb),
                        android.graphics.Color.green(argb),
                        android.graphics.Color.blue(argb),
                )
            },
            modifier = modifier,
            showIndicator = showIndicator,
    )
}

private const val LIGHT_LUMINANCE_THRESHOLD = 0.9f
private const val HUE_MAX = 360f
private const val SAT_MAX = 100f

private fun offsetToHs(
        offset: Offset,
        widthPx: Float,
        heightPx: Float,
): Triple<Offset, Float, Float>? {
    if (widthPx <= 0f || heightPx <= 0f) return null
    val clampedX = offset.x.coerceIn(0f, widthPx)
    val clampedY = offset.y.coerceIn(0f, heightPx)
    val hue = (clampedX / widthPx) * HUE_MAX
    val sat = (clampedY / heightPx) * SAT_MAX
    return Triple(Offset(clampedX, clampedY), hue, sat)
}
