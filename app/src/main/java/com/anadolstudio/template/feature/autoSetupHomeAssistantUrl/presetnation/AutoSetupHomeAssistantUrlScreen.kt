@file:OptIn(ExperimentalMaterial3Api::class)

package com.anadolstudio.template.feature.autoSetupHomeAssistantUrl.presetnation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.anadolstudio.compose.ui.theme.AppTheme
import com.anadolstudio.compose.ui.theme.Dimension
import com.anadolstudio.compose.ui.theme.Shapes
import com.anadolstudio.compose.ui.theme.largeShimmer
import com.anadolstudio.compose.ui.theme.preview.ThemePreviewParameter
import com.anadolstudio.compose.ui.view.button.TextButton
import com.anadolstudio.compose.ui.view.snackbar.SnackbarHostState
import com.anadolstudio.template.R
import com.anadolstudio.template.base.view.HomeHubLoader
import com.anadolstudio.template.di.viewmodel.daggerViewModel
import com.anadolstudio.template.event.ObserveEvents
import com.anadolstudio.template.feature.autoSetupHomeAssistantUrl.domain.model.HomeAssistantInstance
import com.anadolstudio.template.feature.autoSetupHomeAssistantUrl.domain.model.HomeAssistantVersion
import com.anadolstudio.template.feature.common.ui.HomeHubIcons
import com.anadolstudio.template.feature.main.NavigationController
import com.anadolstudio.utils.states.ProgressState

@Composable
internal fun AutoSetupHomeAssistantUrlScreen(
        navigator: NavigationController,
        snackbarHostState: SnackbarHostState,
        viewModel: AutoSetupHomeAssistantUrlViewModel = daggerViewModel(),
) {
    val state by viewModel.stateFlow.collectAsState()
    ObserveEvents(viewModel.events, snackbarHostState, navigator)

    AutoSetupHomeAssistantUrlLayout(
            state = state,
            controller = viewModel,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AutoSetupHomeAssistantUrlLayout(
        state: AutoSetupHomeAssistantUrlState,
        controller: AutoSetupHomeAssistantUrlController,
) {
    BackHandler { controller.onBackClicked() }
    Column(
            modifier = Modifier
                    .background(color = AppTheme.colors.colorSecondary)
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(vertical = Dimension.mainMargin)
                    .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
                modifier = Modifier.padding(horizontal = 80.dp),
                text = stringResource(R.string.auto_setup_home_assistant_url_title),
                style = AppTheme.typography.textBook24.copy(fontWeight = FontWeight.Bold),
                color = AppTheme.colors.colorAccent,
                textAlign = TextAlign.Center,
        )
        Box(
                modifier = Modifier
                        .fillMaxWidth()
                        .weight(1F),
        ) {
            when (state.progressState) {
                ProgressState.Loading -> Loading(modifier = Modifier.fillMaxSize())

                is ProgressState.Error -> Error(modifier = Modifier.fillMaxSize()) // TODO нужна дефолтная заглушка

                ProgressState.Content, ProgressState.Refresh -> Content(
                        modifier = Modifier.fillMaxSize(),
                        isRefreshing = state.progressState is ProgressState.Refresh,
                        instances = state.instanceSet,
                        controller = controller
                )

                else -> Unit
            }
        }

        TextButton(
                text = stringResource(R.string.auto_setup_home_assistant_url_manual_enter_button),
                shape = Shapes.large,
                onClick = { controller.onManualEnterClicked() },
        )
    }
}

@Composable
private fun Loading(modifier: Modifier) {
    Column(
            modifier = modifier.padding(horizontal = Dimension.largeMargin),
            verticalArrangement = Arrangement.Center,
    ) {
        HomeHubLoader(Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.height(Dimension.mainMargin))
            Text(
                    modifier = Modifier.padding(horizontal = Dimension.mainMargin),
                    text = stringResource(R.string.auto_setup_home_assistant_url_loading),
                    style = AppTheme.typography.textBook18,
                    color = AppTheme.colors.textPrimary,
                    textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun Error(modifier: Modifier) {
    Column(
            modifier = modifier.padding(horizontal = Dimension.mainMargin),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
    ) {
        Text(
                text = stringResource(R.string.auto_setup_home_assistant_url_wifi_required_error),
                style = AppTheme.typography.textBook18,
                color = AppTheme.colors.textPrimary,
                textAlign = TextAlign.Center,
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun Content(
        modifier: Modifier,
        instances: Set<HomeAssistantInstance>,
        isRefreshing: Boolean,
        controller: AutoSetupHomeAssistantUrlController,
) {
    val pullToRefreshState = rememberPullToRefreshState()
    PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { controller.onRefreshSwiped() },
            state = pullToRefreshState,
            indicator = {
                PullToRefreshDefaults.Indicator(
                        modifier = Modifier.align(Alignment.TopCenter),
                        isRefreshing = isRefreshing,
                        state = pullToRefreshState,
                        containerColor = AppTheme.colors.colorPrimary,
                        color = AppTheme.colors.colorAccent,
                )
            },
    ) {

        if (instances.isEmpty()) {
            EmptyState(modifier = modifier)
            return@PullToRefreshBox
        }

        LazyColumn(
                modifier = modifier.padding(top = Dimension.mainMargin),
                contentPadding = PaddingValues(
                        horizontal = Dimension.mainMargin,
                        vertical = Dimension.mainMargin,
                ),
                verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(items = instances.toList(), key = { it.url }) { instance ->
                ServerListItem(
                        instance = instance,
                        onClick = { controller.onInstanceClicked(instance) },
                )
            }
        }
    }
}

@Composable
private fun EmptyState(modifier: Modifier) {
    Column(
            modifier = modifier.padding(horizontal = Dimension.mainMargin),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
    ) {
        Text(
                text = stringResource(R.string.auto_setup_home_assistant_url_empty_state),
                style = AppTheme.typography.textBook18,
                color = AppTheme.colors.textPrimary,
                textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun ServerListItem(
        instance: HomeAssistantInstance,
        onClick: () -> Unit,
) {
    Row(
            modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 4.dp, shape = Shapes.largeShimmer)
                    .background(color = AppTheme.colors.colorPrimary, shape = Shapes.largeShimmer)
                    .clickable(onClick = onClick)
                    .padding(vertical = Dimension.smallMargin)
                    .padding(start = Dimension.extraSmallMargin, end = Dimension.mediumMargin),
            verticalAlignment = Alignment.CenterVertically,
    ) {
        ServerIcon(painter = HomeHubIcons.Host)
        Spacer(modifier = Modifier.size(Dimension.smallMargin))
        Column(modifier = Modifier.weight(1F)) {
            Text(
                    text = instance.name,
                    style = AppTheme.typography.textMedium18,
                    color = AppTheme.colors.colorAccent,
                    maxLines = 2
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                    text = instance.url,
                    style = AppTheme.typography.captionBook14,
                    color = AppTheme.colors.colorAccent,
            )
        }
    }
}

@Composable
private fun ServerIcon(painter: Painter) {
    Box(
            modifier = Modifier
                    .size(48.dp)
                    .background(color = Color.Transparent, shape = Shapes.small),
            contentAlignment = Alignment.Center,
    ) {
        androidx.compose.foundation.Image(
                modifier = Modifier.size(32.dp),
                painter = painter,
                contentDescription = null,
                colorFilter = ColorFilter.tint(AppTheme.colors.colorAccent),
        )
    }
}

@Preview(widthDp = 360, heightDp = 640)
@Composable
private fun PreviewLoading(@PreviewParameter(ThemePreviewParameter::class) useDarkMode: Boolean) {
    val state =
            remember { AutoSetupHomeAssistantUrlState(progressState = ProgressState.Loading, hasWifiConnect = true) }
    AppTheme(useDarkMode) {
        AutoSetupHomeAssistantUrlLayout(state = state, controller = PreviewController)
    }
}

@Preview(widthDp = 360, heightDp = 640)
@Composable
private fun PreviewContent(@PreviewParameter(ThemePreviewParameter::class) useDarkMode: Boolean) {
    val state = remember {
        AutoSetupHomeAssistantUrlState(
                progressState = ProgressState.Content,
                hasWifiConnect = true,
                instanceSet = setOf(
                        HomeAssistantInstance(
                                name = "Home Sweet Home",
                                url = "http://192.168.1.10:8123",
                                version = HomeAssistantVersion(2024, 10, 3),
                        ),
                        HomeAssistantInstance(
                                name = "Home Assistant Server",
                                url = "http://192.168.1.20:8123",
                                version = HomeAssistantVersion(2024, 9, 1),
                        ),
                        HomeAssistantInstance(
                                name = "Home Assistant Server Home Assistant Server Home Assistant Server",
                                url = "http://192.168.1.30:8123",
                                version = HomeAssistantVersion(2024, 9, 1),
                        ),
                ),
        )
    }
    AppTheme(useDarkMode) {
        AutoSetupHomeAssistantUrlLayout(state = state, controller = PreviewController)
    }
}

@Preview(widthDp = 360, heightDp = 640)
@Composable
private fun PreviewEmpty(@PreviewParameter(ThemePreviewParameter::class) useDarkMode: Boolean) {
    val state =
            remember { AutoSetupHomeAssistantUrlState(progressState = ProgressState.Content, hasWifiConnect = true) }
    AppTheme(useDarkMode) {
        AutoSetupHomeAssistantUrlLayout(state = state, controller = PreviewController)
    }
}

@Preview(widthDp = 360, heightDp = 640)
@Composable
private fun PreviewError(@PreviewParameter(ThemePreviewParameter::class) useDarkMode: Boolean) {
    val state =
            remember { AutoSetupHomeAssistantUrlState(progressState = ProgressState.Error(), hasWifiConnect = false) }
    AppTheme(useDarkMode) {
        AutoSetupHomeAssistantUrlLayout(state = state, controller = PreviewController)
    }
}

private val PreviewController = object : AutoSetupHomeAssistantUrlController {
    override fun onBackClicked() = Unit
    override fun onManualEnterClicked() = Unit
    override fun onInstanceClicked(instance: HomeAssistantInstance) = Unit
    override fun onRefreshSwiped() = Unit
}
