package com.anadolstudio.template.feature.home.data.model

import kotlinx.serialization.Serializable

@Serializable
data class UnitSystem(
        val length: String,
        val mass: String,
        val temperature: String,
        val volume: String,
)
