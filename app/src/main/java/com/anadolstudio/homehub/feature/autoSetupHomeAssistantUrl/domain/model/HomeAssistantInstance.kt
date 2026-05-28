package com.anadolstudio.homehub.feature.autoSetupHomeAssistantUrl.domain.model

import androidx.core.net.toUri
import kotlinx.serialization.Serializable

@Serializable
data class HomeAssistantInstance(
        val name: String,
        val internalUrl: String,
        val version: HomeAssistantVersion,
        val externalUrl: String? = null,
) {
    private val internalUri get() = internalUrl.toUri()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is HomeAssistantInstance) return false

        return internalUri.host == internalUri.host
    }

    override fun hashCode(): Int = internalUri.host.hashCode()
}
