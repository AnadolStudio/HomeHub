package com.anadolstudio.homehub.feature.add.presentation

import com.anadolstudio.homehub.base.viewmodel.StatefulViewModel
import com.anadolstudio.homehub.feature.main.MainGraph.navigateToAddDeviceGroup
import com.anadolstudio.homehub.feature.main.MainGraph.navigateToAddMatter
import com.anadolstudio.homehub.feature.main.MainGraph.navigateToAddPerson
import com.anadolstudio.homehub.feature.main.MainGraph.navigateToAddZigbee
import javax.inject.Inject

internal class AddViewModel @Inject constructor() :
    StatefulViewModel<AddScreenState>(AddScreenState()),
    AddController {

    override fun onZigbeeClicked() = navigateToAddZigbee()

    override fun onMatterClicked() = navigateToAddMatter()

    override fun onPersonClicked() = navigateToAddPerson()

    override fun onDeviceGroupClicked() = navigateToAddDeviceGroup()
}
