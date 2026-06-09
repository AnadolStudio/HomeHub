package com.anadolstudio.homehub.feature.automation.automationConditionPicker.presentation

import com.anadolstudio.homehub.base.viewmodel.StatefulViewModel
import com.anadolstudio.homehub.event.navigateUp
import com.anadolstudio.homehub.feature.automation.automationDetail.presentation.AUTOMATION_CONDITION_OPERATOR_KEY
import com.anadolstudio.homehub.feature.automation.automationDetail.presentation.logic_block.LogicOperator
import com.anadolstudio.homehub.feature.main.MainGraph.navigateToConditionDevicePicker
import javax.inject.Inject

internal class AutomationConditionPickerViewModel @Inject constructor() :
        StatefulViewModel<Unit>(Unit),
        AutomationConditionPickerController {

    override fun onOperatorClicked(operator: LogicOperator) =
            navigateUp(AUTOMATION_CONDITION_OPERATOR_KEY to operator)

    override fun onObjectClicked() = navigateToConditionDevicePicker()

    override fun onCloseClicked() = navigateUp()
}
