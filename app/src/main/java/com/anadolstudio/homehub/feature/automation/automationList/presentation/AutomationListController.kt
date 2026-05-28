package com.anadolstudio.homehub.feature.automation.automationList.presentation

import com.anadolstudio.homehub.feature.home.domain.model.entity.HomeAssistantEntity
import com.anadolstudio.homehub.feature.home.domain.model.states.AutomationAttributes
import com.anadolstudio.homehub.feature.home.domain.model.states.HomeAssistantAttribute
import com.anadolstudio.homehub.feature.home.domain.model.states.SceneAttributes

internal interface AutomationListController {

    fun onAutomationItemClicked()
    fun onAutomationItemEnableChanged(entity: HomeAssistantEntity<HomeAssistantAttribute>)
    fun onAutomationItemDeleteClicked(automation: HomeAssistantEntity<AutomationAttributes>)
    fun onTabSelected(tab: AutomationTab)
    fun onSceneStart(scene: HomeAssistantEntity<SceneAttributes>)
    fun onSceneItemClicked(scene: HomeAssistantEntity<SceneAttributes>)
    fun onSceneItemDeleteClicked(scene: HomeAssistantEntity<SceneAttributes>)
    fun onCreateClicked()

    /** Перезапуск загрузки списка после возврата из SceneCreate. */
    fun onSceneListRefreshRequested()
}
