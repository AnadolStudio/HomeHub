package com.anadolstudio.homehub.feature.automation.automationDetail.presentation

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.anadolstudio.compose.ui.theme.AppTheme
import com.anadolstudio.compose.ui.theme.Dimmens
import com.anadolstudio.compose.ui.theme.Shapes
import com.anadolstudio.compose.ui.theme.largeShimmer
import com.anadolstudio.compose.ui.view.button.OutlineButtonLarge
import com.anadolstudio.compose.ui.view.button.PrimaryButtonLarge
import com.anadolstudio.compose.ui.view.snackbar.SnackbarHostState
import com.anadolstudio.compose.ui.view.text.LargeTextField
import com.anadolstudio.homehub.R
import com.anadolstudio.homehub.base.dialog.AlertDialog
import com.anadolstudio.homehub.di.viewmodel.assistedViewModel
import com.anadolstudio.homehub.di.viewmodel.rememberViewModelFactory
import com.anadolstudio.homehub.event.ObserveEvents
import com.anadolstudio.homehub.feature.automation.automationDetail.presentation.items.TriggerItem
import com.anadolstudio.homehub.feature.automation.automationDetail.presentation.logic_block.LogicBlockEditor
import com.anadolstudio.homehub.feature.automation.automationDetail.presentation.logic_block.LogicOperator
import com.anadolstudio.homehub.feature.automation.automationMode.presentation.AUTOMATION_MODE_RESULT_KEY
import com.anadolstudio.homehub.feature.automation.common.presentation.AutomationMode
import com.anadolstudio.homehub.feature.deviceDetail.demo.DemoDeviceDetailResult
import com.anadolstudio.homehub.feature.deviceDetail.entityPicker.EntityPickerResult
import com.anadolstudio.homehub.feature.home.domain.model.HomeAssistantDevice
import com.anadolstudio.homehub.feature.main.NavigationController
import com.anadolstudio.homehub.feature.sceneCreate.presentation.DeviceCardView
import com.anadolstudio.homehub.feature.sceneCreate.presentation.SCENE_DEVICE_SNAPSHOT_KEY
import com.anadolstudio.homehub.navigation.ObserveResultValue
import com.anadolstudio.utils.states.ProgressState

@Composable
internal fun AutomationDetailScreen(
        navigator: NavigationController,
        snackbarHostState: SnackbarHostState,
        automationId: String?,
) {
    val factory = rememberViewModelFactory<AutomationDetailViewModel.Factory>()
    val viewModel = assistedViewModel { factory.create(automationId) }
    val state by viewModel.stateFlow.collectAsState()
    ObserveEvents(viewModel.events, snackbarHostState, navigator)
    ObserveResultValue<AutomationMode>(navigator = navigator, key = AUTOMATION_MODE_RESULT_KEY, onValue = viewModel::onModeChanged)
    ObserveResultValue<HomeAssistantDevice>(navigator = navigator, key = AUTOMATION_TRIGGER_DEVICE_KEY, onValue = viewModel::onTriggerDeviceAdded)
    ObserveResultValue<HomeAssistantDevice>(navigator = navigator, key = SCENE_DEVICE_SNAPSHOT_KEY, onValue = viewModel::onServiceDeviceAdded)
    ObserveResultValue<Set<String>>(navigator = navigator, key = DemoDeviceDetailResult.KEY, onValue = viewModel::onServiceConfigured)
    ObserveResultValue<LogicOperator>(navigator = navigator, key = AUTOMATION_CONDITION_OPERATOR_KEY, onValue = viewModel::onConditionBlockChosen)
    ObserveResultValue<HomeAssistantDevice>(navigator = navigator, key = AUTOMATION_CONDITION_DEVICE_KEY, onValue = viewModel::onConditionDeviceChosen)
    // Must come after the device observers so the pending target/device is set before the entity set is applied.
    ObserveResultValue<Set<String>>(navigator = navigator, key = EntityPickerResult.KEY, onValue = viewModel::onEntitiesConfigured)

    AutomationDetailLayout(state = state, controller = viewModel)
}

@Composable
private fun AutomationDetailLayout(
        state: AutomationDetailScreenState,
        controller: AutomationDetailController,
) {
    var infoDialog by remember { mutableStateOf<InfoDialogContent?>(null) }

    fun showInfo(@StringRes titleRes: Int, @StringRes descriptionRes: Int) {
        infoDialog = InfoDialogContent(titleRes = titleRes, descriptionRes = descriptionRes)
    }

    Column(
            modifier = Modifier
                    .fillMaxSize()
                    .background(AppTheme.colors.colorSecondary)
                    .statusBarsPadding(),
    ) {
        Header(
                title = stringResource(R.string.automation_detail_title),
                onCloseClicked = controller::onCloseClicked,
        )

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item(key = "top_spacer") { Spacer(modifier = Modifier.height(16.dp)) }

            item(key = "name_field") {
                Row(
                        modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = Dimmens.mainMargin)
                                .shadow(2.dp, Shapes.largeShimmer)
                                .background(AppTheme.colors.colorPrimary)
                                .padding(vertical = Dimmens.smallMargin),
                ) {
                    LargeTextField(
                            value = state.name,
                            onValueChange = controller::onNameChanged,
                            labelText = stringResource(R.string.automation_detail_label_name),
                            isRequired = true,
                            showHint = false,
                            modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = Dimmens.mediumMargin),
                    )
                }
            }

            item(key = "name_to_mode_spacer") { Spacer(modifier = Modifier.height(24.dp)) }

            item(key = "mode_field") {
                Column(
                        modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = Dimmens.mainMargin),
                ) {
                    Text(
                            text = stringResource(R.string.automation_detail_label_mode),
                            style = AppTheme.typography.textMedium18,
                            color = AppTheme.colors.colorAccent,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                            modifier = Modifier
                                    .fillMaxWidth()
                                    .shadow(2.dp, Shapes.largeShimmer)
                                    .background(AppTheme.colors.colorPrimary)
                                    .clip(Shapes.largeShimmer)
                                    .clickable(onClick = controller::onModeClicked)
                                    .padding(horizontal = Dimmens.mediumMargin, vertical = Dimmens.mediumMargin),
                            verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                                modifier = Modifier.weight(1f),
                                text = stringResource(state.mode.titleRes),
                                style = AppTheme.typography.textMedium18,
                                color = AppTheme.colors.colorAccent,
                        )
                        Icon(
                                imageVector = Icons.Outlined.ChevronRight,
                                contentDescription = null,
                                tint = AppTheme.colors.colorAccent,
                        )
                    }
                }
            }

            item(key = "mode_to_triggers_spacer") { Spacer(modifier = Modifier.height(24.dp)) }

            triggersSection(
                    state = state,
                    controller = controller,
                    onInfoClicked = {
                        showInfo(
                                R.string.automation_detail_section_triggers,
                                R.string.automation_detail_info_triggers,
                        )
                    },
            )

            item(key = "triggers_to_conditions_spacer") { Spacer(modifier = Modifier.height(24.dp)) }

            conditionsSection(
                    state = state,
                    controller = controller,
                    onInfoClicked = {
                        showInfo(
                                R.string.automation_detail_section_conditions,
                                R.string.automation_detail_info_conditions,
                        )
                    },
            )

            item(key = "conditions_to_services_spacer") { Spacer(modifier = Modifier.height(24.dp)) }

            servicesSection(
                    state = state,
                    controller = controller,
                    onInfoClicked = {
                        showInfo(
                                R.string.automation_detail_section_services,
                                R.string.automation_detail_info_services,
                        )
                    },
            )

            item(key = "services_to_save_spacer") { Spacer(modifier = Modifier.height(24.dp)) }

            item(key = "save_button") {
                PrimaryButtonLarge(
                        modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = Dimmens.mainMargin),
                        elevation = ButtonDefaults.elevation(),
                        text = stringResource(R.string.automation_detail_button_save),
                        onClick = controller::onSaveClicked,
                        enabled = state.canSave,
                        loading = state.progressState is ProgressState.Loading,
                )
            }

            item(key = "bottom_spacer") {
                Spacer(
                        modifier = Modifier
                                .height(16.dp)
                                .navigationBarsPadding()
                )
            }
        }
    }

    infoDialog?.let { content ->
        AlertDialog(
                onDismissClick = { infoDialog = null },
                onConfirmClick = { infoDialog = null },
                title = stringResource(content.titleRes),
                text = stringResource(content.descriptionRes),
                confirmButtonText = stringResource(android.R.string.ok),
        )
    }
}

private data class InfoDialogContent(
        @StringRes val titleRes: Int,
        @StringRes val descriptionRes: Int,
)

private fun LazyListScope.triggersSection(
        state: AutomationDetailScreenState,
        controller: AutomationDetailController,
        onInfoClicked: () -> Unit,
) {
    sectionHeader(
            keyPrefix = "triggers",
            titleRes = R.string.automation_detail_section_triggers,
            addButtonRes = R.string.automation_detail_button_add_trigger,
            onAddClicked = controller::onAddTriggerClicked,
            onInfoClicked = onInfoClicked,
    )

    if (state.triggers.isEmpty()) {
        emptySection(keyPrefix = "triggers", textRes = R.string.automation_detail_empty_triggers)
    } else {
        items(items = state.triggers, key = { "trigger_${it.id}" }) { trigger ->
            TriggerItem(
                    modifier = Modifier
                            .padding(horizontal = Dimmens.mainMargin)
                            .padding(bottom = 12.dp)
                            .animateItem(),
                    title = trigger.title,
                    subtitle = trigger.subtitle,
                    image = trigger.image,
                    onClick = { controller.onTriggerEditClicked(trigger) },
                    onDeleteClicked = { controller.onTriggerDeleteClicked(trigger) },
            )
        }
    }
}

private fun LazyListScope.conditionsSection(
        state: AutomationDetailScreenState,
        controller: AutomationDetailController,
        onInfoClicked: () -> Unit,
) {
    sectionHeader(
            keyPrefix = "conditions",
            titleRes = R.string.automation_detail_section_conditions,
            addButtonRes = R.string.automation_detail_button_add_condition,
            onAddClicked = {},
            onInfoClicked = onInfoClicked,
            showAddButton = false,
    )

    item(key = "conditions_editor") {
        LogicBlockEditor(
                modifier = Modifier
                        .padding(horizontal = Dimmens.mainMargin)
                        .animateItem(),
                nodes = state.conditionTree,
                onDelete = controller::onConditionDeleted,
                onToggleCollapse = controller::onConditionCollapseToggled,
                onAddCondition = controller::onAddConditionClicked,
                onLeafClicked = controller::onConditionLeafClicked,
                onEntityValueChanged = controller::onConditionEntityValueChanged,
        )
    }
}

private fun LazyListScope.servicesSection(
        state: AutomationDetailScreenState,
        controller: AutomationDetailController,
        onInfoClicked: () -> Unit,
) {
    sectionHeader(
            keyPrefix = "services",
            titleRes = R.string.automation_detail_section_services,
            addButtonRes = R.string.automation_detail_button_add_service,
            onAddClicked = controller::onAddServiceClicked,
            onInfoClicked = onInfoClicked,
    )

    if (state.services.isEmpty()) {
        emptySection(keyPrefix = "services", textRes = R.string.automation_detail_empty_services)
    } else {
        items(items = state.services, key = { "service_${it.id}" }) { service ->
            DeviceCardView(
                    modifier = Modifier
                            .padding(horizontal = Dimmens.mainMargin)
                            .padding(bottom = 12.dp)
                            .animateItem(),
                    card = service,
                    onEditClicked = { controller.onServiceEditClicked(service) },
                    onRemoveClicked = { controller.onServiceDeleteClicked(service) },
                    onEntityRemoved = { entityState -> controller.onServiceEntityRemoved(service.id, entityState) },
            )
        }
    }
}

private fun LazyListScope.sectionHeader(
        keyPrefix: String,
        @StringRes titleRes: Int,
        @StringRes addButtonRes: Int,
        onAddClicked: () -> Unit,
        onInfoClicked: () -> Unit,
        showAddButton: Boolean = true,
) {
    item(key = "${keyPrefix}_title") {
        Row(
                modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Dimmens.mainMargin),
                verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                    text = stringResource(titleRes),
                    style = AppTheme.typography.textMedium18,
                    color = AppTheme.colors.colorAccent,
            )
            IconButton(onClick = onInfoClicked) {
                Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = stringResource(titleRes),
                        tint = AppTheme.colors.colorAccent,
                )
            }
        }
    }

    item(key = "${keyPrefix}_title_spacer") { Spacer(modifier = Modifier.height(8.dp)) }

    if (showAddButton) {
        item(key = "${keyPrefix}_add_button") {
            OutlineButtonLarge(
                    modifier = Modifier.padding(horizontal = Dimmens.mainMargin),
                    text = stringResource(addButtonRes),
                    onClick = onAddClicked,
                    icon = rememberVectorPainter(Icons.Outlined.Add),
            )
        }

        item(key = "${keyPrefix}_add_button_spacer") { Spacer(modifier = Modifier.height(12.dp)) }
    }
}

private fun LazyListScope.emptySection(
        keyPrefix: String,
        @StringRes textRes: Int,
) {
    item(key = "${keyPrefix}_empty") {
        Text(
                modifier = Modifier
                        .padding(horizontal = Dimmens.mainMargin)
                        .animateItem(),
                text = stringResource(textRes),
                style = AppTheme.typography.captionBook14,
                color = AppTheme.colors.textSecondary,
        )
    }
}

@Composable
private fun Header(title: String, onCloseClicked: () -> Unit) {
    Row(
            modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onCloseClicked) {
            Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = null,
                    tint = AppTheme.colors.colorAccent,
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
                text = title,
                style = AppTheme.typography.textMedium18,
                color = AppTheme.colors.colorAccent,
        )
    }
}