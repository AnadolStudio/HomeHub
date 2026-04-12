package com.anadolstudio.template.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraph
import com.anadolstudio.compose.ui.view.snackbar.SnackbarHostState
import com.anadolstudio.template.feature.main.NavigationController

internal abstract class RootBottomNavGraphContract : RootNavGraphContract() {

    abstract val noBottomNavigationRoutes: Set<String>

    @Composable
    abstract operator fun invoke(
        rootNavigator: NavigationController,
        navigator: NavigationController,
        snackbarHostState: SnackbarHostState
    ): NavGraph
}
