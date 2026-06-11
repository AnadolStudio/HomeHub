package com.anadolstudio.homehub.feature.addPerson.presentation

import com.anadolstudio.homehub.base.viewmodel.StatefulViewModel
import javax.inject.Inject

internal class AddPersonViewModel @Inject constructor() :
        StatefulViewModel<AddPersonScreenState>(AddPersonScreenState()),
        AddPersonController
