package com.anadolstudio.template.feature.homeAssistantAuth.data.api.model

import com.anadolstudio.template.feature.homeAssistantAuth.domain.model.Config
import java.time.ZoneId
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** GET /api/config */
@Serializable
data class ConfigResponse(
        val components: List<String>,
        @SerialName("config_dir") val configDir: String,
        val elevation: Double,
        val latitude: Double,
        val longitude: Double,
        @SerialName("location_name") val locationName: String,
        @SerialName("time_zone") val timeZone: String,
        @SerialName("unit_system") val unitSystem: UnitSystem,
        val version: String,
        @SerialName("whitelist_external_dirs") val whitelistExternalDirs: List<String>,
) {
    fun toDomain(): Config = Config(
            components = components,
            configDir = configDir,
            elevation = elevation,
            latitude = latitude,
            longitude = longitude,
            locationName = locationName,
            timeZone = ZoneId.of(timeZone),
            unitSystem = unitSystem,
            version = version,
            whitelistExternalDirs = whitelistExternalDirs,
    )
}

