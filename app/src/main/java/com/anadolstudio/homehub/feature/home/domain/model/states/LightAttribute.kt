package com.anadolstudio.homehub.feature.home.domain.model.states

import android.graphics.Color
import com.anadolstudio.ha_resources.HaIcon
import com.anadolstudio.ha_resources.HaIcons
import com.anadolstudio.homehub.feature.home.domain.model.states.LightEntityColorMode.HS
import com.anadolstudio.homehub.feature.home.domain.model.states.LightEntityColorMode.RGB
import com.anadolstudio.homehub.feature.home.domain.model.states.LightEntityColorMode.Temperature
import com.anadolstudio.homehub.feature.home.domain.model.states.LightEntityColorMode.XY
import com.anadolstudio.homehub.util.parcel.JsonObjectParceler
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.TypeParceler
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement

@Parcelize
@TypeParceler<JsonObject, JsonObjectParceler>()
@Serializable
data class LightAttribute(
        @Transient override val jsonAttributes: JsonObject = JsonObject(emptyMap()),
        @SerialName("friendly_name") override val friendlyName: String = "",
        @SerialName("min_color_temp_kelvin") val minKelvin: Int? = null,
        @SerialName("max_color_temp_kelvin") val maxKelvin: Int? = null,
        @SerialName("supported_color_modes") val supportedColorModes: List<String> = emptyList(),
        @SerialName("color_mode") val colorMode: String? = null,
        @SerialName("brightness") val brightness: Int? = null,
        @SerialName("color_temp_kelvin") val colorKelvin: Int? = null,
        @SerialName("hs_color") val hsColor: List<Double> = emptyList(),
        @SerialName("rgb_color") val rgbColor: List<Int> = emptyList(),
        @SerialName("xy_color") val xyColor: List<Double> = emptyList(),
        @Transient override val icon: HaIcon = requireNotNull(HaIcons.resolve(haIconName = "mdi:lightbulb"))
) : HomeAssistantAttribute, Iconable {

    val colorModeList: List<LightEntityColorMode> get() = listOfNotNull(
            runCatching {
                if (!supportedColorModes.contains("hs")) throw IllegalArgumentException("not supported hs")
                val hue = hsColor.firstOrNull() ?: 0.0
                val saturation = hsColor.lastOrNull() ?: 0.0
                HS(hue = hue, saturation = saturation, hasValue = hsColor.size == 2)
            },
            runCatching {
                if (!supportedColorModes.contains("color_temp")) throw IllegalArgumentException("not supported color_temp")
                val current = colorKelvin ?: 0
                Temperature(current = current, min = minKelvin!!, max = maxKelvin!!, hasValue = colorKelvin != null)
            },
            runCatching {
                if (!supportedColorModes.contains("xy")) throw IllegalArgumentException("not supported xy")
                XY(x = xyColor.firstOrNull() ?: 0.0, y = xyColor.lastOrNull() ?: 0.0, hasValue = xyColor.size == 2)
            },
            runCatching {
                val r = rgbColor.getOrNull(0)
                val g = rgbColor.getOrNull(1)
                val b = rgbColor.getOrNull(2)
                val a = brightness ?: 0
                RGB(alpha = a, red = r ?: 0, green = g ?: 0, blue = b ?: 0, hasValue = rgbColor.size == 3)
            }
    ).mapNotNull { it.getOrNull() }

    val color: Int? get() = colorModeList
            .firstOrNull { it is RGB }
            ?.let { it as? RGB }
            ?.color
}

fun JsonObject.toLight(json: Json): LightAttribute = json
        .decodeFromJsonElement<LightAttribute>(this)
        .copy(jsonAttributes = this)

@Serializable
sealed interface LightEntityColorMode {
    val hasValue: Boolean

    @Serializable
    data class RGB(
            val alpha: Int,
            val red: Int,
            val green: Int,
            val blue: Int,
            override val hasValue: Boolean,
    ) : LightEntityColorMode {
        val color: Int = Color.argb(alpha, red, green, blue)
    }

    @Serializable
    data class XY(
            val x: Double,
            val y: Double,
            override val hasValue: Boolean,
    ) : LightEntityColorMode

    @Serializable
    data class Temperature(
            val current: Int,
            val min: Int,
            val max: Int,
            override val hasValue: Boolean,
    ) : LightEntityColorMode

    @Serializable
    data class HS(
            val hue: Double,
            val saturation: Double,
            override val hasValue: Boolean,
    ) : LightEntityColorMode
}
