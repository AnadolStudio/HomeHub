package com.anadolstudio.homehub.feature.automation.automationDetail.presentation.items

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.anadolstudio.compose.ui.theme.AppTheme
import com.anadolstudio.compose.ui.theme.Dimmens
import com.anadolstudio.compose.ui.theme.Shapes
import com.anadolstudio.compose.ui.theme.largeShimmer
import com.anadolstudio.homehub.R
import com.anadolstudio.homehub.feature.home.domain.model.DeviceImage
import com.anadolstudio.homehub.feature.home.presentation.components.DeviceImageView

@Composable
internal fun TriggerItem(
        title: String,
        modifier: Modifier = Modifier,
        subtitle: String? = null,
        image: DeviceImage? = null,
        onClick: () -> Unit = {},
        onDeleteClicked: () -> Unit = {},
) {
    Row(
            modifier = modifier
                    .fillMaxWidth()
                    .shadow(2.dp, Shapes.largeShimmer)
                    .background(AppTheme.colors.colorPrimary)
                    .clip(Shapes.largeShimmer)
                    .clickable(onClick = onClick)
                    .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
    ) {
        DeviceImageView(
                image = image,
                modifier = Modifier.size(40.dp),
                imageSize = 40.dp,
        )

        Spacer(modifier = Modifier.width(Dimmens.smallMargin))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                    text = title,
                    style = AppTheme.typography.textMedium18,
                    color = AppTheme.colors.colorAccent,
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                        text = subtitle,
                        style = AppTheme.typography.captionBook14,
                        color = AppTheme.colors.textSecondary,
                )
            }
        }

        IconButton(onClick = onDeleteClicked) {
            Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = stringResource(R.string.automation_detail_action_delete),
                    tint = AppTheme.colors.colorAccent,
            )
        }
    }
}
