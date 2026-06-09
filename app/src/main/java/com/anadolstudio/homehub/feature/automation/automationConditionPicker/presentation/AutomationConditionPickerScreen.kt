package com.anadolstudio.homehub.feature.automation.automationConditionPicker.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.anadolstudio.compose.ui.theme.AppTheme
import com.anadolstudio.compose.ui.theme.Dimmens
import com.anadolstudio.compose.ui.view.button.OutlineButtonLarge
import com.anadolstudio.compose.ui.view.snackbar.SnackbarHostState
import com.anadolstudio.homehub.R
import com.anadolstudio.homehub.di.viewmodel.daggerViewModel
import com.anadolstudio.homehub.event.ObserveEvents
import com.anadolstudio.homehub.feature.automation.automationDetail.presentation.logic_block.LogicOperator
import com.anadolstudio.homehub.feature.main.NavigationController

@Composable
internal fun AutomationConditionPickerScreen(
        navigator: NavigationController,
        snackbarHostState: SnackbarHostState,
        blocksEnabled: Boolean,
        viewModel: AutomationConditionPickerViewModel = daggerViewModel(),
) {
    ObserveEvents(viewModel.events, snackbarHostState, navigator)

    AutomationConditionPickerLayout(controller = viewModel, blocksEnabled = blocksEnabled)
}

@Composable
private fun AutomationConditionPickerLayout(
        controller: AutomationConditionPickerController,
        blocksEnabled: Boolean,
) {
    Column(
            modifier = Modifier
                    .fillMaxWidth()
                    .background(
                            color = AppTheme.colors.colorSecondary,
                            shape = RoundedCornerShape(topStart = Dimmens.mainMargin, topEnd = Dimmens.mainMargin),
                    )
                    .padding(horizontal = Dimmens.mainMargin)
                    .padding(top = Dimmens.largeMargin, bottom = Dimmens.mainMargin)
                    .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(Dimmens.smallMargin),
    ) {
        Text(
                text = stringResource(R.string.automation_detail_button_add_condition),
                style = AppTheme.typography.textMedium18,
                color = AppTheme.colors.colorAccent,
        )

        GroupLabel(stringResource(R.string.automation_condition_picker_group_blocks))

        OutlineButtonLarge(
                text = stringResource(R.string.logic_block_operator_and),
                onClick = { controller.onOperatorClicked(LogicOperator.AND) },
                enabled = blocksEnabled,
        )
        OutlineButtonLarge(
                text = stringResource(R.string.logic_block_operator_or),
                onClick = { controller.onOperatorClicked(LogicOperator.OR) },
                enabled = blocksEnabled,
        )
        OutlineButtonLarge(
                text = stringResource(R.string.logic_block_operator_not),
                onClick = { controller.onOperatorClicked(LogicOperator.NOT) },
                enabled = blocksEnabled,
        )

        GroupLabel(stringResource(R.string.automation_condition_picker_group_objects))

        OutlineButtonLarge(
                text = stringResource(R.string.automation_condition_picker_object),
                onClick = controller::onObjectClicked,
        )
    }
}

@Composable
private fun GroupLabel(text: String) {
    Text(
            modifier = Modifier.padding(top = Dimmens.smallMargin),
            text = text,
            style = AppTheme.typography.captionMedium14,
            color = AppTheme.colors.textSecondary,
    )
}
