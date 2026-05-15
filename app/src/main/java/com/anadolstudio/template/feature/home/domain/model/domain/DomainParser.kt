package com.anadolstudio.template.feature.home.domain.model.domain

import com.anadolstudio.template.feature.home.domain.model.AllowedDomain

interface DomainParser {
    val entityId: String

    val domain: String get() = entityId.split(".").first()
    val allowedDomain: AllowedDomain? get() = AllowedDomain.getByName(domain)
}
