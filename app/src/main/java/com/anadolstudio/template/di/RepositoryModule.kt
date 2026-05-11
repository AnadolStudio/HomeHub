package com.anadolstudio.template.di

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.anadolstudio.template.core.websocket.WebSocketCore
import com.anadolstudio.template.feature.common.data.NightModeRepositoryImpl
import com.anadolstudio.template.feature.common.data.PreferenceRepositoryImpl
import com.anadolstudio.template.feature.common.data.PreferencesStorage
import com.anadolstudio.template.feature.common.data.ResourceRepositoryImpl
import com.anadolstudio.template.feature.common.domain.NightModeRepository
import com.anadolstudio.template.feature.common.domain.PreferenceRepository
import com.anadolstudio.template.feature.common.domain.ResourceRepository
import com.anadolstudio.template.feature.home.data.HomeAssistantDevicesUseCase
import com.anadolstudio.template.feature.home.data.HomeAssistantRepositoryImpl
import com.anadolstudio.template.feature.home.domain.HomeAssistantRepository
import com.anadolstudio.template.feature.homeAssistantAuth.data.api.AuthHomeAssistantApi
import dagger.Module
import dagger.Provides
import javax.inject.Singleton
import kotlinx.serialization.json.Json

@Module
internal class RepositoryModule {

    @Provides
    @Singleton
    fun providePreferences(context: Context): SharedPreferences {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

        return EncryptedSharedPreferences.create(
                PREFS_FILE_NAME,
                masterKeyAlias,
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    @Provides
    fun provideResources(context: Context): Resources = context.resources

    @Provides
    @Singleton
    fun providePreferencesStorage(preferences: SharedPreferences): PreferencesStorage = PreferencesStorage(preferences)

    @Provides
    @Singleton
    fun provideNightModeRepository(resources: Resources, preferences: PreferencesStorage): NightModeRepository =
            NightModeRepositoryImpl(resources, preferences)

    @Provides
    fun provideResourceRepository(context: Context): ResourceRepository = ResourceRepositoryImpl(context)

    @Provides
    fun providePreferenceRepositoryImpl(preferences: PreferencesStorage): PreferenceRepository =
            PreferenceRepositoryImpl(preferences)

    @Provides
    @Singleton
    fun provideHomeAssistantRepositoryImpl(
            api: AuthHomeAssistantApi,
            webSocketCore: WebSocketCore,
            json: Json,
    ): HomeAssistantRepository = HomeAssistantRepositoryImpl(
            api = api,
            webSocketCore = webSocketCore,
            json = json,
    )

    @Provides
    @Singleton
    fun homeAssistantDevicesUseCase(
            repository: HomeAssistantRepository
    ): HomeAssistantDevicesUseCase = HomeAssistantDevicesUseCase(repository)

    private companion object {
        const val PREFS_FILE_NAME = "home_hub_encrypted_prefs"
    }
}
