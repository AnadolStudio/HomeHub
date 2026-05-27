package com.anadolstudio.template.feature.home.domain.model.states

import android.os.Parcelable
import com.anadolstudio.ha_resources.HaIcon
import com.anadolstudio.ha_resources.HaIcons
import com.anadolstudio.template.feature.home.domain.model.domain.DomainParser
import com.anadolstudio.template.util.parcel.OffsetDateTimeParceler
import java.time.OffsetDateTime
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.TypeParceler
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Parcelize
@TypeParceler<OffsetDateTime, OffsetDateTimeParceler>()
@Serializable
data class HomeAssistantState<out Attributes : HomeAssistantAttribute>(
        override val entityId: String,
        val attributes: Attributes,
        val allowedState: AllowedState,
        @Contextual val lastChanged: OffsetDateTime? = null,
        @Contextual val lastUpdated: OffsetDateTime? = null,
) : DomainParser, Iconable, Parcelable {

    override val icon: HaIcon
        get() = attributes.icon ?: HaIcons.resolveDefaultIcon(domain, allowedState.value)
}

@Serializable
sealed interface HomeAssistantAttribute : Iconable, Parcelable {
    val jsonAttributes: JsonObject
    val friendlyName: String
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
