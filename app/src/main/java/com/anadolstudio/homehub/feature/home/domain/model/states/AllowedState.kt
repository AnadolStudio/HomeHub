package com.anadolstudio.homehub.feature.home.domain.model.states

import android.os.Parcelable
import androidx.core.text.isDigitsOnly
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class AllowedState : Parcelable {

    abstract val value: String

    @Parcelize
    @Serializable
    @SerialName("On")
    object On : AllowedState() {
        override val value: String get() = "on"
    }

    @Parcelize
    @Serializable
    @SerialName("Off")
    object Off : AllowedState() {
        override val value: String get() = "off"
    }

    @Parcelize
    @Serializable
    @SerialName("Unavailable")
    object Unavailable : AllowedState() {
        override val value: String get() = "unavailable"
    }

    @Parcelize
    @Serializable
    @SerialName("Unknown")
    object Unknown : AllowedState() {
        override val value: String get() = "unknown"
    }

    @Parcelize
    @Serializable
    @SerialName("DigitState")
    data class DigitState(override val value: String) : AllowedState()

    @Parcelize
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

