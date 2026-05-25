package com.anadolstudio.template.feature.home.domain.model

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Immutable
@Parcelize
@Serializable
data class Area(
        val areaId: String,
        val name: String,
        val humidityEntityIid: String?,
        val temperatureEntityId: String?,
        val aliases: List<String>,
) : Parcelable
