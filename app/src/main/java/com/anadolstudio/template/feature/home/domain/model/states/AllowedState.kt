package com.anadolstudio.template.feature.home.domain.model.states

import androidx.core.text.isDigitsOnly

sealed class AllowedState(val value: String) {

    object On : AllowedState(value = "on")
    object Off : AllowedState(value = "off")
    object Unavailable : AllowedState(value = "unavailable")
    object Unknown : AllowedState(value = "unknown")

    class DigitState(value: String) : AllowedState(value = value)

    class UnprocessedState(value: String) : AllowedState(value = value)
}

fun getAllowedStateByName(name: String): AllowedState = when  {
    name == "on" -> AllowedState.On
    name == "off" -> AllowedState.Off
    name == "unavailable" -> AllowedState.Unavailable
    name == "unknown" -> AllowedState.Unknown
    name.isDigitsOnly() -> AllowedState.DigitState(name)
    else -> AllowedState.UnprocessedState(name)
}
