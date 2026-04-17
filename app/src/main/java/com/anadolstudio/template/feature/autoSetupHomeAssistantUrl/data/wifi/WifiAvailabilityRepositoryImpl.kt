package com.anadolstudio.template.feature.autoSetupHomeAssistantUrl.data.wifi

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import com.anadolstudio.template.feature.autoSetupHomeAssistantUrl.domain.wifi.WifiAvailabilityRepository
import javax.inject.Inject
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onStart

internal class WifiAvailabilityRepositoryImpl @Inject constructor(
        private val connectivityManager: ConnectivityManager,
) : WifiAvailabilityRepository {

    override fun isWifiConnected(): Boolean {
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false

        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    override fun observeWifiAvailability(): Flow<Boolean> = callbackFlow {
        // Слушаем любую сеть с интернетом (без TRANSPORT_WIFI фильтра), а в callback-ах всегда
        // пересчитываем актуальное состояние через isWifiConnected(). Узкий фильтр
        // TRANSPORT_WIFI + INTERNET пропускает события при переключениях Wi-Fi ↔ cellular,
        val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(isWifiConnected())
            }

            override fun onLost(network: Network) {
                trySend(isWifiConnected())
            }

            override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
                trySend(isWifiConnected())
            }
        }

        connectivityManager.registerNetworkCallback(request, callback)
        awaitClose { connectivityManager.unregisterNetworkCallback(callback) }
    }
            .buffer(capacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
            .onStart { emit(isWifiConnected()) }
            .distinctUntilChanged()
}
