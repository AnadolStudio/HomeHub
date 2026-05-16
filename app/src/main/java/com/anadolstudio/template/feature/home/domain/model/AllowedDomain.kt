package com.anadolstudio.template.feature.home.domain.model

enum class AllowedDomain(val prefix: String) {
    SENSOR("sensor"),
    BINARY_SENSOR("binary_sensor"),
    SWITCH("switch"),
    LIGHT("light"),
    ZONE_HOME("zone.home"),
    SELECT("select"), // select.0x603d61fffe758b32_power_on_behavior_1,
    PERSON("person"),
    AUTOMATION("automation"),
    SCENE("scene"),
    WEATHER("weather"),
    BUTTON("button"); // bulb_gx53_color_identifikatsiia_2

    companion object {

        fun getByName(name: String): AllowedDomain? = entries.firstOrNull { it.prefix == name.lowercase() }

        fun getRegex(): Regex {
            val regexString = entries.joinToString(
                    separator = "|",
                    transform = { component -> component.prefix }
            )

            return Regex("^$regexString")
        }

        fun getZigbeeAndMatterComponentsRegex(): Regex {
            val regexString = listOf(SENSOR, BINARY_SENSOR, SWITCH, LIGHT).joinToString(
                    separator = "|",
                    transform = { component -> component.prefix }
            )

            return Regex("^$regexString")
        }
    }
}
