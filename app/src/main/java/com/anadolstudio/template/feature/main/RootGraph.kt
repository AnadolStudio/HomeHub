package com.anadolstudio.template.feature.main

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavGraph
import com.anadolstudio.compose.ui.view.snackbar.SnackbarHostState
import com.anadolstudio.template.feature.main.MainGraph.mainGraph
import com.anadolstudio.template.navigation.RootNavGraphContract

internal object RootGraph : RootNavGraphContract() {

    override val startDestination = main()

    private fun main() = route { "main" }

    @Composable
    operator fun invoke(
            navigator: NavigationController,
            snackbarHostState: SnackbarHostState,
    ): NavGraph = remember(navigator, snackbarHostState) {
        navigator.createGraph {
            mainGraph(main(), navigator, snackbarHostState)
        }
    }

}
