package com.anadolstudio.homehub.core.network

import javax.inject.Inject
import retrofit2.Retrofit

class HomeAssistantApiFactory @Inject constructor(
        private val retrofitBuilder: Retrofit.Builder,
) {

    fun <T> create(baseUrl: String, service: Class<T>): T =
            retrofitBuilder
                    .baseUrl(baseUrl)
                    .build()
                    .create(service)
}
