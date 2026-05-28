package com.anadolstudio.homehub.feature.add_device.addMatter.presentation

import com.anadolstudio.homehub.base.viewmodel.BaseController
import com.anadolstudio.homehub.feature.add_device.common.BaseAddDeviceController

internal interface AddMatterController : BaseAddDeviceController, BaseController {

    fun onStartCommissioningClicked()

    fun onGoogleCommissioningUiFinished(canceledByUser: Boolean)

    fun onAddAnotherClicked()

    fun onFinishClicked()

    fun onRetryClicked()

    override fun onBackClicked()
}
