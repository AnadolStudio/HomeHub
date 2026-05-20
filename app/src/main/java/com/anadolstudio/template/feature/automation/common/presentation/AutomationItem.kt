package com.anadolstudio.template.feature.automation.common.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import com.anadolstudio.compose.ui.theme.AppTheme
import com.anadolstudio.compose.ui.theme.Dimmens
import com.anadolstudio.compose.ui.theme.Shapes
import com.anadolstudio.compose.ui.theme.largeShimmer
import com.anadolstudio.template.base.view.homeHubSwitchDefaults

@Composable
internal fun AutomationItem(
        icon: Painter,
        title: String,
        isEnable: Boolean? = null,
        onEnableClicked: (Boolean) -> Unit = {},
        onClicked: (() -> Unit)? = null,
) {
    Row(
            modifier = Modifier
                    .fillMaxWidth()
                    .clip(Shapes.largeShimmer)
                    .background(AppTheme.colors.colorPrimary)
                    .clickable(enabled = onClicked != null, onClick = { onClicked?.invoke() })
                    .heightIn(min = LocalMinimumInteractiveComponentSize.current)
                    .padding(vertical = Dimmens.smallMargin, horizontal = Dimmens.smallMargin),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimmens.smallMargin)
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

        if (isEnable != null) {
            Switch(
                    modifier = Modifier.padding(end = Dimmens.smallMargin),
                    checked = isEnable,
                    onCheckedChange = { value -> onEnableClicked.invoke(value) },
                    colors = homeHubSwitchDefaults,
            )
        }
    }
}
