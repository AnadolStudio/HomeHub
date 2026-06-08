package com.anadolstudio.homehub.feature.automation.automationDetail.presentation.items

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FlashOn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
internal fun TriggerItem(
        title: String,
        modifier: Modifier = Modifier,
        subtitle: String? = null,
        onEditClicked: () -> Unit = {},
        onDeleteClicked: () -> Unit = {},
) {
    AutomationDetailCard(
            modifier = modifier,
            icon = Icons.Outlined.FlashOn,
            title = title,
            subtitle = subtitle,
            onEditClicked = onEditClicked,
            onDeleteClicked = onDeleteClicked,
    )
}
