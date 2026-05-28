package com.anadolstudio.homehub.feature.home.domain.model.entity

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import com.anadolstudio.homehub.feature.home.domain.model.domain.DomainParser
import com.anadolstudio.homehub.feature.home.domain.model.states.HomeAssistantAttribute
import com.anadolstudio.homehub.feature.home.domain.model.states.HomeAssistantState
import com.anadolstudio.homehub.feature.home.domain.model.states.mapAttributes
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Immutable
@Parcelize
@Serializable
data class HomeAssistantEntity<out Attribute : HomeAssistantAttribute>(
        override val entityId: String,
        val deviceId: String,
        val name: String,
        val platform: String,
        val services: Set<String>,
        val entityCategory: EntityCategory,
        val state: HomeAssistantState<Attribute>,
) : DomainParser, Parcelable

fun <E : HomeAssistantAttribute, T : HomeAssistantAttribute> HomeAssistantEntity<E>.mapAttributes(
        block: (JsonObject) -> T,
): HomeAssistantEntity<T> = HomeAssistantEntity(
        entityId = entityId,
        deviceId = deviceId,
        name = name,
        services = services,
        entityCategory = entityCategory,
        platform = platform,
        state = state.mapAttributes(block)
)

inline fun <reified Attribute : HomeAssistantAttribute> List<HomeAssistantEntity<HomeAssistantAttribute>>.castEntityList() = this
        .filter { it.state.attributes is Attribute }
        .map { it as HomeAssistantEntity<Attribute>}
