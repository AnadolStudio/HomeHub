package com.anadolstudio.homehub.feature.home.domain.model

enum class AllowedDomain(val prefix: String) {
    SENSOR("sensor"),
    BINARY_SENSOR("binary_sensor"),
    SWITCH("switch"),
    LIGHT("light"),
    CLIMATE("climate"),
    ZONE_HOME("zone.home"),
    SELECT("select"),
    NUMBER("number"),
    PERSON("person"),
    AUTOMATION("automation"),
    SCENE("scene"),
    WEATHER("weather"),
    BUTTON("button");

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
            val regexString = listOf(SENSOR, BINARY_SENSOR, SWITCH, LIGHT, SELECT, CLIMATE, NUMBER).joinToString(
                    separator = "|",
                    transform = { component -> component.prefix }
            )

            return Regex("^$regexString")
        }
    }
}
