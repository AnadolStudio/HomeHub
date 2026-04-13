package com.anadolstudio.template.feature.autoSetupHomeAssistantUrl.domain.wifi

import kotlinx.coroutines.flow.Flow

/**
 * Абстракция над проверкой подключения к Wi-Fi сети.
 */
interface WifiAvailabilityChecker {

    fun isWifiConnected(): Boolean

    /**
     * Поток состояния подключения к Wi-Fi. Эмитит `true`, когда активное сетевое подключение
     * использует Wi-Fi транспорт и имеет доступ в интернет; `false` в остальных случаях.
     * Первое значение — текущее состояние на момент подписки.
     */
    fun observeWifiAvailability(): Flow<Boolean>
}
