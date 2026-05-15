package com.anadolstudio.template.feature.automation.automationList.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.DeviceHub
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.anadolstudio.compose.ui.theme.AppTheme
import com.anadolstudio.compose.ui.theme.Dimension
import com.anadolstudio.compose.ui.theme.Shapes
import com.anadolstudio.compose.ui.theme.largeShimmer
import com.anadolstudio.compose.ui.view.button.FloatTextButton
import com.anadolstudio.compose.ui.view.snackbar.SnackbarHostState
import com.anadolstudio.template.R
import com.anadolstudio.template.di.viewmodel.daggerViewModel
import com.anadolstudio.template.event.ObserveEvents
import com.anadolstudio.template.feature.main.NavigationController

@Composable
internal fun AutomationListScreen(
        navigator: NavigationController,
        snackbarHostState: SnackbarHostState,
        viewModel: AutomationListViewModel = daggerViewModel(),
) {
    val state by viewModel.stateFlow.collectAsState()
    ObserveEvents(viewModel.events, snackbarHostState, navigator)

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
                    contentPadding = PaddingValues(Dimension.mainMargin),
                    verticalArrangement = Arrangement.spacedBy(Dimension.mediumMargin),
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
                    transitionSpec = {
                        (fadeIn() + slideInVertically { height -> height / 2 })
                                .togetherWith(fadeOut() + slideOutVertically { height -> -height / 2 })
                    },
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
                val title = stringResource(tab.titleRes)

                NavigationBarItem(
                        selected = isSelected,
                        onClick = { controller.onTabSelected(tab) },
                        icon = { Icon(imageVector = tab.icon, contentDescription = title) },
                        label = {
                            Text(
                                    text = title,
                                    style = AppTheme.typography.textBook14,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
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
    items(state.automationList) { automation ->
        AutomationItem(
                title = automation.state.attributes.friendlyName,
                icon = Icons.Outlined.DeviceHub, // TODO temp
                isEnable = automation.state.allowedState.toBooleanOrNull(),
                onClicked = { controller.onAutomationItemClicked() },
                onEnableClicked = { controller.onAutomationItemEnableChanged(automation) },
        )
    }
}

private fun LazyListScope.sceneItems(
        state: AutomationListScreenState,
        controller: AutomationListController,
) {
    items(state.sceneList) { scene ->
        AutomationItem(
                title = scene.state.attributes.friendlyName,
                icon = Icons.Outlined.Movie, // TODO temp
                isEnable = null,
                onClicked = { controller.onSceneItemClicked() },
                onEnableClicked = {},
        )
    }
}

@Composable
private fun AutomationItem(
        icon: ImageVector,
        title: String,
        isEnable: Boolean?,
        onClicked: () -> Unit,
        onEnableClicked: (Boolean) -> Unit,
) {
    Row(
            modifier = Modifier
                    .fillMaxWidth()
                    .clip(Shapes.largeShimmer)
                    .background(AppTheme.colors.colorPrimary)
                    .clickable(onClick = onClicked)
                    .padding(vertical = Dimension.mediumMargin, horizontal = Dimension.mainMargin),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimension.smallMargin)
    ) {
        Icon(
                imageVector = icon,
                tint = AppTheme.colors.colorAccent,
                contentDescription = null
        )
        Text(
                modifier = Modifier.weight(1f),
                text = title,
                style = AppTheme.typography.textBook18,
                color = AppTheme.colors.colorAccent,
        )

        if (isEnable != null) {
            Switch(
                    checked = isEnable,
                    onCheckedChange = { isEnable -> onEnableClicked.invoke(isEnable) },
                    colors = SwitchDefaults.colors(
                            checkedThumbColor = AppTheme.colors.colorPrimary,
                            checkedTrackColor = AppTheme.colors.colorAccent,
                            checkedBorderColor = AppTheme.colors.colorAccent,
                            uncheckedThumbColor = AppTheme.colors.colorPrimary,
                            uncheckedTrackColor = AppTheme.colors.disable,
                            uncheckedBorderColor = AppTheme.colors.disable,
                    )
            )
        }
    }
}
