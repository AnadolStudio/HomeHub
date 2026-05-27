package com.anadolstudio.template.feature.add.presentation

import com.anadolstudio.template.base.viewmodel.StatefulViewModel
import com.anadolstudio.template.feature.main.MainGraph.navigateToAddDeviceGroup
import com.anadolstudio.template.feature.main.MainGraph.navigateToAddMatter
import com.anadolstudio.template.feature.main.MainGraph.navigateToAddPerson
import com.anadolstudio.template.feature.main.MainGraph.navigateToAddZigbee
import javax.inject.Inject

internal class AddViewModel @Inject constructor() :
    StatefulViewModel<AddScreenState>(AddScreenState()),
    AddController {

    override fun onZigbeeClicked() = navigateToAddZigbee()

    override fun onMatterClicked() = navigateToAddMatter()

    override fun onPersonClicked() = navigateToAddPerson()

    override fun onDeviceGroupClicked() = navigateToAddDeviceGroup()
}
