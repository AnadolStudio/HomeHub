package com.anadolstudio.template.feature.homeAssistantAuth.data.api.model

import com.anadolstudio.template.feature.homeAssistantAuth.domain.model.ServiceDomain
import kotlinx.serialization.Serializable

/** Элемент ответа GET /api/services */
@Serializable
data class ServiceDomainResponse(
        val domain: String,
        val services: List<String>,
) {
    fun toDomain(): ServiceDomain = ServiceDomain(domain = domain, services = services)
}
