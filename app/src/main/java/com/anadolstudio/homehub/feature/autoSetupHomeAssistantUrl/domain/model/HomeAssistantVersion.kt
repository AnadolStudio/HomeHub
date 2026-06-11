@file:OptIn(InternalSerializationApi::class)

package com.anadolstudio.homehub.feature.autoSetupHomeAssistantUrl.domain.model

import java.util.regex.Pattern
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable

@Serializable
data class HomeAssistantVersion(
        val year: Int,
        val month: Int,
        val release: Int,
) {

    override fun toString(): String = "$year.$month.$release"

    fun isAtLeast(minYear: Int, minMonth: Int, minRelease: Int = 0): Boolean =
            year > minYear ||
                    (year == minYear && (month > minMonth || (month == minMonth && release >= minRelease)))

    companion object {

        private val VERSION_PATTERN = Pattern.compile("([0-9]{4})\\.([0-9]{1,2})\\.([0-9]{1,2}).*")

        fun fromString(versionString: String): HomeAssistantVersion? {
            val matches = VERSION_PATTERN.matcher(versionString)

            return if (matches.find() && matches.matches()) {
                val coreYear = matches.group(1)?.toIntOrNull() ?: 0
                val coreMonth = matches.group(2)?.toIntOrNull() ?: 0
                val coreRelease = matches.group(3)?.toIntOrNull() ?: 0
                HomeAssistantVersion(coreYear, coreMonth, coreRelease)
            } else {
                null
            }
        }
    }
}
