package com.anadolstudio.homehub.di

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.anadolstudio.homehub.core.websocket.WebSocketCore
import com.anadolstudio.homehub.feature.common.data.NightModeRepositoryImpl
import com.anadolstudio.homehub.feature.common.data.PreferenceRepositoryImpl
import com.anadolstudio.homehub.feature.common.data.PreferencesStorage
import com.anadolstudio.homehub.feature.common.data.ResourceRepositoryImpl
import com.anadolstudio.homehub.feature.common.domain.NightModeRepository
import com.anadolstudio.homehub.feature.common.domain.PreferenceRepository
import com.anadolstudio.homehub.feature.common.domain.ResourceRepository
import com.anadolstudio.homehub.feature.home.data.HARestRepositoryImpl
import com.anadolstudio.homehub.feature.home.data.HAWebsocketRepositoryImpl
import com.anadolstudio.homehub.feature.home.domain.HARestRepository
import com.anadolstudio.homehub.feature.home.domain.HAWebsocketRepository
import com.anadolstudio.homehub.feature.homeAssistantAuth.data.api.AuthHomeAssistantApi
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
    fun provideHARestRepository(
            api: AuthHomeAssistantApi,
            json: Json,
    ): HARestRepository = HARestRepositoryImpl(
            api = api,
            json = json,
    )

    @Provides
    @Singleton
    fun provideHAWebsocketRepository(
            webSocketCore: WebSocketCore,
            json: Json,
    ): HAWebsocketRepository = HAWebsocketRepositoryImpl(
            webSocketCore = webSocketCore,
            json = json,
    )

    private companion object {
        const val PREFS_FILE_NAME = "home_hub_encrypted_prefs"
    }
}
