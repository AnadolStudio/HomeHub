package com.anadolstudio.homehub.feature.home.domain.model

import com.anadolstudio.homehub.feature.home.data.model.UnitSystem
import java.time.ZoneId

data class Config(
        val components: List<String>,
        val configDir: String,
        val elevation: Double,
        val latitude: Double,
        val longitude: Double,
        val locationName: String,
        val timeZone: ZoneId,
        val unitSystem: UnitSystem,
        val version: String,
        val whitelistExternalDirs: List<String>,
)

