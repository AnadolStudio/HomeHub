package com.anadolstudio.homehub.feature.deviceDetail.ordinary.presentation

import com.anadolstudio.homehub.feature.deviceDetail.base.BaseDeviceDetailController

internal interface DeviceDetailController : BaseDeviceDetailController {

    fun onRetryClicked()

    fun onHistoryRetryClicked()

}
