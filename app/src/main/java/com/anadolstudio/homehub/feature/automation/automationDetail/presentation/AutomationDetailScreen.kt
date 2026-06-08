package com.anadolstudio.homehub.feature.automation.automationDetail.presentation

import androidx.annotation.StringRes
import androidx.compose.foundation.background
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
import com.anadolstudio.homehub.di.viewmodel.daggerViewModel
import com.anadolstudio.homehub.event.ObserveEvents
import com.anadolstudio.homehub.feature.automation.automationDetail.presentation.items.ConditionItem
import com.anadolstudio.homehub.feature.automation.automationDetail.presentation.items.ServiceItem
import com.anadolstudio.homehub.feature.automation.automationDetail.presentation.items.TriggerItem
import com.anadolstudio.homehub.feature.main.NavigationController
import com.anadolstudio.utils.states.ProgressState

@Composable
internal fun AutomationDetailScreen(
        navigator: NavigationController,
        snackbarHostState: SnackbarHostState,
        viewModel: AutomationDetailViewModel = daggerViewModel(),
) {
    val state by viewModel.stateFlow.collectAsState()
    ObserveEvents(viewModel.events, snackbarHostState, navigator)

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

            item(key = "name_to_triggers_spacer") { Spacer(modifier = Modifier.height(24.dp)) }

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
                    onEditClicked = { controller.onTriggerEditClicked(trigger) },
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
            onAddClicked = controller::onAddConditionClicked,
            onInfoClicked = onInfoClicked,
    )

    if (state.conditions.isEmpty()) {
        emptySection(keyPrefix = "conditions", textRes = R.string.automation_detail_empty_conditions)
    } else {
        items(items = state.conditions, key = { "condition_${it.id}" }) { condition ->
            ConditionItem(
                    modifier = Modifier
                            .padding(horizontal = Dimmens.mainMargin)
                            .padding(bottom = 12.dp)
                            .animateItem(),
                    title = condition.title,
                    subtitle = condition.subtitle,
                    onEditClicked = { controller.onConditionEditClicked(condition) },
                    onDeleteClicked = { controller.onConditionDeleteClicked(condition) },
            )
        }
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
            ServiceItem(
                    modifier = Modifier
                            .padding(horizontal = Dimmens.mainMargin)
                            .padding(bottom = 12.dp)
                            .animateItem(),
                    title = service.title,
                    subtitle = service.subtitle,
                    onEditClicked = { controller.onServiceEditClicked(service) },
                    onDeleteClicked = { controller.onServiceDeleteClicked(service) },
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