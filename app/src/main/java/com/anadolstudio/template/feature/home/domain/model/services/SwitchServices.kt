package com.anadolstudio.template.feature.home.domain.model.services

sealed class SwitchServices() : Services() {
    object Off : SwitchServices()
    object On : SwitchServices()
    object Toggle : SwitchServices()

    companion object {
        private val map: Map<SwitchServices, String> = mapOf(
                Off to "turn_off",
                On to "turn_on",
                Toggle to "toggle",
        )

        fun toServices(name: String): SwitchServices? = map.entries
                .associate{ (service, serviceName) -> serviceName to service}
                .get(name)

        fun toString(service: SwitchServices): String = map.getValue(service)
    }
}
