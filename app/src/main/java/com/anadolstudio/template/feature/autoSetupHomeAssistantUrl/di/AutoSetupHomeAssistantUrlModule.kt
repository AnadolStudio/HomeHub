package com.anadolstudio.template.feature.autoSetupHomeAssistantUrl.di

import android.content.Context
import android.net.ConnectivityManager
import android.net.nsd.NsdManager
import android.net.wifi.WifiManager
import androidx.lifecycle.ViewModel
import com.anadolstudio.template.di.viewmodel.ViewModelKey
import com.anadolstudio.template.feature.autoSetupHomeAssistantUrl.data.repository.HomeAssistantDiscoveryRepositoryImpl
import com.anadolstudio.template.feature.autoSetupHomeAssistantUrl.data.wifi.WifiAvailabilityRepositoryImpl
import com.anadolstudio.template.feature.autoSetupHomeAssistantUrl.domain.repository.HomeAssistantDiscoveryRepository
import com.anadolstudio.template.feature.autoSetupHomeAssistantUrl.domain.wifi.WifiAvailabilityRepository
import com.anadolstudio.template.feature.autoSetupHomeAssistantUrl.presetnation.AutoSetupHomeAssistantUrlViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap

@Module
internal interface AutoSetupHomeAssistantUrlModule {

    @Binds
    @IntoMap
    @ViewModelKey(AutoSetupHomeAssistantUrlViewModel::class)
    fun bindAutoSetupHomeAssistantUrlViewModel(impl: AutoSetupHomeAssistantUrlViewModel): ViewModel

    @Binds
    fun bindHomeAssistantDiscoveryRepository(
            impl: HomeAssistantDiscoveryRepositoryImpl,
    ): HomeAssistantDiscoveryRepository

    @Binds
    fun bindWifiAvailabilityChecker(impl: WifiAvailabilityRepositoryImpl): WifiAvailabilityRepository

    companion object {

        @Provides
        fun provideNsdManager(context: Context): NsdManager =
                context.getSystemService(Context.NSD_SERVICE) as NsdManager

        @Provides
        fun provideWifiManager(context: Context): WifiManager =
                context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

        @Provides
        fun provideConnectivityManager(context: Context): ConnectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }
}
