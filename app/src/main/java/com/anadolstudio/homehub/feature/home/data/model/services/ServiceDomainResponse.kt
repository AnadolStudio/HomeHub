import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

@Serializable
data class ServiceDomainResponse(
        @SerialName("domain") val domain: String,
        @SerialName("services") val services: Map<String, HaServiceResponse> = emptyMap()
)

@Serializable
data class HaServiceResponse(
        @SerialName("name") val name: String? = null,
        @SerialName("description") val description: String? = null,
        @SerialName("fields") val fields: Map<String, HaServiceFieldResponse> = emptyMap(),
        @SerialName("target") val target: HaServiceTargetResponse? = null,
        @SerialName("response") val response: HaServiceCallResponse? = null
)

@Serializable
data class HaServiceFieldResponse(
        @SerialName("name") val name: String? = null,
        @SerialName("description") val description: String? = null,
        @SerialName("required") val required: Boolean? = null,
        @SerialName("advanced") val advanced: Boolean? = null,
        @SerialName("collapsed") val collapsed: Boolean? = null,
        @SerialName("default") val defaultValue: JsonElement? = null,
        @SerialName("example") val example: JsonElement? = null,
        @SerialName("selector") val selector: JsonObject? = null,
        @SerialName("filter") val filter: HaServiceFilterResponse? = null,
        @SerialName("fields") val fields: Map<String, HaServiceFieldResponse> = emptyMap()
)

@Serializable
data class HaServiceFilterResponse(
        @SerialName("supported_features") val supportedFeatures: List<Int>? = null,
        @SerialName("attribute") val attribute: Map<String, JsonElement>? = null
)

@Serializable
data class HaServiceTargetResponse(
        @SerialName("entity") val entity: List<HaTargetEntityResponse>? = null
)

@Serializable
data class HaTargetEntityResponse(
        @SerialName("domain") val domain: List<String>? = null,
        @SerialName("integration") val integration: String? = null,
        @SerialName("supported_features") val supportedFeatures: List<Int>? = null
)

@Serializable
data class HaServiceCallResponse(
        @SerialName("optional") val optional: Boolean? = null
)
