package com.anadolstudio.template.feature.automation.automationList.presentation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeviceHub
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector
import com.anadolstudio.template.R
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
        @StringRes val titleRes: Int,
        val icon: ImageVector,
) {
    AUTOMATIONS(titleRes = R.string.automation_list_tab_automations, icon = Icons.Outlined.DeviceHub),
    SCENES(titleRes = R.string.automation_list_tab_scenes, icon = Icons.Outlined.Movie),
}
