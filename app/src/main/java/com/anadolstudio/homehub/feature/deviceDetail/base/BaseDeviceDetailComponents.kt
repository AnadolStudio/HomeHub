package com.anadolstudio.homehub.feature.deviceDetail.base

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anadolstudio.compose.ui.theme.AppTheme
import com.anadolstudio.compose.ui.theme.Dimmens
import com.anadolstudio.compose.ui.theme.Shapes
import com.anadolstudio.compose.ui.theme.largeShimmer
import com.anadolstudio.compose.ui.theme.preview.ThemePreviewParameter
import com.anadolstudio.compose.ui.view.text.LargeTextField
import com.anadolstudio.homehub.R
import com.anadolstudio.homehub.base.view.HomeHubBrightnessRectangle
import com.anadolstudio.homehub.base.view.HomeHubColorRectangle
import com.anadolstudio.homehub.base.view.HomeHubColorTemperatureRectangle
import com.anadolstudio.homehub.base.view.HomeHubFilterChip
import com.anadolstudio.homehub.base.view.homeHubSwitchDefaults
import com.anadolstudio.homehub.base.view.rememberThrottled
import com.anadolstudio.homehub.feature.home.domain.model.AllowedDomain
import com.anadolstudio.homehub.feature.home.domain.model.HomeAssistantDevice
import com.anadolstudio.homehub.feature.home.domain.model.entity.EntityCategory
import com.anadolstudio.homehub.feature.home.domain.model.entity.HomeAssistantEntity
import com.anadolstudio.homehub.feature.home.domain.model.services.HomeAssistantService
import com.anadolstudio.homehub.feature.home.domain.model.services.LightService
import com.anadolstudio.homehub.feature.home.domain.model.services.SelectService
import com.anadolstudio.homehub.feature.home.domain.model.services.SimpleToggleableService
import com.anadolstudio.homehub.feature.home.domain.model.states.AllowedState
import com.anadolstudio.homehub.feature.home.domain.model.states.ClimateAttribute
import com.anadolstudio.homehub.feature.home.domain.model.states.HomeAssistantAttribute
import com.anadolstudio.homehub.feature.home.domain.model.states.HomeAssistantState
import com.anadolstudio.homehub.feature.home.domain.model.states.LightAttribute
import com.anadolstudio.homehub.feature.home.domain.model.states.LightEntityColorMode
import com.anadolstudio.homehub.feature.home.domain.model.states.NumberAttribute
import com.anadolstudio.homehub.feature.home.domain.model.states.SelectAttribute
import com.anadolstudio.homehub.feature.home.domain.model.states.SensorAttributes
import com.anadolstudio.homehub.feature.home.domain.model.states.SwitchAttribute
import com.anadolstudio.homehub.feature.home.presentation.PreviewUtils
import com.anadolstudio.homehub.feature.home.presentation.components.DeviceImageView
import com.anadolstudio.homehub.feature.main.NavigationController
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import java.time.OffsetDateTime
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject

private val DEVICE_DETAIL_IMAGE_SIZE = 120.dp

@OptIn(ExperimentalMaterialNavigationApi::class)
@Composable
internal fun BaseDeviceDetailScreenSetup(
        navigator: NavigationController,
        controller: BaseDeviceDetailController,
) {

    val sheetState = navigator.bottomSheetNavigator.navigatorSheetState

    LaunchedEffect(Unit) {
        snapshotFlow { sheetState.currentValue }
                .filter { it == ModalBottomSheetValue.Expanded }
                .first()
        controller.onSheetExpanded()

        snapshotFlow { sheetState.targetValue }
                .filter { it == ModalBottomSheetValue.Hidden }
                .first()
        controller.onSheetHidden()
    }

    BackHandler(enabled = sheetState.isVisible) { controller.onBackClicked() }
}

@Composable
internal fun DeviceContent(
        state: BaseDeviceDetailState<*>,
        device: HomeAssistantDevice,
        controller: BaseDeviceDetailController,
        onEditClicked: (() -> Unit)? = null,
        selectedEntitySet: Set<String> = device.allEntityList.map { it.entityId }.toSet(),
        belowMainInfo: @Composable ColumnScope.() -> Unit = {},
) {
    Column(
            modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Spacer(modifier = Modifier.statusBarsPadding())

        GeneralInfoSection(device = device, onEditClicked = onEditClicked)

        val selectedEntity = remember(selectedEntitySet) { selectedEntitySet }

        EntitySection(
                title = stringResource(R.string.device_detail_section_control),
                entities = device.targetEntityList.filter { it.allowedDomain != AllowedDomain.SENSOR },
                controller = controller,
                entityIdToTextFieldDataMap = state.entityIdToTextFieldDataMap,
                selectedEntity = selectedEntity
        )
        EntitySection(
                title = stringResource(R.string.device_detail_section_sensors),
                entities = device.targetEntityList.filter { it.allowedDomain == AllowedDomain.SENSOR },
                controller = controller,
                entityIdToTextFieldDataMap = state.entityIdToTextFieldDataMap,
                selectedEntity = selectedEntity
        )
        EntitySection(
                title = stringResource(R.string.device_detail_section_config),
                entities = device.configEntityList,
                controller = controller,
                entityIdToTextFieldDataMap = state.entityIdToTextFieldDataMap,
                selectedEntity = selectedEntity
        )
        EntitySection(
                title = stringResource(R.string.device_detail_section_diagnostic),
                entities = device.diagnosticEntityList,
                controller = controller,
                entityIdToTextFieldDataMap = state.entityIdToTextFieldDataMap,
                selectedEntity = selectedEntity
        )
        belowMainInfo.invoke(this)

        Spacer(modifier = Modifier.navigationBarsPadding())
    }
}

@Composable
private fun GeneralInfoSection(device: HomeAssistantDevice, onEditClicked: (() -> Unit)? = null) {
    val emptyValue = stringResource(R.string.device_detail_value_empty)
    SectionContainer(
            title = stringResource(R.string.device_detail_section_general_info),
            actionImage = Icons.Outlined.Edit,
            onActionClicked = onEditClicked
    ) {
        Spacer(modifier = Modifier.height(Dimmens.mainMargin))

        Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            DeviceImageView(
                    image = device.image,
                    modifier = Modifier.size(DEVICE_DETAIL_IMAGE_SIZE),
                    imageSize = DEVICE_DETAIL_IMAGE_SIZE,
            )
            Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                KeyValueRow(R.string.device_detail_field_name, device.name.ifBlank { emptyValue })
                KeyValueRow(R.string.device_detail_field_id, device.id)
                KeyValueRow(R.string.device_detail_field_model, device.model)
                KeyValueRow(R.string.device_detail_field_model_id, device.modelId)
                KeyValueRow(R.string.device_detail_field_manufacturer, device.manufacturer)
                KeyValueRow(R.string.device_detail_field_area, device.area?.name)
                KeyValueRow(R.string.device_detail_field_entity_count, device.allEntityList.size.toString())
            }
        }
    }
}

@Composable
private fun KeyValueRow(key: Int, value: String?) {
    if (value.isNullOrBlank()) return

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
                text = stringResource(key),
                style = AppTheme.typography.captionMedium16,
                fontWeight = FontWeight.Bold,
                color = AppTheme.colors.colorAccent,
        )
        Text(
                text = value,
                style = AppTheme.typography.captionMedium14,
                color = AppTheme.colors.colorAccent,
                overflow = TextOverflow.Ellipsis,
                maxLines = 2,
        )
    }
}

@Composable
private fun EntitySection(
        title: String,
        entities: List<HomeAssistantEntity<HomeAssistantAttribute>>,
        controller: BaseDeviceDetailController,
        entityIdToTextFieldDataMap: Map<String, TextFieldData>,
        selectedEntity: Set<String> = emptySet(),
) {
    if (entities.isEmpty()) return

    SectionContainer(title = title) {
        entities.forEach { entity ->
            val isChoose = selectedEntity.contains(entity.entityId)
            when (entity.state.attributes) {
                is LightAttribute -> LightEntityValueRow(
                        entity = entity as HomeAssistantEntity<LightAttribute>,
                        onEntityChanged = controller::onEntityChanged,
                        isChoose = isChoose
                )

                is NumberAttribute -> NumericEntityValueRow(
                        entity = entity as HomeAssistantEntity<NumberAttribute>,
                        textFieldData = entityIdToTextFieldDataMap[entity.entityId] ?: TextFieldData(""),
                        onNumericEntityChanged = controller::onNumericEntityChanged,
                        onNumericEntityFocusLost = controller::onNumericEntityFocusLost,
                        isChoose = isChoose
                )

                else -> EntityValueRow(
                        entity = entity,
                        onEntityChanged = controller::onEntityChanged,
                        isChoose = isChoose
                )
            }
        }
    }
}

@Composable
private fun LightEntityValueRow(
        entity: HomeAssistantEntity<LightAttribute>,
        onEntityChanged: ((HomeAssistantEntity<LightAttribute>, service: HomeAssistantService<*>) -> Unit),
        isChoose: Boolean = true,
) {
    val attributes = entity.state.attributes
    val drawableRes = entity.state.icon.drawableRes
    val displayTitle = entity.name

    val modifier = Modifier
            .fillMaxWidth()
            .setChoose(isChoose)
            .heightIn(min = LocalMinimumInteractiveComponentSize.current)
            .padding(vertical = Dimmens.extraSmallMargin, horizontal = Dimmens.smallMargin)

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimmens.smallMargin),
        ) {
            BaseDescription(drawableRes, displayTitle)

            SwitchAttributeControl(
                    entity = entity,
                    onEntityChanged = { _, _ -> onEntityChanged.invoke(entity, LightService.Toggle) }
            )
        }

        if (entity.state.allowedState is AllowedState.Unavailable) return@Column

        val colorPages = remember(attributes.colorModeList) {
            val list = attributes.colorModeList
            val hasHs = list.any { it is LightEntityColorMode.HS }
            list.filter { mode ->
                when (mode) {
                    is LightEntityColorMode.XY -> false
                    is LightEntityColorMode.RGB -> !hasHs
                    else -> true
                }
            }
        }

        val brightnessTrackColor = attributes.color?.let { Color(it) } ?: AppTheme.colors.disable
        val brightness = attributes.brightness ?: 0
        val brightnessPercent = (brightness * 100 / 255).coerceIn(0, 100)

        Spacer(modifier = Modifier.height(Dimmens.smallMargin))
        val totalPages = colorPages.size + 1 // первая страница — brightness
        val pagerState = rememberPagerState(pageCount = { totalPages })
        val pageModifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(Dimmens.smallMargin))

        HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth(),
                pageSpacing = Dimmens.smallMargin,
        ) { pageIndex ->
            if (pageIndex == 0) {
                HomeHubBrightnessRectangle(
                        value = brightnessPercent,
                        color = brightnessTrackColor,
                        onChanged = { percent -> onEntityChanged(entity, LightService.SetBrightness(percent)) },
                        modifier = pageModifier,
                )
            } else {
                ColorModePage(
                        mode = colorPages[pageIndex - 1],
                        onServiceCalled = { service -> onEntityChanged(entity, service) },
                        modifier = pageModifier,
                )
            }
        }

        if (totalPages > 1) {
            Spacer(modifier = Modifier.height(Dimmens.smallMargin))
            val scope = rememberCoroutineScope()

            val labels = buildList {
                add(stringResource(R.string.light_color_mode_brightness))
                colorPages.forEach { mode ->
                    when (mode) {
                        is LightEntityColorMode.HS -> stringResource(R.string.light_color_mode_hs)
                        is LightEntityColorMode.Temperature -> stringResource(R.string.light_color_mode_temperature)
                        is LightEntityColorMode.RGB -> stringResource(R.string.light_color_mode_rgb)
                        is LightEntityColorMode.XY -> stringResource(R.string.light_color_mode_xy)
                    }.also {
                        add(it)
                    }
                }
            }
            Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Dimmens.smallMargin, Alignment.CenterHorizontally),
            ) {
                labels.forEachIndexed { index, label ->
                    HomeHubFilterChip(
                            selected = pagerState.currentPage == index,
                            onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                            label = { Text(label) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ColorModePage(
        mode: LightEntityColorMode,
        onServiceCalled: (HomeAssistantService<*>) -> Unit,
        modifier: Modifier = Modifier,
) {
    // Не чаще одного раза в 300 мс, чтобы не спамить устройство во время свайпа по пикеру.
    val throttledOnService = rememberThrottled<HomeAssistantService<*>>(action = onServiceCalled)
    when (mode) {
        is LightEntityColorMode.HS -> HomeHubColorRectangle(
                hue = mode.hue.toFloat(),
                saturation = mode.saturation.toFloat(),
                onChanged = { h, s -> throttledOnService(LightService.SetHsColor(h, s)) },
                showIndicator = mode.hasValue,
                modifier = modifier
        )

        is LightEntityColorMode.RGB -> HomeHubColorRectangle(
                red = mode.red,
                green = mode.green,
                blue = mode.blue,
                onChanged = { r, g, b -> throttledOnService(LightService.SetRgbColor(r, g, b)) },
                showIndicator = mode.hasValue,
                modifier = modifier
        )

        is LightEntityColorMode.Temperature -> HomeHubColorTemperatureRectangle(
                value = mode.current,
                min = mode.min,
                max = mode.max,
                onChanged = { kelvin -> throttledOnService(LightService.SetColorTemp(kelvin)) },
                showIndicator = mode.hasValue,
                modifier = modifier
        )

        is LightEntityColorMode.XY -> Unit // отфильтровывается до пейджера
    }
}

@Composable
private fun NumericEntityValueRow(
        entity: HomeAssistantEntity<NumberAttribute>,
        textFieldData: TextFieldData,
        onNumericEntityChanged: (value: String, HomeAssistantEntity<NumberAttribute>) -> Unit,
        onNumericEntityFocusLost: (HomeAssistantEntity<NumberAttribute>) -> Unit,
        isChoose: Boolean = true,
) {
    val modifier = Modifier
            .fillMaxWidth()
            .setChoose(isChoose)
            .heightIn(min = LocalMinimumInteractiveComponentSize.current)
            .padding(vertical = Dimmens.extraSmallMargin, horizontal = Dimmens.smallMargin)

    Column(modifier = modifier) {
        NumberAttributeControl(
                entity = entity,
                textFieldData = textFieldData,
                onNumericEntityChanged = onNumericEntityChanged,
                onNumericEntityFocusLost = onNumericEntityFocusLost
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun NumberAttributeControl(
        entity: HomeAssistantEntity<NumberAttribute>,
        textFieldData: TextFieldData,
        onNumericEntityChanged: (value: String, HomeAssistantEntity<NumberAttribute>) -> Unit,
        onNumericEntityFocusLost: (HomeAssistantEntity<NumberAttribute>) -> Unit,
) {
    var isFocused by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val imeVisible = WindowInsets.isImeVisible
    LaunchedEffect(imeVisible) {
        if (!imeVisible && isFocused) {
            focusManager.clearFocus()
        }
    }

    val style = AppTheme.typography.captionBook16.copy(lineHeight = 16.sp)
    LargeTextField(
            value = textFieldData.value,
            onValueChange = { onNumericEntityChanged.invoke(it, entity) },
            labelText = entity.name,
            hintText = textFieldData.hintText,
            showHint = textFieldData.hintText.isNotBlank(),
            isError = textFieldData.hasError,
            enabled = textFieldData.enable,
            singleLine = true,
            textStyle = style,
            keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Done,
            ),
            leadingIcon = {
                Icon(
                        painter = painterResource(entity.state.icon.drawableRes),
                        contentDescription = null,
                        modifier = Modifier
                                .size(24.dp)
                                .offset(x = (-12).dp),
                        tint = AppTheme.colors.colorAccent
                )
            },
            trailingIcon = entity.state.attributes.unitOfMeasurement
                    .ifBlank { null }
                    ?.let { text ->
                        @Composable {
                            Text(color = AppTheme.colors.colorAccentAlternative, text = text, style = style)
                        }
                    },
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            modifier = Modifier.onFocusChanged { focusState ->
                if (isFocused && !focusState.isFocused && textFieldData.enable) {
                    onNumericEntityFocusLost.invoke(entity)
                }
                isFocused = focusState.isFocused
            },
    )
}

@Composable
private fun EntityValueRow(
        entity: HomeAssistantEntity<HomeAssistantAttribute>,
        onEntityChanged: ((HomeAssistantEntity<HomeAssistantAttribute>, service: HomeAssistantService<*>) -> Unit),
        isChoose: Boolean = true,
) {
    val attributes = entity.state.attributes
    val drawableRes = entity.state.icon.drawableRes
    val displayTitle = entity.name
    val displayValue = entityValueDisplay(entity)

    val rowModifier = Modifier
            .fillMaxWidth()
            .setChoose(isChoose)
            .heightIn(min = LocalMinimumInteractiveComponentSize.current)
            .padding(vertical = Dimmens.extraSmallMargin, horizontal = Dimmens.smallMargin)

    Row(
            modifier = rowModifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimmens.smallMargin),
    ) {
        BaseDescription(drawableRes, displayTitle)

        when (attributes) {
            is ClimateAttribute -> {}
            is SelectAttribute -> SelectAttributeControl(
                    entity = entity,
                    attributes = attributes,
                    onEntityChanged = onEntityChanged,
            )

            is SwitchAttribute -> SwitchAttributeControl(
                    entity = entity,
                    onEntityChanged = onEntityChanged,
            )

            is NumberAttribute -> Unit
            is LightAttribute -> Unit

            else -> Text(
                    text = displayValue,
                    style = AppTheme.typography.captionBook16,
                    color = AppTheme.colors.colorAccent,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun Modifier.setChoose(isChoose: Boolean): Modifier {
    return if (isChoose) {
        this.background(AppTheme.colors.colorPrimary)
    } else {
        this
                .shadow(2.dp, Shapes.largeShimmer)
                .background(AppTheme.colors.colorSecondary)
    }
}

private fun entityValueDisplay(entity: HomeAssistantEntity<HomeAssistantAttribute>): String {
    val rawValue = entity.state.allowedState.value

    return when (val attribute = entity.state.attributes) {
        is SensorAttributes -> {
            val unit = attribute.unitOfMeasurement
            if (unit.isBlank()) rawValue else "$rawValue $unit"
        }

        else -> rawValue
    }
}

@Composable
private fun SwitchAttributeControl(
        entity: HomeAssistantEntity<HomeAssistantAttribute>,
        onEntityChanged: (HomeAssistantEntity<HomeAssistantAttribute>, HomeAssistantService<*>) -> Unit,
) {
    val allowedState = entity.state.allowedState
    if (allowedState is AllowedState.Unavailable) {
        Icon(
                imageVector = Icons.Outlined.WifiOff,
                contentDescription = null,
                tint = AppTheme.colors.disable
        )
    } else {
        Switch(
                checked = entity.state.allowedState.toBooleanOrNull() ?: false,
                onCheckedChange = { value ->
                    val service = if (value) SimpleToggleableService.On else SimpleToggleableService.Off
                    onEntityChanged(entity, service)
                },
                colors = homeHubSwitchDefaults,
        )
    }
}

@Composable
private fun SelectAttributeControl(
        entity: HomeAssistantEntity<HomeAssistantAttribute>,
        attributes: SelectAttribute,
        onEntityChanged: (HomeAssistantEntity<HomeAssistantAttribute>, HomeAssistantService<*>) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val currentValue = entity.state.allowedState.value
    Box {
        val enable = entity.state.allowedState !is AllowedState.Unavailable

        Row(
                modifier = Modifier.clickable(enabled = enable) { expanded = true },
                verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                    text = currentValue,
                    style = AppTheme.typography.captionBook16,
                    color = AppTheme.colors.colorAccent,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
            )
            Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = AppTheme.colors.colorAccent,
            )
        }
        DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(AppTheme.colors.colorPrimary),
        ) {
            attributes.options.forEach { option ->
                DropdownMenuItem(
                        text = {
                            Text(
                                    text = option,
                                    style = AppTheme.typography.captionBook16,
                                    color = AppTheme.colors.colorAccent,
                            )
                        },
                        enabled = entity.state.allowedState !is AllowedState.Unavailable,
                        onClick = {
                            expanded = false
                            onEntityChanged(entity, SelectService.Option(option))
                        },
                        colors = MenuDefaults.itemColors(
                                textColor = AppTheme.colors.colorAccent,
                                leadingIconColor = AppTheme.colors.colorAccent,
                                trailingIconColor = AppTheme.colors.colorAccent,
                        ),
                )
            }
        }
    }
}

@Composable
private fun RowScope.BaseDescription(
        drawableRes: Int,
        displayTitle: String,
) {
    Icon(
            painter = painterResource(drawableRes),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = AppTheme.colors.colorAccent,
    )
    Text(
            modifier = Modifier.weight(1f),
            text = displayTitle,
            style = AppTheme.typography.captionBook16,
            color = AppTheme.colors.colorAccent,
            overflow = TextOverflow.Ellipsis,
    )
}

@Composable
internal fun SectionContainer(
        title: String,
        actionImage: ImageVector? = null,
        onActionClicked: (() -> Unit)? = null,
        content: @Composable () -> Unit = {},
) {
    Column(
            modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(Dimmens.mediumMargin))
                    .background(AppTheme.colors.colorPrimary)
                    .padding(Dimmens.mediumMargin),
            verticalArrangement = Arrangement.spacedBy(Dimmens.smallMargin),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                    modifier = Modifier.weight(1.0F),
                    text = title,
                    style = AppTheme.typography.textBook22.copy(),
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.colorAccent,
            )

            if (onActionClicked != null && actionImage != null) {
                IconButton(
                        onClick = onActionClicked,
                        modifier = Modifier,
                ) {
                    Icon(
                            imageVector = actionImage,
                            contentDescription = stringResource(R.string.device_detail_action_edit),
                            tint = AppTheme.colors.colorAccent,
                    )
                }
            }
        }

        content.invoke()
    }
}

private fun previewNumberEntity(
        value: String,
        min: Double? = 0.0,
        max: Double? = 100.0,
        unit: String = "%",
): HomeAssistantEntity<NumberAttribute> = HomeAssistantEntity(
        entityId = "number.preview_value",
        deviceId = "preview_device",
        name = "Яркость",
        platform = "mqtt",
        services = setOf("set_value"),
        entityCategory = EntityCategory.TARGET,
        state = HomeAssistantState(
                entityId = "number.preview_value",
                attributes = NumberAttribute(
                        jsonAttributes = JsonObject(emptyMap()),
                        friendlyName = "Яркость",
                        min = min,
                        max = max,
                        step = 1.0,
                        mode = "slider",
                        unitOfMeasurement = unit,
                ),
                allowedState = AllowedState.DigitState(value),
                lastChanged = OffsetDateTime.MIN,
                lastUpdated = null,
        ),
)


private val previewBaseDeviceDetailController = object : BaseDeviceDetailController {
    override fun onBackClicked() = Unit
    override fun onSheetExpanded() = Unit
    override fun onSheetHidden() = Unit
    override fun onEntityChanged(
            entity: HomeAssistantEntity<HomeAssistantAttribute>,
            service: HomeAssistantService<*>,
    ) = Unit

    override fun onNumericEntityChanged(value: String, entity: HomeAssistantEntity<NumberAttribute>) = Unit
    override fun onNumericEntityFocusLost(entity: HomeAssistantEntity<NumberAttribute>) = Unit
}

@Preview(showBackground = true, widthDp = 360, heightDp = 760)
@Composable
private fun DeviceContentPreview(
        @PreviewParameter(ThemePreviewParameter::class) useDarkMode: Boolean,
) {
    AppTheme(useDarkMode) {
        val device = PreviewUtils.previewDevices.first()
        val numberFields = device.allEntityList
                .filter { it.state.attributes is NumberAttribute }
                .associate { entity -> entity.entityId to TextFieldData(value = entity.state.allowedState.value) }
        val state = BaseDeviceDetailState(
                device = device,
                entityIdToTextFieldDataMap = numberFields,
                extraState = object : ExtraDeviceDetailScreenState {},
        )
        Box(
                modifier = Modifier
                        .background(AppTheme.colors.colorSecondary)
                        .padding(horizontal = Dimmens.mediumMargin),
        ) {
            DeviceContent(
                    state = state,
                    device = device,
                    controller = previewBaseDeviceDetailController,
                    onEditClicked = {},
            )
        }
    }
}
