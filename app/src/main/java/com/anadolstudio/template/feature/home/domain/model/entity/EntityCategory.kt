package com.anadolstudio.template.feature.home.domain.model.entity

import kotlinx.serialization.Serializable

@Serializable
enum class EntityCategory(val value: String?) {
    CONFIG("config"),
    DIAGNOSTIC("diagnostic"),
    TARGET(null);

    companion object {
        fun fromString(name: String?): EntityCategory = entries
                .firstOrNull { it.value == name?.lowercase() }
                ?: TARGET
    }
}
