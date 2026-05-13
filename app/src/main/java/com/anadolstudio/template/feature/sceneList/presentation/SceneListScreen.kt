package com.anadolstudio.template.feature.sceneList.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.anadolstudio.compose.ui.theme.AppTheme
import com.anadolstudio.compose.ui.theme.Dimension
import com.anadolstudio.compose.ui.view.snackbar.SnackbarHostState
import com.anadolstudio.template.di.viewmodel.daggerViewModel
import com.anadolstudio.template.event.ObserveEvents
import com.anadolstudio.template.feature.main.NavigationController

private val SCENE_PLACEHOLDER_LIST = listOf(
        "Утро",
        "Уход из дома",
        "Тихий вечер",
        "Кино",
        "Ночь",
)

@Composable
internal fun SceneListScreen(
        navigator: NavigationController,
        snackbarHostState: SnackbarHostState,
        viewModel: SceneListViewModel = daggerViewModel(),
) {
    val state by viewModel.stateFlow.collectAsState()
    ObserveEvents(viewModel.events, snackbarHostState, navigator)

    SceneListLayout(state = state, controller = viewModel)
}

@Composable
private fun SceneListLayout(
        @Suppress("UNUSED_PARAMETER") state: SceneListScreenState,
        controller: SceneListController,
) {
    LazyColumn(
            modifier = Modifier
                    .fillMaxSize()
                    .background(AppTheme.colors.colorSecondary)
                    .systemBarsPadding(),
            contentPadding = PaddingValues(Dimension.mainMargin),
            verticalArrangement = Arrangement.spacedBy(Dimension.mediumMargin),
    ) {
        item {
            Text(
                    text = "Сценарии (заглушка)",
                    style = AppTheme.typography.textBook18,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.colorAccent,
            )
        }

        items(SCENE_PLACEHOLDER_LIST) { sceneName ->
            ScenePlaceholderRow(
                    title = sceneName,
                    onClick = { controller.onSceneItemClicked() },
            )
        }
    }
}

@Composable
private fun ScenePlaceholderRow(
        title: String,
        onClick: () -> Unit,
) {
    Column(
            modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(AppTheme.colors.colorPrimary)
                    .clickable(onClick = onClick)
                    .padding(Dimension.mediumMargin),
    ) {
        Text(
                text = title,
                style = AppTheme.typography.textBook18,
                color = AppTheme.colors.colorAccent,
        )
    }
}
