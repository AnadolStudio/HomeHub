package com.anadolstudio.template.feature.addDeviceGroup.presentation

import com.anadolstudio.template.base.viewmodel.StatefulViewModel
import javax.inject.Inject

internal class AddDeviceGroupViewModel @Inject constructor() :
        StatefulViewModel<AddDeviceGroupScreenState>(AddDeviceGroupScreenState()),
        AddDeviceGroupController
