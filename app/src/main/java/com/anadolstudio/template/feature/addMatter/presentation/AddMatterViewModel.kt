package com.anadolstudio.template.feature.addMatter.presentation

import com.anadolstudio.template.base.viewmodel.StatefulViewModel
import javax.inject.Inject

internal class AddMatterViewModel @Inject constructor() :
        StatefulViewModel<AddMatterScreenState>(AddMatterScreenState()),
        AddMatterController
