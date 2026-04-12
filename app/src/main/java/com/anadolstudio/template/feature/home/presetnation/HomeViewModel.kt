package com.anadolstudio.template.feature.home.presetnation

import android.content.res.Resources
import com.anadolstudio.template.base.viewmodel.StatefulViewModel
import com.anadolstudio.template.event.navigateUp
import com.anadolstudio.template.event.showTodo
import timber.log.Timber
import javax.inject.Inject

internal class HomeViewModel @Inject constructor(
    private val resources: Resources,
) : StatefulViewModel<HomeState>(
    HomeState()
), HomeController {

    override fun onBackClicked() {
        Timber.tag("DEBUG_TAG").d("onBackClicked:")
        navigateUp()
    }
}
