package com.anadolstudio.template.feature.main

import android.content.res.Resources
import com.anadolstudio.template.base.viewmodel.StatefulViewModel
import javax.inject.Inject

internal class MainViewModel @Inject constructor(
    private val resources: Resources
) : StatefulViewModel<MainScreenState>(
    MainScreenState(
    )
), MainController {

    override fun onBackClicked() = Unit

}

internal class MainScreenState() {

}
