package com.anadolstudio.template.feature.lightDetail.presentation

internal interface LightDetailController {

    fun onToggleClicked(turnOn: Boolean)

    fun onBrightnessChanged(percent: Int)

    fun onRgbChanged(red: Int, green: Int, blue: Int)

    fun onCloseClicked()
}
