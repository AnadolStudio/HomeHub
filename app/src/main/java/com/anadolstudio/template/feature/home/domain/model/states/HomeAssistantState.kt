package com.anadolstudio.template.feature.home.domain.model.states

import com.anadolstudio.ha_resources.HaIcon
import com.anadolstudio.template.feature.home.domain.model.domain.DomainParser
import java.time.OffsetDateTime
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class HomeAssistantState<out Attributes : HomeAssistantAttribute>(
        override val entityId: String,
        val attributes: Attributes,
        val allowedState: AllowedState,
        @Contextual val lastChanged: OffsetDateTime,
        @Contextual val lastUpdated: OffsetDateTime?,
) : DomainParser

@Serializable
sealed interface HomeAssistantAttribute: Iconable {
    val jsonAttributes: JsonObject
    val friendlyName: String

    override val icon: HaIcon?
        get() = null
}

fun <E : HomeAssistantAttribute, T : HomeAssistantAttribute> HomeAssistantState<E>.mapAttributes(
        block: (JsonObject) -> T,
): HomeAssistantState<T> = HomeAssistantState(
        entityId = entityId,
        allowedState = allowedState,
        lastChanged = lastChanged,
        lastUpdated = lastUpdated,
        attributes = block.invoke(attributes.jsonAttributes)
)
