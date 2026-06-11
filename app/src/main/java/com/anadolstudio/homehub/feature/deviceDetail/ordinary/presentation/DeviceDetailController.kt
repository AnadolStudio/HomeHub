package com.anadolstudio.homehub.feature.deviceDetail.ordinary.presentation

import com.anadolstudio.homehub.feature.deviceDetail.base.BaseDeviceDetailController
import com.anadolstudio.homehub.feature.home.domain.model.Area

internal interface DeviceDetailController : BaseDeviceDetailController {

    fun onRetryClicked()

    fun onHistoryRetryClicked()

    fun onEditClicked()

    fun onEditNameChanged(value: String)

    fun onEditAreaSelected(area: Area?)

    fun onEditDismissed()

    fun onEditSaveClicked()

}
