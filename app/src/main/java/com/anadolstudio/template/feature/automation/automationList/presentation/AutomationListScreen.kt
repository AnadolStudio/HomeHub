package com.anadolstudio.template.feature.automation.automationList.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.HdrAuto
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.anadolstudio.compose.ui.theme.AppTheme
import com.anadolstudio.compose.ui.theme.Dimmens
import com.anadolstudio.compose.ui.view.button.FloatTextButton
import com.anadolstudio.compose.ui.view.snackbar.SnackbarHostState
import com.anadolstudio.template.R
import com.anadolstudio.template.base.view.homeHubSwitchDefaults
import com.anadolstudio.template.di.viewmodel.daggerViewModel
import com.anadolstudio.template.event.ObserveEvents
import com.anadolstudio.template.feature.automation.common.presentation.AutomationItem
import com.anadolstudio.template.feature.automation.common.presentation.SceneItem
import com.anadolstudio.template.feature.main.NavigationController
import com.anadolstudio.template.feature.sceneCreate.presentation.SCENE_LIST_NEEDS_REFRESH_KEY
import com.anadolstudio.template.navigation.ObserveResultValue
import com.anadolstudio.template.util.toPainter

@Composable
internal fun AutomationListScreen(
        navigator: NavigationController,
        snackbarHostState: SnackbarHostState,
        viewModel: AutomationListViewModel = daggerViewModel(),
) {
    val state by viewModel.stateFlow.collectAsState()
    ObserveEvents(viewModel.events, snackbarHostState, navigator)

    // После закрытия SceneCreate приходит маркер — перезагружаем сцены/автоматизации.
    ObserveResultValue<Boolean>(
            navigator = navigator,
            key = SCENE_LIST_NEEDS_REFRESH_KEY,
    ) { _ -> viewModel.onSceneListRefreshRequested() }

    AutomationListLayout(state = state, controller = viewModel)
}

@Composable
private fun AutomationListLayout(
        state: AutomationListScreenState,
        controller: AutomationListController,
) {

    Column(
            modifier = Modifier
                    .fillMaxSize()
                    .background(AppTheme.colors.colorSecondary)
                    .statusBarsPadding(),
    ) {
        Box(
                modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
        ) {
            LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(Dimmens.mainMargin),
                    verticalArrangement = Arrangement.spacedBy(Dimmens.mediumMargin),
            ) {
                when (state.currentTab) {
                    AutomationTab.AUTOMATIONS -> automationItems(state, controller)
                    AutomationTab.SCENES -> sceneItems(state, controller)
                }
            }

            AnimatedContent(
                    modifier = Modifier.align(Alignment.BottomEnd),
                    targetState = state.currentTab,
                    label = "FloatTextButtonAnimation",
                    transitionSpec = { fadeIn().togetherWith(fadeOut()) },
            ) { tab ->
                FloatTextButton(
                        text = stringResource(
                                when (tab) {
                                    AutomationTab.AUTOMATIONS -> R.string.automation_list_create_automation
                                    AutomationTab.SCENES -> R.string.automation_list_create_scene
                                }
                        ),
                        onClick = controller::onCreateClicked,
                        icon = rememberVectorPainter(Icons.Outlined.Add),
                )
            }
        }

        NavigationBar(
                containerColor = AppTheme.colors.colorPrimary,
                contentColor = AppTheme.colors.colorAccent,
        ) {
            state.tabList.forEachIndexed { _, tab ->
                val isSelected = state.currentTab == tab
                val unselectedColor = AppTheme.colors.colorAccent.copy(alpha = 0.7f)
                val title = stringResource(tab.titleRes)

                NavigationBarItem(
                        selected = isSelected,
                        onClick = { controller.onTabSelected(tab) },
                        icon = {
                            Icon(
                                    imageVector = tab.icon,
                                    contentDescription = title,
                                    tint = if (isSelected) AppTheme.colors.colorAccent else unselectedColor
                            )
                        },
                        label = {
                            Text(
                                    text = title,
                                    style = AppTheme.typography.textBook14,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) AppTheme.colors.colorAccent else unselectedColor
                            )
                        },
                        alwaysShowLabel = true,
                        colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = AppTheme.colors.colorAccent,
                                selectedTextColor = AppTheme.colors.colorAccent,
                                unselectedIconColor = AppTheme.colors.textSecondary,
                                unselectedTextColor = AppTheme.colors.textSecondary,
                                indicatorColor = AppTheme.colors.colorSecondary,
                        ),
                )
            }
        }
    }
}

private fun LazyListScope.automationItems(
        state: AutomationListScreenState,
        controller: AutomationListController,
) {
    items(
            items = state.automationList,
            key = { it.entityId },
    ) { automation ->
        AutomationItem(
                modifier = Modifier.animateItem(),
                title = automation.name,
                icon = automation.state.attributes.icon?.toPainter()
                        ?: rememberVectorPainter(Icons.Outlined.HdrAuto),
                draggableActionIcon = Icons.Outlined.DeleteOutline,
                onClicked = { controller.onAutomationItemClicked() },
                onDraggableActionClicked = { controller.onAutomationItemDeleteClicked(automation) },
                trailing = {
                    Switch(
                            modifier = Modifier.padding(end = Dimmens.smallMargin),
                            checked = automation.state.allowedState.toBooleanOrNull() ?: false,
                            onCheckedChange = { controller.onAutomationItemEnableChanged(automation) },
                            colors = homeHubSwitchDefaults,
                    )
                }
        )
    }
}

private fun LazyListScope.sceneItems(
        state: AutomationListScreenState,
        controller: AutomationListController,
) {
    items(
            items = state.sceneList,
            key = { it.entityId },
    ) { scene ->
        SceneItem(
                modifier = Modifier.animateItem(),
                title = scene.name,
                icon = scene.state.attributes.icon?.toPainter()
                        ?: rememberVectorPainter(Icons.Outlined.Movie),
                onClicked = { controller.onSceneItemClicked(scene) },
                draggableActionIcon = Icons.Outlined.DeleteOutline,
                onDraggableActionClicked = { controller.onSceneItemDeleteClicked(scene) },
                trailing = {
                    IconButton(
                            onClick = { controller.onSceneStart(scene) }
                    ) {
                        Icon(
                                modifier = Modifier.size(32.dp),
                                imageVector = Icons.Outlined.PlayCircle,
                                contentDescription = null,
                        )
                    }
                }
        )
    }
}

