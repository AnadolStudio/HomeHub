package com.anadolstudio.homehub.feature.addDeviceGroup.presentation

import com.anadolstudio.homehub.base.viewmodel.StatefulViewModel
import javax.inject.Inject

internal class AddDeviceGroupViewModel @Inject constructor() :
        StatefulViewModel<AddDeviceGroupScreenState>(AddDeviceGroupScreenState()),
        AddDeviceGroupController
