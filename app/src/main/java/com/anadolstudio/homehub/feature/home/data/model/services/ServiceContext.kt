package com.anadolstudio.homehub.feature.home.data.model.services

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Контекст вызова сервиса HA: уникальный id события, опциональные родительский id и id пользователя.
 */
@Serializable
data class ServiceContext(
    @SerialName("id") val id: String,
    @SerialName("parent_id") val parentId: String? = null,
    @SerialName("user_id") val userId: String? = null,
)
