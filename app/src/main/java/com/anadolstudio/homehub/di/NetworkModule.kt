package com.anadolstudio.homehub.di

import com.anadolstudio.homehub.BuildConfig
import com.anadolstudio.homehub.core.network.AuthInterceptor
import com.anadolstudio.homehub.core.network.HomeHubAuthenticator
import com.anadolstudio.homehub.feature.common.data.PreferencesStorage
import com.anadolstudio.homehub.feature.homeAssistantAuth.data.api.AuthHomeAssistantApi
import com.anadolstudio.homehub.util.serializer.OffsetDateTimeSerializer
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import java.time.OffsetDateTime
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class Authenticated

@Module
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        serializersModule = SerializersModule {
            contextual(OffsetDateTime::class, OffsetDateTimeSerializer)
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val builder = OkHttpClient.Builder()
                .connectTimeout(1, TimeUnit.MINUTES)
                .readTimeout(1, TimeUnit.MINUTES)
                .writeTimeout(1, TimeUnit.MINUTES)

        if (BuildConfig.DEBUG) {
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            builder.addInterceptor(loggingInterceptor)
        }

        return builder.build()
    }

    @Provides
    @Singleton
    fun provideRetrofitBuilder(
            okHttpClient: OkHttpClient,
            json: Json,
    ): Retrofit.Builder = Retrofit.Builder()
            .client(okHttpClient)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))

    @Provides
    @Authenticated
    @Singleton
    fun provideAuthenticatedOkHttpClient(
            okHttpClient: OkHttpClient,
            authInterceptor: AuthInterceptor,
            homeHubAuthenticator: HomeHubAuthenticator,
    ): OkHttpClient = okHttpClient.newBuilder()
            .addInterceptor(authInterceptor)
            .authenticator(homeHubAuthenticator)
            .build()

    @Provides
    @Singleton
    fun provideAuthHomeAssistantApi(
            @Authenticated okHttpClient: OkHttpClient,
            preferencesStorage: PreferencesStorage,
            json: Json,
    ): AuthHomeAssistantApi {
        val baseUrl = preferencesStorage.baseUrl ?: PLACEHOLDER_BASE_URL

        return Retrofit.Builder()
                .client(okHttpClient)
                .baseUrl(baseUrl)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
                .build()
                .create(AuthHomeAssistantApi::class.java)
    }

    private const val PLACEHOLDER_BASE_URL = "http://localhost/"
}
