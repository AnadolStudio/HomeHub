package com.anadolstudio.template.feature.home.domain.model.states

import androidx.core.text.isDigitsOnly
import kotlinx.serialization.Serializable

@Serializable
sealed class AllowedState(val value: String) {

    object On : AllowedState(value = "on")
    object Off : AllowedState(value = "off")
    object Unavailable : AllowedState(value = "unavailable")
    object Unknown : AllowedState(value = "unknown")

    class DigitState(value: String) : AllowedState(value = value)

    class UnprocessedState(value: String) : AllowedState(value = value);

    fun toBooleanOrNull(): Boolean? = when (this) {
        is On -> true
        is Off -> false
        else -> null
    }

    companion object {
        fun getAllowedStateByName(name: String): AllowedState = when {
            name == "on" -> On
            name == "off" -> Off
            name == "unavailable" -> Unavailable
            name == "unknown" -> Unknown
            name.isDigitsOnly() -> DigitState(name)
            else -> UnprocessedState(name)
        }
    }
}

