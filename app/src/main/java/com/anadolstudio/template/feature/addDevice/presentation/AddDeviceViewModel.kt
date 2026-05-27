package com.anadolstudio.template.feature.addDevice.presentation

import com.anadolstudio.template.base.viewmodel.StatefulViewModel
import javax.inject.Inject

internal class AddDeviceViewModel @Inject constructor() :
        StatefulViewModel<AddDeviceScreenState>(AddDeviceScreenState()),
        AddDeviceController
