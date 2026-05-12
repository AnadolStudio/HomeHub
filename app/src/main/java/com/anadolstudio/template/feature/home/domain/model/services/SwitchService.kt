package com.anadolstudio.template.feature.home.domain.model.services

sealed interface SwitchService : HomeAssistantService {

    override fun toStringService(): String = toStringName(this)

    object Off : SwitchService
    object On : SwitchService
    object Toggle : SwitchService

    companion object {
        private val map: Map<SwitchService, String> = mapOf(
                Off to "turn_off",
                On to "turn_on",
                Toggle to "toggle",
        )

        fun toServices(name: String): SwitchService? = map.entries
                .associate{ (service, serviceName) -> serviceName to service}
                .get(name)

        fun toStringName(service: SwitchService): String = map[service].orEmpty()
    }
}
