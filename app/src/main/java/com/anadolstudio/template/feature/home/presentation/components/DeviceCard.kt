package com.anadolstudio.template.feature.home.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowColumn
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.PowerSettingsNew
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.anadolstudio.compose.ui.theme.AppTheme
import com.anadolstudio.compose.ui.theme.Dimension
import com.anadolstudio.compose.ui.theme.Shapes
import com.anadolstudio.compose.ui.theme.image
import com.anadolstudio.compose.ui.theme.preview.ThemePreviewParameter
import com.anadolstudio.template.feature.home.domain.model.HomeAssistantEntity
import com.anadolstudio.template.feature.home.domain.model.states.AllowedState
import com.anadolstudio.template.feature.home.presentation.PreviewUtils
import kotlin.math.min

private val DEVICE_IMAGE_MAX_SIZE = 60.dp
private val DEVICE_IMAGE_MIN_SIZE = 48.dp
private val DEVICE_CARD_SHAPE = RoundedCornerShape(12.dp)
private val DEVICE_CARD_ELEVATION = 4.dp
private const val MAX_SWITCH_ENTITY = 6
private const val MAX_PER_COLUMN = 3

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SwitchCard(
        title: String,
        description: String?,
        imageUrl: String?,
        switchEntityList: List<HomeAssistantEntity>,
        onInnerEntityClicked: (entity: HomeAssistantEntity) -> Unit,
        onDeviceClicked: () -> Unit,
) {
    BaseDeviceCard(
            title = title,
            description = description,
            imageUrl = imageUrl,
            onDeviceClicked = onDeviceClicked,
    ) {

        val size = switchEntityList.size
        val maxItemsInEachColumn = remember(switchEntityList.size) {
            val columns = ((size + MAX_PER_COLUMN - 1) / MAX_PER_COLUMN).coerceAtLeast(1)
            (size + columns - 1) / columns
        }

        FlowColumn(
                modifier = Modifier,
                maxItemsInEachColumn = maxItemsInEachColumn
        ) {
            repeat(min(MAX_SWITCH_ENTITY, size)) { index ->
                val entity = switchEntityList[index]
                val enable = when (entity.allowedState) {
                    AllowedState.On -> true
                    AllowedState.Unavailable, AllowedState.Unknown, AllowedState.Off -> false
                    else -> return@repeat
                }

                Icon(
                        imageVector = Icons.Outlined.PowerSettingsNew,
                        contentDescription = null,
                        tint = if (enable) AppTheme.colors.colorAccent else AppTheme.colors.disable,
                        modifier = Modifier
                                .size(24.dp)
                                .clip(Shapes.image)
                                .clickable(onClick = { onInnerEntityClicked.invoke(entity) }),
                )
            }
        }
    }
}

@Composable
fun BaseDeviceCard(
        title: String,
        description: String?,
        imageUrl: String?,
        onDeviceClicked: () -> Unit,
        entityActionRow: (@Composable RowScope.() -> Unit)? = null,
) {
    Column(
            modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = DEVICE_CARD_ELEVATION, shape = DEVICE_CARD_SHAPE)
                    .background(color = AppTheme.colors.colorPrimary)
                    .clickable(onClick = { onDeviceClicked.invoke() })
                    .padding(Dimension.mediumMargin),
    ) {
        Row(
                modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = DEVICE_IMAGE_MAX_SIZE)
                        .clipToBounds(),
                verticalAlignment = Alignment.Top,
        ) {
            val hasActions = entityActionRow != null
            DeviceImage(
                    modifier = Modifier
                            .sizeIn(
                                    minWidth = DEVICE_IMAGE_MIN_SIZE,
                                    maxWidth = DEVICE_IMAGE_MAX_SIZE,
                                    minHeight = DEVICE_IMAGE_MIN_SIZE,
                                    maxHeight = DEVICE_IMAGE_MAX_SIZE,
                            )
                            .weight(1f, hasActions)
                            .aspectRatio(1f),
                    imageUrl = imageUrl
            )

            entityActionRow?.invoke(this@Row)
        }

        Spacer(modifier = Modifier.height(Dimension.smallMargin))

        Text(
                text = title,
                style = AppTheme.typography.captionMedium12,
                fontWeight = FontWeight.Bold,
                color = AppTheme.colors.colorAccent,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
        )

        Spacer(modifier = Modifier.height(2.dp))

        description?.let {
            Text(
                    text = it, // TODO temp
                    style = AppTheme.typography.captionMedium12,
                    color = AppTheme.colors.colorAccent,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun DeviceImage(
        imageUrl: String?,
        modifier: Modifier = Modifier,
) {
    if (imageUrl == null) {
        Icon(
                imageVector = Icons.Outlined.HelpOutline,
                contentDescription = null,
                modifier = modifier,
                tint = AppTheme.colors.textPrimary,
        )
        return
    }

    val context = LocalContext.current
    val sizePx = with(LocalDensity.current) { DEVICE_IMAGE_MAX_SIZE.roundToPx() }
    val fallbackPainter = rememberVectorPainter(Icons.Outlined.HelpOutline)
    val painter = rememberAsyncImagePainter(
            model = remember(imageUrl, sizePx) {
                ImageRequest.Builder(context)
                        .data(imageUrl)
                        .size(sizePx)
                        .crossfade(false)
                        .build()
            },
            placeholder = fallbackPainter,
            error = fallbackPainter,
    )

    Image(
            painter = painter,
            contentDescription = null,
            modifier = modifier,
    )
}

@Preview(showBackground = true, widthDp = 140)
@Composable
private fun BaseDeviceCardPreview(
        @PreviewParameter(ThemePreviewParameter::class) useDarkMode: Boolean,
) {
    AppTheme(useDarkMode) {
        Column(
                Modifier
                        .background(color = AppTheme.colors.colorSecondary)
                        .padding(Dimension.mediumMargin),
                verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            val device = PreviewUtils.previewDevices.first()
            BaseDeviceCard(
                    title = device.name,
                    description = null,
                    imageUrl = device.imageUrl,
                    onDeviceClicked = {},
            )

            SwitchCard(
                    title = device.name,
                    description = null,
                    imageUrl = device.imageUrl,
                    switchEntityList = device.entityList,
                    onDeviceClicked = {},
                    onInnerEntityClicked = {}
            )
        }
    }
}
