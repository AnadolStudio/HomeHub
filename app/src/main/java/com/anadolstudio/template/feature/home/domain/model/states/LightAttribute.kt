package com.anadolstudio.template.feature.home.domain.model.states

import android.graphics.Color
import com.anadolstudio.ha_resources.HaIcon
import com.anadolstudio.ha_resources.HaIcons
import com.anadolstudio.template.feature.home.domain.model.states.LightEntityColorMode.HS
import com.anadolstudio.template.feature.home.domain.model.states.LightEntityColorMode.RGB
import com.anadolstudio.template.feature.home.domain.model.states.LightEntityColorMode.Temperature
import com.anadolstudio.template.feature.home.domain.model.states.LightEntityColorMode.XY
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement

@Serializable
data class LightAttribute(
        @Transient override val jsonAttributes: JsonObject = JsonObject(emptyMap()),
        @SerialName("friendly_name") override val friendlyName: String,
        @SerialName("min_color_temp_kelvin") val minTempKelvin: Int? = null,
        @SerialName("max_color_temp_kelvin") val maxTempKelvin: Int? = null,
        @SerialName("supported_color_modes") val supportedColorModes: List<String>? = null,
        @SerialName("color_mode") val colorMode: String? = null,
        @SerialName("brightness") val brightness: Int = 0,
        @SerialName("color_temp_kelvin") val colorTempKelvin: Int? = null,
        @SerialName("hs_color") val hsColor: List<Double>? = null,
        @SerialName("rgb_color") val rgbColor: List<Int>? = null,
        @SerialName("xy_color") val xyColor: List<Double>? = null,
) : HomeAssistantAttribute, Iconable {

    val colorModeList: List<LightEntityColorMode> = listOfNotNull(
            runCatching { HS(hue = hsColor!!.first(), saturation = hsColor.last()) },
            runCatching { Temperature(current = colorTempKelvin!!, min = minTempKelvin!!, max = maxTempKelvin!!) },
            runCatching { XY(x = xyColor!!.first(), y = xyColor.last()) },
            runCatching { RGB(alpha = brightness, red = rgbColor!![0], green = rgbColor[1], blue = rgbColor[2]) }
    ).mapNotNull { it.getOrNull() }

    val color: Int? = colorModeList
            .firstOrNull { it is RGB }
            ?.let { it as? RGB }
            ?.color

    override val icon: HaIcon = requireNotNull(HaIcons.resolve(haIconName = "mdi:lightbulb", tint = color))
}

fun JsonObject.toLight(json: Json): LightAttribute = json
        .decodeFromJsonElement<LightAttribute>(this)
        .copy(jsonAttributes = this)

@Serializable
sealed interface LightEntityColorMode {

    @Serializable
    data class RGB(val alpha: Int, val red: Int, val green: Int, val blue: Int) : LightEntityColorMode {
        val color: Int = Color.argb(alpha, red, green, blue)
    }

    @Serializable
    data class XY(val x: Double, val y: Double) : LightEntityColorMode

    @Serializable
    data class Temperature(val current: Int, val min: Int, val max: Int) : LightEntityColorMode

    @Serializable
    data class HS(val hue: Double, val saturation: Double) : LightEntityColorMode
}
