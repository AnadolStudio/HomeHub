package com.anadolstudio.template.feature.automationList.presentation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeviceHub
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector
import com.anadolstudio.template.feature.home.domain.model.entity.HomeAssistantEntity
import com.anadolstudio.template.feature.home.domain.model.states.AutomationAttributes
import com.anadolstudio.template.feature.home.domain.model.states.SceneAttributes
import com.anadolstudio.utils.states.ProgressState

@Immutable
internal data class AutomationListScreenState(
        val progressState: ProgressState = ProgressState.Content,
        val tabList: List<AutomationTab> = AutomationTab.entries,
        val currentTab: AutomationTab = AutomationTab.AUTOMATIONS,
        val sceneList: List<HomeAssistantEntity<SceneAttributes>> = emptyList(),
        val automationList: List<HomeAssistantEntity<AutomationAttributes>> = emptyList(),
)

enum class AutomationTab(
        val title: String,
        val icon: ImageVector,
) {
    AUTOMATIONS(title = "Автоматизации", icon = Icons.Outlined.DeviceHub),
    SCENES(title = "Сценарии", icon = Icons.Outlined.Movie),
}
