package com.anadolstudio.homehub.feature.automation.automationConditionPicker.presentation

import com.anadolstudio.homehub.feature.automation.automationDetail.presentation.logic_block.LogicOperator

internal interface AutomationConditionPickerController {

    fun onOperatorClicked(operator: LogicOperator)

    fun onObjectClicked()

    fun onCloseClicked()
}
