package com.anadolstudio.homehub.feature.addMatter.domain.repository

import com.anadolstudio.homehub.event.Text
import kotlinx.coroutines.flow.SharedFlow

internal interface MatterRepository {


    val outcomes: SharedFlow<Outcome>

    /**
     * Финализирующий commissioning по сетевому адресу.
     * Вызывается из `MatterCommissioningService` после того, как
     * Google Home Mobile SDK выполнил BLE-pairing и Wi-Fi handoff.
     *
     * Под капотом публикует [Outcome.Success] / [Outcome.Failure] в [outcomes].
     *
     * @return `true`, если HA подтвердил `success: true`, иначе `false`.
     */
    suspend fun commissionOnNetwork(pin: Long, ipAddr: String?): Boolean

    /**
     * Fallback-сценарий: commissioning по pairing-коду (multi-admin share
     * из Google Home через intent ACTION_COMMISSION_DEVICE).
     */
    suspend fun commissionWithCode(setupCode: String): Boolean

    /** Сбросить replay-кеш [outcomes] перед новым заходом на экран. */
    fun resetOutcomes()

    sealed interface Outcome {
        data object Success : Outcome

        /**
         * [message] — обёртка [Text], чтобы из data-слоя (без Context) можно было
         * отдать как готовую строку из системы (`Text.Plain(error.message)`),
         * так и ссылку на строковый ресурс (`Text.Resource(R.string.add_matter_error_...)`).
         * UI резолвит её через `Resources` в момент отображения.
         */
        data class Failure(val message: Text) : Outcome
    }
}
