package com.anadolstudio.compose.ui.view.snackbar

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import com.anadolstudio.compose.ui.theme.AppTheme
import com.anadolstudio.compose.ui.theme.Dimension
import kotlin.coroutines.resume
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.launch
import androidx.compose.material.SnackbarHost as MaterialSnackbarHost
import androidx.compose.material.SnackbarHostState as MaterialSnackbarHostState

interface SnackbarStyle {

    val icon: Painter?
        @Composable get() = null
    val shape: Shape
        @Composable get() = RoundedCornerShape(0)
    val backgroundColor: Color
        @Composable get() = SnackbarDefaults.backgroundColor
    val contentColor: Color
        @Composable get() = Color.White

    object Default : SnackbarStyle {
        override val backgroundColor: Color
            @Composable get() = AppTheme.colors.colorPrimary
        override val contentColor: Color
            @Composable get() = AppTheme.colors.colorAccent
        override val shape: Shape
            @Composable get() = RoundedCornerShape(Dimension.smallMargin)
    }

    object Error : SnackbarStyle {
        override val backgroundColor: Color
            @Composable get() = AppTheme.colors.colorError
        override val contentColor: Color
            @Composable get() = AppTheme.colors.colorAccent
        override val shape: Shape
            @Composable get() = RoundedCornerShape(Dimension.smallMargin)
    }
}

@Suppress("ForbiddenMethodCall")
@Composable
fun SnackbarHost(
        hostState: MaterialSnackbarHostState,
        modifier: Modifier = Modifier,
) {
    MaterialSnackbarHost(
            hostState = hostState,
            modifier = modifier,
            snackbar = { SwipeToDismissSnackbar(it) },
    )
}

private const val DISMISS_THRESHOLD = 100f

@Composable
private fun StyledSnackbar(snackbarData: SnackbarData) {
    if (snackbarData is StyledSnackbarData) {
        Snackbar(snackbarData = snackbarData)
    } else {
        Snackbar(snackbarData)
    }
}

@Composable
private fun SwipeToDismissSnackbar(snackbarData: SnackbarData) {
    val offsetX = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .pointerInput(snackbarData) {
                    detectDragGestures(
                            onDragEnd = {
                                scope.launch {
                                    if (abs(offsetX.value) > DISMISS_THRESHOLD) {
                                        val targetX =
                                                if (offsetX.value > 0) size.width.toFloat() else -size.width.toFloat()
                                        offsetX.animateTo(targetX, tween(200))
                                        snackbarData.dismiss()
                                    } else {
                                        offsetX.animateTo(0f, tween(200))
                                    }
                                }
                            },
                            onDrag = { _, dragAmount ->
                                scope.launch {
                                    offsetX.snapTo(offsetX.value + dragAmount.x)
                                }
                            },
                    )
                }
    ) {
        StyledSnackbar(snackbarData)
    }
}

@Stable
internal class StyledSnackbarData(
        override val message: String,
        override val actionLabel: String?,
        override val duration: SnackbarDuration,
        val style: SnackbarStyle,
        private val continuation: CancellableContinuation<SnackbarResult>,
) : SnackbarData {
    override fun dismiss() {
        if (continuation.isActive) continuation.resume(SnackbarResult.Dismissed)
    }

    override fun performAction() {
        if (continuation.isActive) continuation.resume(SnackbarResult.ActionPerformed)
    }
}
