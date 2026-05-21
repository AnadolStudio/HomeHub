package com.anadolstudio.template.feature.home.domain.model.states

import androidx.core.text.isDigitsOnly
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class AllowedState {

    abstract val value: String

    @Serializable
    @SerialName("On")
    object On : AllowedState() {
        override val value: String get() = "on"
    }

    @Serializable
    @SerialName("Off")
    object Off : AllowedState() {
        override val value: String get() = "off"
    }

    @Serializable
    @SerialName("Unavailable")
    object Unavailable : AllowedState() {
        override val value: String get() = "unavailable"
    }

    @Serializable
    @SerialName("Unknown")
    object Unknown : AllowedState() {
        override val value: String get() = "unknown"
    }

    @Serializable
    @SerialName("DigitState")
    data class DigitState(override val value: String) : AllowedState()

    @Serializable
    @SerialName("UnprocessedState")
    data class UnprocessedState(override val value: String) : AllowedState()

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

