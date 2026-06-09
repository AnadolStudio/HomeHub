package com.anadolstudio.homehub.feature.home.data.model.automation

import com.anadolstudio.homehub.util.serializer.StringOrListSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AutomationConfigResult(
        @SerialName("config") val config: AutomationConfigResponse,
)

@Serializable
data class AutomationConfigResponse(
        @SerialName("id") val id: String? = null,
        @SerialName("alias") val alias: String? = null,
        @SerialName("description") val description: String? = null,
        @SerialName("mode") val mode: String? = null,
        @SerialName("triggers") val triggers: List<AutomationTriggerResponse> = emptyList(),
        @SerialName("actions") val actions: List<AutomationActionResponse> = emptyList(),
)

@Serializable
data class AutomationTriggerResponse(
        @SerialName("trigger") val trigger: String? = null,
        @SerialName("entity_id")
        @Serializable(with = StringOrListSerializer::class)
        val entityId: List<String> = emptyList(),
)

@Serializable
data class AutomationActionResponse(
        @SerialName("action") val action: String? = null,
        @SerialName("target") val target: AutomationActionTarget? = null,
)

@Serializable
data class AutomationActionTarget(
        @SerialName("entity_id")
        @Serializable(with = StringOrListSerializer::class)
        val entityId: List<String> = emptyList(),
)
