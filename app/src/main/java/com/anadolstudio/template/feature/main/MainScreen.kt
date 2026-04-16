@file:OptIn(
    ExperimentalComposeUiApi::class, ExperimentalMaterialNavigationApi::class,
    ExperimentalMaterial3Api::class
)

package com.anadolstudio.template.feature.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.ModalBottomSheetDefaults
import androidx.compose.material.Scaffold
import androidx.compose.material.rememberScaffoldState
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.BottomAppBarScrollBehavior
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import com.anadolstudio.compose.ui.theme.AppTheme
import com.anadolstudio.compose.ui.theme.shape
import com.anadolstudio.compose.ui.view.snackbar.SnackbarHost
import com.anadolstudio.compose.ui.view.snackbar.SnackbarHostState
import com.anadolstudio.compose.ui.view.snackbar.rememberSnackbarHostState
import com.anadolstudio.template.di.viewmodel.daggerViewModel
import com.anadolstudio.template.event.ObserveEvents
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.google.accompanist.navigation.material.ModalBottomSheetLayout

@Composable
internal fun MainScreen(
    navigator: NavigationController,
    viewModel: MainViewModel = daggerViewModel(),
) {
    val scaffoldState = rememberScaffoldState()
    val snackbarHostState = rememberSnackbarHostState(scaffoldState.snackbarHostState)

    ObserveEvents(viewModel.events, snackbarHostState, navigator)

    val state by viewModel.stateFlow.collectAsState()
    MainLayout(navigator, state, viewModel)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainLayout(
    navigator: NavigationController,
    state: MainScreenState,
    controller: MainController,
) {
    val scaffoldState = rememberScaffoldState()
    val snackbarHostState = rememberSnackbarHostState(scaffoldState.snackbarHostState)
    val scrollBottomBehavior = BottomAppBarDefaults.exitAlwaysScrollBehavior()

    Scaffold(
        modifier = Modifier.semantics { testTagsAsResourceId = true },
        scaffoldState = scaffoldState,
        backgroundColor = AppTheme.colors.colorSecondary,
        snackbarHost = { },
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            ModalBottomSheet(
                    navigator,
                    snackbarHostState,
                    paddingValues,
                    scrollBottomBehavior
            )
            SnackbarHost(
                hostState = scaffoldState.snackbarHostState,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp)
                    .statusBarsPadding()
                    .imePadding(),
            )
        }
    }
}

@Composable
private fun ModalBottomSheet(
    navigator: NavigationController,
    snackbarHostState: SnackbarHostState,
    paddingValues: PaddingValues,
    scrollBottomBehavior: BottomAppBarScrollBehavior
) {
    ModalBottomSheetLayout(
        bottomSheetNavigator = navigator.bottomSheetNavigator,
        sheetShape = ModalBottomSheetDefaults.shape,
        scrimColor = AppTheme.colors.textPrimary.copy(alpha = 0.32f),
        sheetBackgroundColor = AppTheme.colors.colorPrimary
    ) {
        NavHost(
            navController = navigator,
            graph = RootGraph(navigator, snackbarHostState),
            modifier = Modifier
                .padding(paddingValues.calculateTopPadding())
                .nestedScroll(scrollBottomBehavior.nestedScrollConnection),
        )
    }
}
