package com.anadolstudio.template.feature.home.domain.model.entity

enum class EntityCategory(val value: String?) {
    CONFIG("config"),
    DIAGNOSTIC("diagnostic"),
    CONTROL(null);

    companion object {
        fun fromString(name: String?): EntityCategory = entries
                .firstOrNull { it.value == name?.lowercase() }
                ?: CONTROL
    }
}
