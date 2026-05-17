package com.anadolstudio.template.feature.automation.automationList.presentation

import com.anadolstudio.template.feature.home.domain.model.entity.HomeAssistantEntity
import com.anadolstudio.template.feature.home.domain.model.states.HomeAssistantAttribute
import com.anadolstudio.template.feature.home.domain.model.states.SceneAttributes

internal interface AutomationListController {

    fun onAutomationItemClicked()
    fun onAutomationItemEnableChanged(entity: HomeAssistantEntity<HomeAssistantAttribute>)
    fun onTabSelected(tab: AutomationTab)
    fun onSceneItemClicked(scene: HomeAssistantEntity<SceneAttributes>)
    fun onCreateClicked()

    /** Перезапуск загрузки списка после возврата из SceneCreate. */
    fun onSceneListRefreshRequested()
}
