package com.anadolstudio.template.feature.home.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowColumn
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.outlined.PanoramaFishEye
import androidx.compose.material.icons.outlined.PowerSettingsNew
import androidx.compose.material.icons.outlined.RemoveRedEye
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.anadolstudio.compose.ui.theme.AppTheme
import com.anadolstudio.compose.ui.theme.Dimmens
import com.anadolstudio.compose.ui.theme.Shapes
import com.anadolstudio.compose.ui.theme.image
import com.anadolstudio.compose.ui.theme.preview.ThemePreviewParameter
import com.anadolstudio.template.feature.home.domain.model.DeviceImage
import com.anadolstudio.template.feature.home.domain.model.entity.HomeAssistantEntity
import com.anadolstudio.template.feature.home.domain.model.states.AllowedState
import com.anadolstudio.template.feature.home.domain.model.states.HomeAssistantAttribute
import com.anadolstudio.template.feature.home.domain.model.states.LightAttribute
import com.anadolstudio.template.feature.home.domain.model.states.SensorAttributes
import com.anadolstudio.template.feature.home.domain.model.states.SwitchAttribute
import com.anadolstudio.template.feature.home.presentation.PreviewUtils
import com.anadolstudio.template.util.toPainter

private val DEVICE_IMAGE_MAX_SIZE = 70.dp
private val DEVICE_CARD_SHAPE = RoundedCornerShape(12.dp)
private val DEVICE_CARD_ELEVATION = 4.dp
private const val MAX_SWITCH_ENTITY = 6
private const val MAX_PER_COLUMN = 3
private const val ENTITY_TEXT_MAX_LENGTH = 15

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DeviceCard(
        title: String,
        description: String?,
        image: DeviceImage?,
        entityList: List<HomeAssistantEntity<HomeAssistantAttribute>>,
        onInnerEntityClicked: (entity: HomeAssistantEntity<HomeAssistantAttribute>) -> Unit,
        onDeviceClicked: () -> Unit,
) {
    BaseDeviceCard(
            title = title,
            description = description,
            image = image,
            onDeviceClicked = onDeviceClicked,
    ) {
        val size = entityList.size
        val maxItemsInEachColumn = remember(entityList.size) {
            val columns = ((size + MAX_PER_COLUMN - 1) / MAX_PER_COLUMN).coerceAtLeast(1)
            (size + columns - 1) / columns
        }

        FlowColumn(
                modifier = Modifier.fillMaxWidth(),
                maxItemsInEachColumn = maxItemsInEachColumn,
        ) {
            entityList.forEach { entity ->
                when (val attribute = entity.state.attributes) {
                    is LightAttribute, is SwitchAttribute -> EntityItem(
                            icon = when (entity.state.allowedState) {
                                AllowedState.On,
                                AllowedState.Unknown,
                                AllowedState.Off -> Icons.Outlined.PowerSettingsNew

                                else -> Icons.Outlined.WifiOff
                            }.let { rememberVectorPainter(it) },
                            text = entity.name.takeIf { size > 1 },
                            isEnable = when (entity.state.allowedState) {
                                AllowedState.On -> true
                                AllowedState.Unavailable, AllowedState.Unknown, AllowedState.Off -> false
                                else -> return@forEach
                            },
                            onClicked = { onInnerEntityClicked.invoke(entity) },
                    )

                    is SensorAttributes -> {
                        EntityItem(
                                icon = attribute.icon
                                        ?.toPainter()
                                        ?: let {
                                            val icon = when (entity.state.allowedState) {
                                                is AllowedState.On -> Icons.Outlined.RemoveRedEye
                                                is AllowedState.Off -> Icons.Outlined.PanoramaFishEye
                                                else -> Icons.AutoMirrored.Outlined.HelpOutline
                                            }
                                            rememberVectorPainter(icon)
                                        },
                                text = "${entity.state.allowedState.value} ${attribute.unitOfMeasurement}",
                                isEnable = true,
                        )
                    }

                    else -> Unit
                }
            }
        }
    }
}

@Composable
fun ColumnScope.EntityItem(
        icon: Painter?,
        text: String?,
        isEnable: Boolean = true,
        onClicked: (() -> Unit)? = null,
) {
    Row(
            modifier = Modifier
                    .weight(1f, false)
                    .heightIn(min = 24.dp)
                    .padding(2.dp)
                    .clip(Shapes.image)
                    .clickable(enabled = onClicked != null, onClick = { onClicked?.invoke() }),
            verticalAlignment = Alignment.CenterVertically
    ) {
        icon?.let {
            Icon(
                    painter = it,
                    contentDescription = null,
                    tint = if (isEnable) AppTheme.colors.colorAccent else AppTheme.colors.disable,
                    modifier = Modifier.size(24.dp),
            )

        }

        if (icon != null && text != null) {
            Spacer(modifier = Modifier.width(Dimmens.extraSmallMargin))
        }

        text?.let {
            val displayText = it.take(ENTITY_TEXT_MAX_LENGTH)
            val style = AppTheme.typography.captionMedium12
            val textMeasurer = rememberTextMeasurer()
            val density = LocalDensity.current

            val textWidth = remember(displayText, style, density) {
                with(density) {
                    textMeasurer.measure(text = displayText, style = style).size.width.toDp()
                }
            }
            Text(
                    modifier = Modifier.widthIn(min = textWidth),
                    text = displayText,
                    style = style,
                    color = AppTheme.colors.colorAccent,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.width(2.dp))
        }
    }
}

@Composable
fun BaseDeviceCard(
        title: String,
        description: String?,
        image: DeviceImage?,
        onDeviceClicked: () -> Unit,
        entityInformationRow: (@Composable RowScope.() -> Unit)? = null,
) {
    Column(
            modifier = Modifier
                    .width(IntrinsicSize.Min)
                    .shadow(elevation = DEVICE_CARD_ELEVATION, shape = DEVICE_CARD_SHAPE)
                    .background(color = AppTheme.colors.colorPrimary)
                    .clickable(onClick = { onDeviceClicked.invoke() })
                    .padding(Dimmens.smallMargin),
    ) {
        Row(
                modifier = Modifier
                        .heightIn(min = DEVICE_IMAGE_MAX_SIZE)
                        .clipToBounds(),
                verticalAlignment = Alignment.Top,
        ) {
            DeviceImageView(
                    modifier = Modifier
                            .heightIn(max = DEVICE_IMAGE_MAX_SIZE)
                            .aspectRatio(1f),
                    image = image
            )

            entityInformationRow?.invoke(this@Row)
        }

        Spacer(modifier = Modifier.height(Dimmens.smallMargin))

        Text(
                text = title,
                style = AppTheme.typography.captionMedium14,
                fontWeight = FontWeight.Bold,
                color = AppTheme.colors.colorAccent,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
        )

        Spacer(modifier = Modifier.height(2.dp))

        description?.let {
            Text(
                    text = it,
                    style = AppTheme.typography.captionMedium12,
                    color = AppTheme.colors.colorAccent,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 300)
@Composable
private fun BaseDeviceCardPreview(
        @PreviewParameter(ThemePreviewParameter::class) useDarkMode: Boolean,
) {
    AppTheme(useDarkMode) {
        Column(
                Modifier
                        .background(color = AppTheme.colors.colorSecondary)
                        .padding(Dimmens.mediumMargin),
                verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            val device = PreviewUtils.previewDevices.first()
            BaseDeviceCard(
                    title = device.name,
                    description = null,
                    image = device.image,
                    onDeviceClicked = {},
            )

            DeviceCard(
                    title = device.name,
                    description = null,
                    image = device.image,
                    entityList = device.targetEntityList,
                    onDeviceClicked = {},
                    onInnerEntityClicked = {}
            )

            DeviceCard(
                    title = device.name,
                    description = null,
                    image = device.image,
                    entityList = device.targetEntityList.take(1),
                    onDeviceClicked = {},
                    onInnerEntityClicked = {}
            )
        }
    }
}
