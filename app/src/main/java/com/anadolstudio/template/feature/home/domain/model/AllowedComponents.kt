package com.anadolstudio.template.feature.home.domain.model

enum class AllowedComponents(val prefix: String) {
    SENSOR("sensor"),
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

        fun getAllComponentsRegex(): Regex {
            val regexString = entries.joinToString(
                    separator = "|",
                    transform = { component -> component.prefix }
            )

            return Regex("^$regexString")
        }

        fun getZigbeeAndMatterComponentsRegex(): Regex {
            val regexString = listOf(SENSOR, SWITCH, LIGHT).joinToString(
                    separator = "|",
                    transform = { component -> component.prefix }
            )

            return Regex("^$regexString")
        }
    }
}
