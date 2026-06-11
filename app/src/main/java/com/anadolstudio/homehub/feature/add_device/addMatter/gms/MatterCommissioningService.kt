package com.anadolstudio.homehub.feature.add_device.addMatter.gms

import android.app.Service
import android.content.Intent
import com.anadolstudio.homehub.di.DI
import com.anadolstudio.homehub.feature.add_device.addMatter.domain.repository.MatterRepository
import com.google.android.gms.home.matter.commissioning.CommissioningCompleteMetadata
import com.google.android.gms.home.matter.commissioning.CommissioningRequestMetadata
import com.google.android.gms.home.matter.commissioning.CommissioningService
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

// android:exported="true" в манифесте обязательно — см. HA Companion PR #5509.
internal class MatterCommissioningService : Service(), CommissioningService.Callback {

    @Inject lateinit var matterRepository: MatterRepository

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val commissioningServiceDelegate by lazy {
        CommissioningService.Builder(this)
                .setCallback(this)
                .build()
    }

    override fun onCreate() {
        super.onCreate()
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

        serviceScope.launch {
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
        const val COMMISSIONING_ERROR_OTHER = 0
    }
}
