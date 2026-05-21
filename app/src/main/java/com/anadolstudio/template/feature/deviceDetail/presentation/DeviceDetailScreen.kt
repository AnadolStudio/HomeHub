package com.anadolstudio.template.feature.deviceDetail.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.anadolstudio.compose.ui.theme.AppTheme
import com.anadolstudio.compose.ui.theme.Dimmens
import com.anadolstudio.compose.ui.theme.Shapes
import com.anadolstudio.compose.ui.theme.largeShimmer
import com.anadolstudio.compose.ui.theme.preview.ThemePreviewParameter
import com.anadolstudio.compose.ui.view.snackbar.SnackbarHostState
import com.anadolstudio.compose.ui.view.text.LargeTextField
import com.anadolstudio.template.R
import com.anadolstudio.template.base.view.HomeHubLoader
import com.anadolstudio.template.base.view.homeHubSwitchDefaults
import com.anadolstudio.template.di.viewmodel.assistedViewModel
import com.anadolstudio.template.di.viewmodel.rememberViewModelFactory
import com.anadolstudio.template.event.ObserveEvents
import com.anadolstudio.template.feature.automation.common.presentation.AutomationItem
import com.anadolstudio.template.feature.home.domain.model.AllowedDomain
import com.anadolstudio.template.feature.home.domain.model.DeviceImage
import com.anadolstudio.template.feature.home.domain.model.HomeAssistantDevice
import com.anadolstudio.template.feature.home.domain.model.entity.EntityCategory
import com.anadolstudio.template.feature.home.domain.model.entity.HomeAssistantEntity
import com.anadolstudio.template.feature.home.domain.model.services.HomeAssistantService
import com.anadolstudio.template.feature.home.domain.model.services.SelectService
import com.anadolstudio.template.feature.home.domain.model.services.SimpleToggleableService
import com.anadolstudio.template.feature.home.domain.model.states.AllowedState
import com.anadolstudio.template.feature.home.domain.model.states.AutomationAttributes
import com.anadolstudio.template.feature.home.domain.model.states.ClimateAttribute
import com.anadolstudio.template.feature.home.domain.model.states.HomeAssistantAttribute
import com.anadolstudio.template.feature.home.domain.model.states.HomeAssistantState
import com.anadolstudio.template.feature.home.domain.model.states.LightAttribute
import com.anadolstudio.template.feature.home.domain.model.states.NumberAttribute
import com.anadolstudio.template.feature.home.domain.model.states.SceneAttributes
import com.anadolstudio.template.feature.home.domain.model.states.SelectAttribute
import com.anadolstudio.template.feature.home.domain.model.states.SensorAttributes
import com.anadolstudio.template.feature.home.domain.model.states.SwitchAttribute
import com.anadolstudio.template.feature.home.presentation.components.DeviceImageView
import com.anadolstudio.template.feature.main.NavigationController
import com.anadolstudio.template.util.toPainter
import com.anadolstudio.utils.states.ProgressState
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.JsonObject

private val DEVICE_DETAIL_IMAGE_SIZE = 120.dp

internal object DeviceDetailResult {
    const val KEY = "deviceDetailResult"
}

@OptIn(ExperimentalMaterialNavigationApi::class)
@Composable
internal fun DeviceDetailScreen(
        navigator: NavigationController,
        snackbarHostState: SnackbarHostState,
        device: HomeAssistantDevice,
) {
    // Workaround for accompanist navigation-material: при анимации перехода между bot-sheet'ами
    // sheetContent может пересоставиться с уже-DESTROYED NavBackStackEntry, и viewModel(...)
    // упадёт через LocalViewModelStoreOwner.current. Гард в MainGraph не ловит этот случай
    // (внутренний RecomposeScope пересоставляется независимо), поэтому проверяем здесь.
    if (LocalLifecycleOwner.current.lifecycle.currentState == Lifecycle.State.DESTROYED) return

    val factory = rememberViewModelFactory<DeviceDetailViewModel.Factory>()
    val viewModel = assistedViewModel { factory.create(device) }

    val state by viewModel.stateFlow.collectAsState()
    ObserveEvents(viewModel.events, snackbarHostState, navigator)

    val sheetState = navigator.bottomSheetNavigator.navigatorSheetState
    LaunchedEffect(Unit) {
        snapshotFlow { sheetState.currentValue }
                .filter { it == ModalBottomSheetValue.Expanded }
                .first()
        viewModel.onSheetExpanded()
    }

    BackHandler(enabled = sheetState.isVisible) {
        navigator.navigateUp()
    }

    val previousBackStackEntry = remember { navigator.previousBackStackEntry }

    DisposableEffect(Unit) {
        onDispose {
            val currentDevice = viewModel.stateFlow.value.device
            val result = ArrayList(currentDevice.targetEntityList + currentDevice.configEntityList)
            previousBackStackEntry?.savedStateHandle?.set(DeviceDetailResult.KEY, result)
        }
    }

    DeviceDetailLayout(state = state, controller = viewModel)
}

@Composable
private fun DeviceDetailLayout(
        state: DeviceDetailScreenState,
        controller: DeviceDetailController,
) {
    Box(
            modifier = Modifier
                    .fillMaxSize()
                    .background(
                            color = AppTheme.colors.colorSecondary,
                            shape = RoundedCornerShape(topStart = Dimmens.mainMargin, topEnd = Dimmens.mainMargin)
                    )
                    .padding(horizontal = 16.dp),
    ) {
        when (state.progressState) {
            is ProgressState.Loading,
            is ProgressState.LoadingFromError -> LoadingContent()

            is ProgressState.Error -> ErrorContent(onRetryClicked = controller::onRetryClicked)
            else -> DeviceContent(
                    state = state,
                    device = state.device,
                    controller = controller,
            )
        }
    }
}

@Composable
private fun LoadingContent() {
    Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
    ) {
        HomeHubLoader()
    }
}

@Composable
private fun ErrorContent(onRetryClicked: () -> Unit) {
    Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                    text = stringResource(R.string.device_detail_error_generic),
                    style = AppTheme.typography.textBook18,
                    color = AppTheme.colors.colorAccent,
                    textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = onRetryClicked) {
                Text(
                        text = stringResource(R.string.device_detail_retry),
                        style = AppTheme.typography.textMedium18,
                        color = AppTheme.colors.template,
                )
            }
        }
    }
}

@Composable
private fun DeviceContent(
        state: DeviceDetailScreenState,
        device: HomeAssistantDevice,
        controller: DeviceDetailController,
) {
    Column(
            modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Spacer(modifier = Modifier.statusBarsPadding())

        GeneralInfoSection(device = device)

        EntitySection(
                title = stringResource(R.string.device_detail_section_control),
                entities = device.targetEntityList.filter { it.allowedDomain != AllowedDomain.SENSOR },
                controller = controller,
                entityIdToTextFieldDataMap = state.entityIdToTextFieldDataMap
        )
        EntitySection(
                title = stringResource(R.string.device_detail_section_sensors),
                entities = device.targetEntityList.filter { it.allowedDomain == AllowedDomain.SENSOR },
                controller = controller,
                entityIdToTextFieldDataMap = state.entityIdToTextFieldDataMap
        )
        EntitySection(
                title = stringResource(R.string.device_detail_section_config),
                entities = device.configEntityList,
                controller = controller,
                entityIdToTextFieldDataMap = state.entityIdToTextFieldDataMap
        )
        EntitySection(
                title = stringResource(R.string.device_detail_section_diagnostic),
                entities = device.diagnosticEntityList,
                controller = controller,
                entityIdToTextFieldDataMap = state.entityIdToTextFieldDataMap
        )
        if (state.automationList.isNotEmpty()) {
            AutomationsSection(automations = state.automationList)
        }
        if (state.sceneList.isNotEmpty()) {
            ScenesSection(scenes = state.sceneList)
        }
        HistorySection(
                historyState = state.historyState,
                onRetryClicked = controller::onHistoryRetryClicked,
        )

        Spacer(modifier = Modifier.navigationBarsPadding())
    }
}

@Composable
private fun GeneralInfoSection(device: HomeAssistantDevice) {
    val emptyValue = stringResource(R.string.device_detail_value_empty)
    SectionContainer(title = stringResource(R.string.device_detail_section_general_info)) {
        Spacer(modifier = Modifier.height(Dimmens.mainMargin))

        Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            DeviceImageView(
                    image = when (val image = device.image) {
                        is DeviceImage.HaIconType -> image.copy(haIcon = image.haIcon.copy(tint = null))
                        else -> device.image
                    },
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
private fun EntitySection(
        title: String,
        entities: List<HomeAssistantEntity<HomeAssistantAttribute>>,
        controller: DeviceDetailController,
        entityIdToTextFieldDataMap: Map<String, TextFieldData>,
) {
    if (entities.isEmpty()) return

    SectionContainer(title = title) {
        entities.forEach { entity ->
            when (entity.state.attributes) {
                //                is LightAttribute -> TODO()
                is NumberAttribute -> NumericEntityValueRow(
                        entity = entity as HomeAssistantEntity<NumberAttribute>,
                        textFieldData = entityIdToTextFieldDataMap[entity.entityId] ?: TextFieldData(""),
                        onNumericEntityChanged = controller::onNumericEntityChanged,
                        onNumericEntityFocusLost = controller::onNumericEntityFocusLost,
                )

                else -> EntityValueRow(
                        entity = entity,
                        onEntityChanged = controller::onEntityChanged,
                        onEntityClick = if (entity.state.attributes is LightAttribute) { // TODO
                            { controller.onLightEntityClicked(entity) }
                        } else {
                            null
                        },
                )
            }
        }
    }
}

@Composable
private fun NumericEntityValueRow(
        entity: HomeAssistantEntity<NumberAttribute>,
        textFieldData: TextFieldData,
        onNumericEntityChanged: (value: String, HomeAssistantEntity<NumberAttribute>) -> Unit,
        onNumericEntityFocusLost: (HomeAssistantEntity<NumberAttribute>) -> Unit,
) {
    val modifier = Modifier
            .fillMaxWidth()
            .clip(Shapes.largeShimmer)
            .background(AppTheme.colors.colorPrimary)
            .heightIn(min = LocalMinimumInteractiveComponentSize.current)
            .padding(vertical = Dimmens.smallMargin, horizontal = Dimmens.smallMargin)

    Column(modifier = modifier) {
        NumberAttributeControl(
                entity = entity,
                textFieldData = textFieldData,
                onNumericEntityChanged = onNumericEntityChanged,
                onNumericEntityFocusLost = onNumericEntityFocusLost
        )
    }
}

@Composable
private fun EntityValueRow(
        entity: HomeAssistantEntity<HomeAssistantAttribute>,
        onEntityChanged: ((HomeAssistantEntity<HomeAssistantAttribute>, service: HomeAssistantService<*>) -> Unit),
        onEntityClick: (() -> Unit)? = null,
) {
    val attributes = entity.state.attributes
    val drawableRes = entity.state.icon.drawableRes
    val displayTitle = entity.name
    val displayValue = entityValueDisplay(entity)

    val rowModifier = Modifier
            .fillMaxWidth()
            .clip(Shapes.largeShimmer)
            .background(AppTheme.colors.colorPrimary)
            .clickable(enabled = onEntityClick != null, onClick = { onEntityClick?.invoke() })
            .heightIn(min = LocalMinimumInteractiveComponentSize.current)
            .padding(vertical = Dimmens.smallMargin, horizontal = Dimmens.smallMargin)

    Row(
            modifier = rowModifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimmens.smallMargin),
    ) {
        BaseDescription(drawableRes, attributes, displayTitle)

        when (attributes) {
            is ClimateAttribute -> {}
            is LightAttribute -> {}

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
private fun RowScope.BaseDescription(
        drawableRes: Int,
        attributes: HomeAssistantAttribute,
        displayTitle: String,
) {
    Icon(
            painter = painterResource(drawableRes),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = (attributes as? LightAttribute)?.color?.let { Color(it) } ?: AppTheme.colors.colorAccent,
    )
    Text(
            modifier = Modifier.weight(1f),
            text = displayTitle,
            style = AppTheme.typography.captionBook16,
            color = AppTheme.colors.colorAccent,
            overflow = TextOverflow.Ellipsis,
    )
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
                isFocused = focusState.isFocused
                if (!isFocused) onNumericEntityFocusLost.invoke(entity)
            },
    )
}

@Composable
private fun SwitchAttributeControl(
        entity: HomeAssistantEntity<HomeAssistantAttribute>,
        onEntityChanged: (HomeAssistantEntity<HomeAssistantAttribute>, HomeAssistantService<*>) -> Unit,
) {
    Switch(
            modifier = Modifier,
            checked = entity.state.allowedState.toBooleanOrNull() ?: false,
            onCheckedChange = { value ->
                val service = if (value) SimpleToggleableService.On else SimpleToggleableService.Off
                onEntityChanged(entity, service)
            },
            colors = homeHubSwitchDefaults,
    )
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
        Row(
                modifier = Modifier.clickable { expanded = true },
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
private fun AutomationsSection(automations: List<HomeAssistantEntity<AutomationAttributes>>) {
    SectionContainer(title = stringResource(R.string.device_detail_section_automations)) {
        automations.forEach { automation ->
            AutomationItem(
                    title = automation.name,
                    icon = automation.state.icon.toPainter(),
            )
        }
    }
}

@Composable
private fun ScenesSection(scenes: List<HomeAssistantEntity<SceneAttributes>>) {
    SectionContainer(title = stringResource(R.string.device_detail_section_scenes)) {
        scenes.forEach { scene ->
            AutomationItem(
                    title = scene.name,
                    icon = scene.state.icon.toPainter(),
            )
        }
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
private fun HistorySection(
        historyState: HistoryState,
        onRetryClicked: () -> Unit,
) {
    SectionContainer(title = stringResource(R.string.device_detail_section_history)) {
        when (historyState.progressState) {
            is ProgressState.Loading,
            is ProgressState.LoadingFromError,
            is ProgressState.Refresh -> Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(
                        modifier = Modifier.size(28.dp),
                        color = AppTheme.colors.colorAccent,
                )
            }

            is ProgressState.Error -> Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                        text = stringResource(R.string.device_detail_history_error),
                        style = AppTheme.typography.captionBook14,
                        color = AppTheme.colors.textSecondary,
                        textAlign = TextAlign.Center,
                )
                TextButton(onClick = onRetryClicked) {
                    Text(
                            text = stringResource(R.string.device_detail_retry),
                            style = AppTheme.typography.captionBook14,
                            color = AppTheme.colors.template,
                    )
                }
            }

            is ProgressState.Content -> {
                if (historyState.entries.isEmpty()) {
                    Text(
                            text = stringResource(R.string.device_detail_history_empty),
                            style = AppTheme.typography.captionBook14,
                            color = AppTheme.colors.textSecondary,
                    )
                } else {
                    historyState.entries.forEach { entry -> HistoryEntryRow(entry = entry) }
                    if (historyState.entries.size < historyState.totalCount) {
                        Divider(
                                modifier = Modifier.padding(bottom = Dimmens.smallMargin),
                                color = AppTheme.colors.colorSecondary
                        )
                        Text(
                                text = stringResource(
                                        R.string.device_detail_history_truncated_format,
                                        historyState.entries.size,
                                        historyState.totalCount,
                                ),
                                style = AppTheme.typography.captionBook14,
                                color = AppTheme.colors.textSecondary,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryEntryRow(entry: DeviceHistoryEntry) {
    val formatter = remember { DateTimeFormatter.ofPattern("HH:mm dd.MM.yyyy", Locale.getDefault()) }
    val formattedTime = remember(entry.timestamp) {
        entry.timestamp.atZoneSameInstant(ZoneId.systemDefault()).format(formatter)
    }

    Row(
            modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = LocalMinimumInteractiveComponentSize.current)
                    .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
    ) {
        if (entry.drawableRes != null) {
            Icon(
                    painter = painterResource(entry.drawableRes),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = AppTheme.colors.colorAccent,
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                    text = entry.friendlyName,
                    style = AppTheme.typography.captionBook14,
                    color = AppTheme.colors.colorAccent,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
            )
            Text(
                    text = formattedTime,
                    style = AppTheme.typography.captionBook14,
                    color = AppTheme.colors.textSecondary,
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
                text = entry.value,
                style = AppTheme.typography.captionMedium14,
                color = AppTheme.colors.colorAccent,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun SectionContainer(
        title: String,
        content: @Composable () -> Unit,
) {
    Column(
            modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(Dimmens.mediumMargin))
                    .background(AppTheme.colors.colorPrimary)
                    .padding(Dimmens.mediumMargin)
                    .padding(bottom = Dimmens.extraSmallMargin),
            verticalArrangement = Arrangement.spacedBy(Dimmens.extraSmallMargin),
    ) {
        Text(
                text = title,
                style = AppTheme.typography.textBook22,
                fontWeight = FontWeight.Bold,
                color = AppTheme.colors.colorAccent,
        )
        content.invoke()
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

// region Previews

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

@Preview(showBackground = true, widthDp = 320)
@Composable
private fun NumberAttributeControlPreview(
        @PreviewParameter(ThemePreviewParameter::class) useDarkMode: Boolean,
) {
    AppTheme(useDarkMode) {
        val entity = previewNumberEntity(value = "42")
        Column(
                modifier = Modifier
                        .background(AppTheme.colors.colorSecondary)
                        .fillMaxWidth()
                        .padding(Dimmens.mediumMargin),
                verticalArrangement = Arrangement.spacedBy(Dimmens.smallMargin),
        ) {
            NumericEntityValueRow(
                    entity = entity,
                    textFieldData = TextFieldData(""),
                    onNumericEntityChanged = { _, _ -> },
                    onNumericEntityFocusLost = { },
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 320)
@Composable
private fun NumberAttributeControlOutOfRangePreview(
        @PreviewParameter(ThemePreviewParameter::class) useDarkMode: Boolean,
) {
    AppTheme(useDarkMode) {
        // 150 при max=100 — состояние ошибки (красная подсветка поля)
        val entity = previewNumberEntity(value = "150")
        Column(
                modifier = Modifier
                        .background(AppTheme.colors.colorSecondary)
                        .padding(Dimmens.mediumMargin),
                verticalArrangement = Arrangement.spacedBy(Dimmens.smallMargin),
        ) {
            NumericEntityValueRow(
                    entity = entity,
                    textFieldData = TextFieldData(entity.state.allowedState.value, true),
                    onNumericEntityChanged = { _, _ -> },
                    onNumericEntityFocusLost = { },
            )
        }
    }
}

// endregion
