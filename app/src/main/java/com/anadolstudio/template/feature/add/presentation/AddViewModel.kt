package com.anadolstudio.template.feature.add.presentation

import com.anadolstudio.template.base.viewmodel.StatefulViewModel
import javax.inject.Inject

internal class AddViewModel @Inject constructor() :
        StatefulViewModel<AddScreenState>(AddScreenState()),
        AddController
