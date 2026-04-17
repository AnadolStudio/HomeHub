package com.anadolstudio.template.feature.homeAssistantAuth.data.api

import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

/**
 * Non-auth API — запросы к `auth/token` не требуют `Authorization` заголовка.
 * Используется через [HomeAssistantApiFactory] с базовым (non-auth) OkHttpClient.
 */
interface NonAuthHomeAssistantApi {

    @FormUrlEncoded
    @POST("auth/token")
    suspend fun exchangeToken(
            @Field("grant_type") grantType: String,
            @Field("code") code: String,
            @Field("client_id") clientId: String,
    ): TokenResponse

    @FormUrlEncoded
    @POST("auth/token")
    suspend fun refreshToken(
            @Field("grant_type") grantType: String,
            @Field("refresh_token") refreshToken: String,
            @Field("client_id") clientId: String,
    ): TokenResponse
}
