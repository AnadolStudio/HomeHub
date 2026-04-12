@file:OptIn(ExperimentalMaterial3Api::class)

package com.anadolstudio.template.feature.home.presetnation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import com.anadolstudio.compose.ui.drawable.Icons
import com.anadolstudio.compose.ui.theme.AppTheme
import com.anadolstudio.compose.ui.theme.preview.ThemePreviewParameter
import com.anadolstudio.compose.ui.view.snackbar.SnackbarHostState
import com.anadolstudio.compose.ui.view.toolbar.Toolbar
import com.anadolstudio.template.R
import com.anadolstudio.template.di.viewmodel.daggerViewModel
import com.anadolstudio.template.event.ObserveEvents
import com.anadolstudio.template.feature.main.NavigationController
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
internal fun HomeScreen(
        navigator: NavigationController,
        snackbarHostState: SnackbarHostState,
        viewModel: HomeViewModel = daggerViewModel()
) {
    val state by viewModel.stateFlow.collectAsState()
    ObserveEvents(viewModel.events, snackbarHostState, navigator)

    HomeLayout(
            state = state,
            controller = viewModel,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeLayout(
        state: HomeState,
        controller: HomeController,
) {
    BackHandler { controller.onBackClicked() }
    Row(modifier = Modifier.statusBarsPadding()) {
        Toolbar(
                title = stringResource(id = R.string.app_name),
                navigationIcon = null,
                onNavigationClick = {},
                actions = {
                    ToolbarAction(action = { /*TODO*/ }, painter = Icons.Search)
                    ToolbarAction(action = { /*TODO*/ }, painter = Icons.VerticalMore)
                }
        )
    }
}


@Composable
private fun ToolbarAction(action: () -> Unit, painter: Painter) {
    IconButton(onClick = { action.invoke() }) {
        Image(
                modifier = Modifier
                        .padding(vertical = 4.dp)
                        .size(24.dp),
                painter = painter,
                contentDescription = null,
        )
    }
}

@Preview
@Composable
private fun PreviewMedia(@PreviewParameter(ThemePreviewParameter::class) useDarkMode: Boolean) {
    val state = remember {
        HomeState()
    }
    val exampleList = listOf("Set", "Set1", "Set2")
    val images = MutableStateFlow(PagingData.from(exampleList)).collectAsLazyPagingItems()

    val controller = object : HomeController {
        override fun onBackClicked() = Unit
    }

    AppTheme(useDarkMode) {
        HomeLayout(
                state = state,
                controller = controller,
        )
    }
}


@Preview
@Composable
private fun Shimmer(@PreviewParameter(ThemePreviewParameter::class) useDarkMode: Boolean) {
    AppTheme(useDarkMode) {

    }
}
