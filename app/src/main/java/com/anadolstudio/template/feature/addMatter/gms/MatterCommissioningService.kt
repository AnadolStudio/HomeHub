package com.anadolstudio.template.feature.addMatter.gms

import android.app.Service
import android.content.Intent
import com.anadolstudio.template.di.DI
import com.anadolstudio.template.feature.addMatter.domain.repository.MatterRepository
import com.google.android.gms.home.matter.commissioning.CommissioningCompleteMetadata
import com.google.android.gms.home.matter.commissioning.CommissioningRequestMetadata
import com.google.android.gms.home.matter.commissioning.CommissioningService
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Точка интеграции с Google Home Mobile SDK для Matter.
 *
 * Жизненный цикл:
 *   1. После того, как `Matter.commissionDevice(...)` запущен из `MatterCommissioningManager`,
 *      Google показывает свой UI и **сам делает BLE-pairing + Wi-Fi handoff** устройству.
 *   2. Когда устройство уже в сети, Google биндится к этому Service и зовёт
 *      [onCommissioningRequested], передавая PIN + IP устройства.
 *   3. Мы шлём в HA WS-команду `matter/commission_on_network` — HA Matter Server
 *      делает CASE-сессию по сети (уже без BLE).
 *   4. Сообщаем Google'у `sendCommissioningComplete(...)` либо `sendCommissioningError(...)`.
 *
 * Результат для UI публикует **сам репозиторий** через свой `outcomes`-flow —
 * Service ничего об этом не знает и просто вызывает метод. ViewModel экрана
 * подписана на тот же flow и обновляет State самостоятельно.
 *
 * `android:exported="true"` в манифесте обязательно — Google делает signature
 * verification, но всё равно нужен binding снаружи (см. HA Companion PR #5509).
 */
internal class MatterCommissioningService : Service(), CommissioningService.Callback {

    @Inject lateinit var matterRepository: MatterRepository

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Делегат от GMS — выставляет AIDL-интерфейс наружу и принимает наши callback'и.
    private val commissioningServiceDelegate by lazy {
        CommissioningService.Builder(this)
                .setCallback(this)
                .build()
    }

    override fun onCreate() {
        super.onCreate()
        // У нашего AppComponent нет автогенерированного inject(service: ...),
        // поэтому пробрасываем зависимости вручную через member-injection.
        DI.appComponent.inject(this)
    }

    override fun onBind(intent: Intent?) = commissioningServiceDelegate.asBinder()

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onCommissioningRequested(metadata: CommissioningRequestMetadata) {
        val pin = metadata.passcode
        val ipAddr = runCatching { metadata.networkLocation.ipAddress.hostAddress }
                .getOrNull()

        Timber.d("Matter onCommissioningRequested: pin=$pin, ip=$ipAddr")

        serviceScope.launch {
            // Репозиторий сам публикует Success/Failure в outcomes — здесь только
            // нужно отрапортовать обратно в GMS. CancellationException пробрасываем,
            // остальные исключения repository уже превратил в Outcome.Failure + false.
            val ok = matterRepository.commissionOnNetwork(pin = pin, ipAddr = ipAddr)
            if (ok) {
                commissioningServiceDelegate.sendCommissioningComplete(
                        CommissioningCompleteMetadata.builder().build(),
                )
            } else {
                commissioningServiceDelegate.sendCommissioningError(COMMISSIONING_ERROR_OTHER)
            }
        }
    }

    private companion object {
        // CommissioningError.OTHER в Google SDK = 0. Используем константу,
        // чтобы не тянуть тип из публичного API напрямую.
        const val COMMISSIONING_ERROR_OTHER = 0
    }
}
