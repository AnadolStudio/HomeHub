package com.anadolstudio.template.feature.automation.automationList.presentation

import com.anadolstudio.template.feature.home.domain.model.entity.HomeAssistantEntity
import com.anadolstudio.template.feature.home.domain.model.states.HomeAssistantAttribute

internal interface AutomationListController {

    fun onAutomationItemClicked()
    fun onAutomationItemEnableChanged(entity: HomeAssistantEntity<HomeAssistantAttribute>)
    fun onTabSelected(tab: AutomationTab)
    fun onSceneItemClicked()
    fun onCreateClicked()

}
