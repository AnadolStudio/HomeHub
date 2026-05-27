package com.anadolstudio.template.feature.homeAssistantAuth.data

import androidx.core.net.toUri
import com.anadolstudio.template.feature.autoSetupHomeAssistantUrl.domain.repository.HomeAssistantDiscoveryRepository
import com.anadolstudio.template.feature.autoSetupHomeAssistantUrl.domain.wifi.WifiAvailabilityRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.firstOrNull

/**
 * Определяет, выполнен ли вход через internal_url Home Assistant.
 *
 * Вход считается выполненным через internal_url, если одновременно:
 * - устройство подключено к Wi-Fi (TRANSPORT_WIFI);
 * - в текущей сети обнаружен экземпляр HA через mDNS (_home-assistant._tcp.local.);
 * - TXT-атрибут `internal_url` resolved-сервиса совпадает с переданным [url].
 */
internal class CheckInternalUrlUseCase @Inject constructor(
        private val wifiAvailabilityRepository: WifiAvailabilityRepository,
        private val discoveryRepository: HomeAssistantDiscoveryRepository,
) {

    suspend fun isInternalUrl(url: String): Boolean {
        if (!wifiAvailabilityRepository.isWifiConnected()) return false

        val targetUri = url.toUri()

        val match = discoveryRepository.discover().firstOrNull { instance ->
            instance.internalUrl.toUri() == targetUri
        }

        return match != null
    }
}
