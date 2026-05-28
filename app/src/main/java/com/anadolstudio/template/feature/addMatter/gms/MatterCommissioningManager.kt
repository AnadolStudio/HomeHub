package com.anadolstudio.template.feature.addMatter.gms

import android.content.ComponentName
import android.content.Context
import android.content.IntentSender
import android.os.Build
import androidx.annotation.RequiresApi
import com.google.android.gms.home.matter.Matter
import com.google.android.gms.home.matter.commissioning.CommissioningRequest
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * Тонкая обёртка над `CommissioningClient` Google Home Mobile SDK.
 *
 * Запрашивает у Google `IntentSender`, который Compose-слой запустит как
 * activity-result. После этого Google показывает свой системный bottom-sheet:
 *   • сканирует QR / просит ввести pairing code;
 *   • устанавливает BLE-сессию с устройством;
 *   • передаёт устройству Wi-Fi credentials (или Thread, если есть border router);
 *   • дожидается, пока устройство окажется в сети;
 *   • биндится к нашему [MatterCommissioningService] и отдаёт `pin` + `ip`.
 *
 * `setCommissioningService(...)` — обязательно. Без этого устройство уехало бы
 * в Google fabric, а не в наш (то есть в HA).
 */
internal class MatterCommissioningManager @Inject constructor(
        private val context: Context,
) {

    @RequiresApi(Build.VERSION_CODES.O_MR1)
    suspend fun requestCommissioningIntent(): IntentSender = suspendCancellableCoroutine { continuation ->
        val request = CommissioningRequest.builder()
                .setCommissioningService(
                        ComponentName(context, MatterCommissioningService::class.java),
                )
                .build()

        Matter.getCommissioningClient(context)
                .commissionDevice(request)
                .addOnSuccessListener { intentSender ->
                    if (continuation.isActive) continuation.resume(intentSender)
                }
                .addOnFailureListener { error ->
                    if (continuation.isActive) continuation.resumeWithException(error)
                }
    }
}
