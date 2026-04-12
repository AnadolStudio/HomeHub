package com.anadolstudio.template.navigation

import androidx.navigation.NavGraph
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import com.anadolstudio.template.event.EventsDispatcher
import com.anadolstudio.template.event.navigateTo
import com.anadolstudio.template.feature.main.NavigationController
import androidx.navigation.createGraph as originalCreateGraph

internal abstract class RootNavGraphContract : BaseNavGraphContract() {

    protected abstract val startDestination: String

    protected fun NavigationController.createGraph(builder: NavGraphBuilder.() -> Unit): NavGraph {
        return originalCreateGraph(startDestination = startDestination, builder = builder)
    }

    @Deprecated(
        "Use createGraph variant without start destination",
        ReplaceWith("createGraph { builder() }"),
        DeprecationLevel.ERROR,
    )
    @Suppress("UNUSED_PARAMETER", "unused")
    protected inline fun NavigationController.createGraph(
        startDestination: String,
        route: String? = null,
        builder: NavGraphBuilder.() -> Unit,
    ): NavGraph = error("Should not be called!")

    override fun buildRoute(childRoute: String): String = childRoute

    override fun EventsDispatcher.navigateFromRoot(
        route: String,
        builder: NavOptionsBuilder.() -> Unit
    ) {
        navigateTo(route) {
            popUpTo(0)
            builder()
        }
    }
}
