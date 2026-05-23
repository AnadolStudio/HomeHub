package com.anadolstudio.template.feature.automation.common.presentation

import androidx.compose.animation.core.animate
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeviceUnknown
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.anadolstudio.compose.ui.theme.AppTheme
import com.anadolstudio.compose.ui.theme.Dimmens
import com.anadolstudio.compose.ui.theme.Shapes
import com.anadolstudio.compose.ui.theme.largeShimmer
import com.anadolstudio.template.R
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun BaseAutomationItem(
        icon: Painter,
        title: String,
        modifier: Modifier = Modifier,
        draggableActionIcon: ImageVector = Icons.Outlined.DeviceUnknown,
        onClicked: (() -> Unit)? = null,
        onDraggableActionClicked: (() -> Unit)? = null,
        trailing: @Composable (RowScope.() -> Unit)? = null,
) {
    if (onDraggableActionClicked == null) {
        BaseAutomationItemRow(
                modifier = modifier
                        .fillMaxWidth()
                        .clip(Shapes.largeShimmer),
                icon = icon,
                title = title,
                onClicked = onClicked,
                trailing = trailing,
        )
        return
    }

    val actionWidthDp = 72.dp
    val actionWidthPx = with(LocalDensity.current) { actionWidthDp.toPx() }
    val velocityThresholdPx = with(LocalDensity.current) { 100.dp.toPx() }
    val scope = rememberCoroutineScope()

    val swipeState = remember {
        AnchoredDraggableState(
                initialValue = SwipeRevealState.Closed,
                anchors = DraggableAnchors {
                    SwipeRevealState.Closed at 0f
                    SwipeRevealState.Open at -actionWidthPx
                },
                positionalThreshold = { totalDistance -> totalDistance * 0.5f },
                velocityThreshold = { velocityThresholdPx },
                snapAnimationSpec = spring(),
                decayAnimationSpec = exponentialDecay(),
        )
    }
    val isOpen = swipeState.currentValue == SwipeRevealState.Open

    val effectiveOnClicked: (() -> Unit)? = if (isOpen) {
        {
            scope.launch { swipeState.animateToValue(SwipeRevealState.Closed) }
        }
    } else {
        onClicked
    }

    Box(
            modifier = modifier
                    .fillMaxWidth()
                    .clip(Shapes.largeShimmer)
                    .height(IntrinsicSize.Min),
    ) {
        Box(
                modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .fillMaxHeight()
                        .width(actionWidthDp)
                        .background(AppTheme.colors.colorError)
                        .clickable(enabled = isOpen) {
                            scope.launch { swipeState.animateToValue(SwipeRevealState.Closed) }
                            onDraggableActionClicked()
                        },
                contentAlignment = Alignment.Center,
        ) {
            Icon(
                    imageVector = draggableActionIcon,
                    contentDescription = stringResource(R.string.automation_list_action_delete),
                    tint = AppTheme.colors.colorPrimary,
            )
        }

        BaseAutomationItemRow(
                modifier = Modifier
                        .offset { IntOffset(swipeState.requireOffset().toInt(), 0) }
                        .anchoredDraggable(state = swipeState, orientation = Orientation.Horizontal),
                icon = icon,
                title = title,
                onClicked = effectiveOnClicked,
                trailing = trailing,
        )
    }
}

@Composable
private fun BaseAutomationItemRow(
        modifier: Modifier,
        icon: Painter,
        title: String,
        onClicked: (() -> Unit)?,
        trailing: @Composable (RowScope.() -> Unit)?,
) {
    Row(
            modifier = modifier
                    .fillMaxWidth()
                    .background(AppTheme.colors.colorPrimary)
                    .clickable(enabled = onClicked != null) { onClicked?.invoke() }
                    .heightIn(min = LocalMinimumInteractiveComponentSize.current)
                    .padding(vertical = Dimmens.smallMargin, horizontal = Dimmens.smallMargin),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimmens.smallMargin),
    ) {
        Icon(
                modifier = Modifier.size(24.dp),
                painter = icon,
                tint = AppTheme.colors.colorAccent,
                contentDescription = null,
        )
        Text(
                modifier = Modifier.weight(1f),
                text = title,
                style = AppTheme.typography.captionBook16,
                color = AppTheme.colors.colorAccent,
        )
        trailing?.invoke(this)
    }
}

private enum class SwipeRevealState { Closed, Open }

@OptIn(ExperimentalFoundationApi::class)
private suspend fun <T> AnchoredDraggableState<T>.animateToValue(targetValue: T) {
    anchoredDrag(targetValue = targetValue) { anchors, target ->
        val to = anchors.positionOf(target)
        if (!to.isNaN()) {
            val from = this@animateToValue.requireOffset()
            animate(initialValue = from, targetValue = to) { value, _ -> dragTo(value) }
        }
    }
}
