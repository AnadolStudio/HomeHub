package com.anadolstudio.homehub.feature.addMatter.presentation

import com.anadolstudio.homehub.base.viewmodel.BaseController

internal interface AddMatterController : BaseController {

    /** «Начать сопряжение» — запрашиваем у Google IntentSender и открываем системный UI. */
    fun onStartCommissioningClicked()

    /** Compose сообщил, что Google UI закрылся (ОК или отменён юзером). */
    fun onGoogleCommissioningUiFinished(canceledByUser: Boolean)

    fun onAddAnotherClicked()
    fun onFinishClicked()
    fun onRetryClicked()

    override fun onBackClicked()
}
