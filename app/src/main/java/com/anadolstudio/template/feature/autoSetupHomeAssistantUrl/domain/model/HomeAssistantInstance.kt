package com.anadolstudio.template.feature.autoSetupHomeAssistantUrl.domain.model

import android.annotation.SuppressLint
import android.net.Uri
import android.net.nsd.NsdServiceInfo
import androidx.core.net.toUri
import kotlinx.serialization.Serializable

private const val HOME_ASSISTANT_PORT = 8123
private const val ATTRIBUTE_VERSION = "version"
private const val ATTRIBUTE_LOCATION_NAME = "location_name"

@Serializable
data class HomeAssistantInstance(
        val name: String,
        val url: String,
        val version: HomeAssistantVersion,
) {

    private val uri: Uri get() = url.toUri()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is HomeAssistantInstance) return false

        return uri.host == other.uri.host
    }

    override fun hashCode(): Int = uri.host.hashCode()
}

/**
 * Маппит [NsdServiceInfo] в [HomeAssistantInstance]. Возвращает `null`, если обязательные данные
 * отсутствуют или невалидны:
 * - host (IP) должен быть валидным;
 * - атрибут `version` должен присутствовать и парситься в [HomeAssistantVersion].
 *
 * URL формируется в формате `http://<ip>:8123` независимо от анонсированных `base_url`/портов,
 * как это требуется ТЗ.
 */
internal fun NsdServiceInfo.toHomeAssistantInstance(): HomeAssistantInstance? {
    val host = host?.hostAddress?.takeIf { it.isNotBlank() } ?: return null

    val attrs = attributes.orEmpty()
    val versionRaw = attrs[ATTRIBUTE_VERSION]?.toString(Charsets.UTF_8) ?: return null
    val version = HomeAssistantVersion.fromString(versionRaw) ?: return null

    val name = attrs[ATTRIBUTE_LOCATION_NAME]
            ?.toString(Charsets.UTF_8)
            ?.takeIf { it.isNotBlank() }
            ?: return null
    val url = "http://$host:$HOME_ASSISTANT_PORT"

    return HomeAssistantInstance(name = name, url = url, version = version)
}
