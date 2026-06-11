package com.anadolstudio.homehub.feature.automation.common.presentation

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeviceUnknown
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
internal fun SceneItem(
        icon: Painter,
        title: String,
        modifier: Modifier = Modifier,
        draggableActionIcon: ImageVector = Icons.Outlined.DeviceUnknown,
        onClicked: (() -> Unit)? = null,
        onDraggableActionClicked: (() -> Unit)? = null,
        trailing: @Composable (RowScope.() -> Unit)? = null
) {
    BaseAutomationItem(
            modifier = modifier,
            icon = icon,
            title = title,
            onClicked = onClicked,
            draggableActionIcon = draggableActionIcon,
            onDraggableActionClicked = onDraggableActionClicked,
            trailing = trailing
    )
}
