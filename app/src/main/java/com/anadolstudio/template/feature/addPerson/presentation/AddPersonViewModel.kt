package com.anadolstudio.template.feature.addPerson.presentation

import com.anadolstudio.template.base.viewmodel.StatefulViewModel
import javax.inject.Inject

internal class AddPersonViewModel @Inject constructor() :
        StatefulViewModel<AddPersonScreenState>(AddPersonScreenState()),
        AddPersonController
