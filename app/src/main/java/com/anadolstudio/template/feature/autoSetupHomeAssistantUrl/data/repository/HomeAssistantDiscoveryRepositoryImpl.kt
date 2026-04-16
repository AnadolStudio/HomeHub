package com.anadolstudio.template.feature.autoSetupHomeAssistantUrl.data.repository

import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.net.wifi.WifiManager
import com.anadolstudio.template.feature.autoSetupHomeAssistantUrl.domain.model.HomeAssistantInstance
import com.anadolstudio.template.feature.autoSetupHomeAssistantUrl.domain.model.toHomeAssistantInstance
import com.anadolstudio.template.feature.autoSetupHomeAssistantUrl.domain.repository.HomeAssistantDiscoveryRepository
import javax.inject.Inject
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import timber.log.Timber

private const val SERVICE_TYPE = "_home-assistant._tcp."
private const val MULTICAST_LOCK_TAG = "HomeHub.HomeAssistantDiscovery"

/**
 * Реализация поиска инстансов Home Assistant через Android NSD (mDNS).
 *
 * Алгоритм:
 * 1. Поднимаем `WifiManager.MulticastLock` — без него mDNS на многих устройствах не работает стабильно.
 * 2. Стартуем `NsdManager.discoverServices(_home-assistant._tcp)`.
 * 3. Каждое `onServiceFound` отправляем во внутренний канал.
 * 4. Параллельная корутина последовательно (через канал) `resolveService(...)` каждый кандидат —
 *    `NsdManager.resolveService` нельзя вызывать конкурентно для нескольких сервисов.
 * 5. Успешно зарезолвленный сервис маппим в [HomeAssistantInstance] и эмитим во внешний `Flow`.
 * 6. На отмену подписки — освобождаем `MulticastLock` и останавливаем discovery.
 */
internal class HomeAssistantDiscoveryRepositoryImpl @Inject constructor(
        private val nsdManager: NsdManager,
        private val wifiManager: WifiManager,
) : HomeAssistantDiscoveryRepository {

    override fun discover(): Flow<HomeAssistantInstance> = callbackFlow {
        val multicastLock = wifiManager.createMulticastLock(MULTICAST_LOCK_TAG).apply {
            setReferenceCounted(false)
            acquire()
        }

        val resolveQueue = Channel<NsdServiceInfo>(capacity = Channel.UNLIMITED)

        val discoveryListener = object : NsdManager.DiscoveryListener {
            override fun onDiscoveryStarted(serviceType: String) = Unit

            override fun onDiscoveryStopped(serviceType: String) = Unit

            override fun onServiceFound(serviceInfo: NsdServiceInfo) {
                Timber.tag(TAG).d("Service found: %s", serviceInfo.serviceName)
                resolveQueue.trySend(serviceInfo)
            }

            override fun onServiceLost(serviceInfo: NsdServiceInfo) = Unit

            override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
                close(IllegalStateException("NSD start failed: code=$errorCode"))
            }

            override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) = Unit
        }

        // Резолвим сервисы строго последовательно — параллельные resolveService приводят к ошибкам.
        val resolveJob = launch {
            for (candidate in resolveQueue) {
                val instance = resolveServiceAwait(candidate)
                        ?.toHomeAssistantInstance()
                        ?: continue

                trySend(instance)
            }
        }

        try {
            nsdManager.discoverServices(
                    SERVICE_TYPE,
                    NsdManager.PROTOCOL_DNS_SD,
                    discoveryListener
            )
        } catch (error: IllegalArgumentException) {
            close(error)
        }

        awaitClose {
            resolveQueue.close()
            resolveJob.cancel()

            runCatching { nsdManager.stopServiceDiscovery(discoveryListener) }
                    .onFailure { Timber.tag(TAG).w(it, "stopServiceDiscovery failed") }
            runCatching { multicastLock.release() }
                    .onFailure { Timber.tag(TAG).w(it, "MulticastLock release failed") }
        }
    }

    private suspend fun resolveServiceAwait(serviceInfo: NsdServiceInfo): NsdServiceInfo? {
        val deferred = CompletableDeferred<NsdServiceInfo?>()

        val listener = object : NsdManager.ResolveListener {
            override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                deferred.complete(null)
            }

            override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
                deferred.complete(serviceInfo)
            }
        }

        return try {
            nsdManager.resolveService(serviceInfo, listener)
            deferred.await()
        } catch (error: IllegalArgumentException) {
            Timber.tag(TAG).w(error, "resolveService threw")
            null
        }
    }

    private companion object {
        const val TAG = "HA_Discovery"
    }
}
