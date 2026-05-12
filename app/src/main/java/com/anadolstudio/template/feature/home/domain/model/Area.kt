package com.anadolstudio.template.feature.home.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class Area(
        val areaId: String,
        val name: String,
        val humidityEntityIid: String?,
        val temperatureEntityId: String?,
        val aliases: List<String>,
)
