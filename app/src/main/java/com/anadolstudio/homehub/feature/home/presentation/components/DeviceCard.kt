package com.anadolstudio.homehub.feature.home.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PowerSettingsNew
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.anadolstudio.compose.ui.theme.AppTheme
import com.anadolstudio.compose.ui.theme.Dimmens
import com.anadolstudio.compose.ui.theme.Shapes
import com.anadolstudio.compose.ui.theme.image
import com.anadolstudio.compose.ui.theme.preview.ThemePreviewParameter
import com.anadolstudio.homehub.feature.home.domain.model.DeviceImage
import com.anadolstudio.homehub.feature.home.domain.model.entity.HomeAssistantEntity
import com.anadolstudio.homehub.feature.home.domain.model.states.AllowedState
import com.anadolstudio.homehub.feature.home.domain.model.states.HomeAssistantAttribute
import com.anadolstudio.homehub.feature.home.domain.model.states.LightAttribute
import com.anadolstudio.homehub.feature.home.domain.model.states.SensorAttributes
import com.anadolstudio.homehub.feature.home.domain.model.states.SwitchAttribute
import com.anadolstudio.homehub.feature.home.presentation.PreviewUtils
import com.anadolstudio.homehub.util.toPainter

internal val DEVICE_IMAGE_MAX_SIZE = 64.dp
private val DEVICE_CARD_SHAPE = RoundedCornerShape(12.dp)
private val DEVICE_CARD_ELEVATION = 4.dp
private const val MAX_VISIBLE_ENTITIES = 3
private const val ENTITY_TEXT_MAX_LENGTH = 20
private val ENTITY_ICON_SIZE = 24.dp
private val ENTITY_TEXT_END_SPACE = 2.dp

@Composable
fun DeviceCard(
        title: String,
        image: DeviceImage?,
        entityList: List<HomeAssistantEntity<HomeAssistantAttribute>>,
        onInnerEntityClicked: (entity: HomeAssistantEntity<HomeAssistantAttribute>) -> Unit,
        onDeviceClicked: () -> Unit,
        modifier: Modifier = Modifier.width(IntrinsicSize.Min),
) {
    BaseDeviceCard(
            modifier = modifier,
            title = title,
            image = image,
            onDeviceClicked = onDeviceClicked,
    ) {
        val size = entityList.size

        Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(Dimmens.extraSmallMargin)
        ) {
            entityList.take(MAX_VISIBLE_ENTITIES).forEach { entity ->
                when (val attribute = entity.state.attributes) {
                    is LightAttribute -> {
                        BaseSwitchEntityItem(
                                entity = entity,
                                size = size,
                                onInnerEntityClicked = onInnerEntityClicked
                        )
                        attribute.color?.let { argb ->
                            val animatedColor by animateColorAsState(
                                    targetValue = Color(argb),
                                    label = "light_color",
                            )
                            Box(
                                    modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .border(1.dp, AppTheme.colors.colorAccentAlternative, CircleShape)
                                            .background(animatedColor),
                            )
                        }                    }

                    is SwitchAttribute -> BaseSwitchEntityItem(
                            entity = entity,
                            size = size,
                            onInnerEntityClicked = onInnerEntityClicked
                    )

                    is SensorAttributes -> EntityItem(
                            icon = entity.state.icon.toPainter(),
                            text = "${entity.state.allowedState.value} ${attribute.unitOfMeasurement}",
                            isEnable = true,
                    )

                    else -> EntityItem(
                            icon = entity.state.icon.toPainter(),
                            text = entity.state.allowedState.value,
                            isEnable = true,
                    )
                }
            }
        }
    }
}

@Composable
private fun BaseSwitchEntityItem(
        entity: HomeAssistantEntity<HomeAssistantAttribute>,
        size: Int,
        onInnerEntityClicked: (HomeAssistantEntity<HomeAssistantAttribute>) -> Unit,
) {
    EntityItem(
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
                else -> return
            },
            onClicked = { onInnerEntityClicked.invoke(entity) },
    )
}

@Composable
fun EntityItem(
        icon: Painter?,
        text: String?,
        isEnable: Boolean = true,
        onClicked: (() -> Unit)? = null,
) {
    Row(
            modifier = Modifier
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
                    modifier = Modifier.size(ENTITY_ICON_SIZE),
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
            Spacer(modifier = Modifier.width(ENTITY_TEXT_END_SPACE))
        }
    }
}

private fun HomeAssistantEntity<HomeAssistantAttribute>.gridDisplayText(deviceEntityCount: Int): String? =
        when (val attribute = state.attributes) {
            is SensorAttributes -> "${state.allowedState.value} ${attribute.unitOfMeasurement}"
            is LightAttribute, is SwitchAttribute -> name.takeIf { deviceEntityCount > 1 }
            else -> null
        }

/** Ширина, которую карточка реально хочет занять — по самому широкому видимому тексту сущности. */
internal fun deviceCardRequiredWidth(
        // TODO доработать, пока берет только текст, но есть разные виды entity
        entityList: List<HomeAssistantEntity<HomeAssistantAttribute>>,
        textMeasurer: TextMeasurer,
        textStyle: TextStyle,
        density: Density,
): Dp {
    val count = entityList.size
    val widestText = entityList
            .take(MAX_VISIBLE_ENTITIES)
            .mapNotNull { entity -> entity.gridDisplayText(count)?.take(ENTITY_TEXT_MAX_LENGTH) }
            .maxByOrNull { text -> text.length }

    val textWidth = widestText
            ?.let { text -> with(density) { textMeasurer.measure(text, textStyle).size.width.toDp() } }
            ?: 0.dp

    val entityColumnWidth = ENTITY_ICON_SIZE +
            if (widestText != null) Dimmens.extraSmallMargin + textWidth + ENTITY_TEXT_END_SPACE else 0.dp

    return Dimmens.smallMargin * 2 + DEVICE_IMAGE_MAX_SIZE + entityColumnWidth
}

@Composable
fun BaseDeviceCard(
        title: String,
        image: DeviceImage?,
        onDeviceClicked: () -> Unit,
        modifier: Modifier = Modifier.width(IntrinsicSize.Min),
        entityInformationRow: (@Composable RowScope.() -> Unit)? = null,
) {
    Column(
            modifier = modifier
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
                    modifier = Modifier.size(DEVICE_IMAGE_MAX_SIZE),
                    image = image,
                    imageSize = DEVICE_IMAGE_MAX_SIZE,
            )

            entityInformationRow?.invoke(this@Row)
        }

        Spacer(modifier = Modifier.height(Dimmens.smallMargin))

        Text(
                text = title,
                style = AppTheme.typography.captionMedium14,
                fontWeight = FontWeight.Bold,
                color = AppTheme.colors.colorAccent,
                minLines = 1,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
        )

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
                    image = device.image,
                    onDeviceClicked = {},
            )

            DeviceCard(
                    title = device.name,
                    image = device.image,
                    entityList = device.targetEntityList,
                    onDeviceClicked = {},
                    onInnerEntityClicked = {}
            )

            DeviceCard(
                    title = device.name,
                    image = device.image,
                    entityList = device.targetEntityList.take(1),
                    onDeviceClicked = {},
                    onInnerEntityClicked = {}
            )
        }
    }
}
