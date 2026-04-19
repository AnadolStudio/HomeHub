package com.anadolstudio.template.feature.home.data.model

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
