package com.anadolstudio.homehub.feature.add_device.addMatter.gms

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
