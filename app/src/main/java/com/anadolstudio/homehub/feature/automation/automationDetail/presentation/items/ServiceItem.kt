package com.anadolstudio.homehub.feature.automation.automationDetail.presentation.items

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
internal fun ServiceItem(
        title: String,
        modifier: Modifier = Modifier,
        subtitle: String? = null,
        onEditClicked: () -> Unit = {},
        onDeleteClicked: () -> Unit = {},
) {
    AutomationDetailCard(
            modifier = modifier,
            icon = Icons.Outlined.Bolt,
            title = title,
            subtitle = subtitle,
            onEditClicked = onEditClicked,
            onDeleteClicked = onDeleteClicked,
    )
}
