package com.anadolstudio.template.feature.home.data.model

import com.anadolstudio.template.feature.home.domain.model.AllowedDomain
import com.anadolstudio.template.feature.home.domain.model.domain.DomainParser
import com.anadolstudio.template.feature.home.domain.model.states.AllowedState.Companion.getAllowedStateByName
import com.anadolstudio.template.feature.home.domain.model.states.HomeAssistantAttribute
import com.anadolstudio.template.feature.home.domain.model.states.HomeAssistantState
import com.anadolstudio.template.feature.home.domain.model.states.toAutomation
import com.anadolstudio.template.feature.home.domain.model.states.toClimate
import com.anadolstudio.template.feature.home.domain.model.states.toHome
import com.anadolstudio.template.feature.home.domain.model.states.toLight
import com.anadolstudio.template.feature.home.domain.model.states.toNumber
import com.anadolstudio.template.feature.home.domain.model.states.toScene
import com.anadolstudio.template.feature.home.domain.model.states.toSelect
import com.anadolstudio.template.feature.home.domain.model.states.toSensor
import com.anadolstudio.template.feature.home.domain.model.states.toSimple
import com.anadolstudio.template.feature.home.domain.model.states.toSwitch
import java.time.OffsetDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

/** GET /api/states, GET /api/states/{entity_id} */
@Serializable
data class StateResponse(
        @SerialName("entity_id") override val entityId: String,
        @SerialName("state") val state: String,
        @SerialName("attributes") val attributes: JsonObject,
        @SerialName("last_changed") val lastChanged: String? = null,
        @SerialName("last_updated") val lastUpdated: String? = null,
) : DomainParser {

    fun toDomain(json: Json): HomeAssistantState<HomeAssistantAttribute> = HomeAssistantState(
            entityId = entityId,
            allowedState = getAllowedStateByName(state),
            attributes = parseAttributes(json),
            lastChanged = lastChanged?.let { OffsetDateTime.parse(it) },
            lastUpdated = lastUpdated?.let { OffsetDateTime.parse(it) },
    )

    private fun parseAttributes(json: Json): HomeAssistantAttribute = when (allowedDomain) {
        AllowedDomain.SENSOR, AllowedDomain.BINARY_SENSOR -> attributes.toSensor(json)
        AllowedDomain.SWITCH -> attributes.toSwitch(json)
        AllowedDomain.LIGHT -> attributes.toLight(json)
        AllowedDomain.CLIMATE -> attributes.toClimate(json)
        AllowedDomain.NUMBER -> attributes.toNumber(json)
        AllowedDomain.SELECT -> attributes.toSelect(json)
        AllowedDomain.ZONE_HOME -> attributes.toHome(json)
        AllowedDomain.AUTOMATION -> attributes.toAutomation(json)
        AllowedDomain.SCENE -> attributes.toScene(json)
        else -> attributes.toSimple(json)
    }
}
