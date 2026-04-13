package com.anadolstudio.template.feature.autoSetupHomeAssistantUrl.domain.repository

import com.anadolstudio.template.feature.autoSetupHomeAssistantUrl.domain.model.HomeAssistantInstance
import kotlinx.coroutines.flow.Flow

/**
 * Поиск инстансов Home Assistant в локальной сети.
 *
 * Реализация запускает discovery (NSD/mDNS) при подписке на [discover] и эмитит каждый
 * валидный найденный сервер. Discovery останавливается автоматически при отмене подписки.
 *
 * Ограничение по времени и дедупликация — ответственность вызывающей стороны.
 */
interface HomeAssistantDiscoveryRepository {

    fun discover(): Flow<HomeAssistantInstance>
}
