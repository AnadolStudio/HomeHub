package com.anadolstudio.template.feature.home.domain.model

import androidx.compose.runtime.Immutable
import com.anadolstudio.template.feature.home.domain.model.entity.EntityCategory
import com.anadolstudio.template.feature.home.domain.model.entity.HomeAssistantEntity
import com.anadolstudio.template.feature.home.domain.model.states.HomeAssistantAttribute

@Immutable
data class HomeAssistantDevice(
        val id: String,
        val name: String,
        val model: String,
        val modelId: String?,
        val manufacturer: String?,
        val area: Area?,
        val entityMap: Map<EntityCategory, List<HomeAssistantEntity<HomeAssistantAttribute>>>,
) {

    val isBindToArea: Boolean get() = area != null

    val allEntityList: List<HomeAssistantEntity<HomeAssistantAttribute>>
        get() = entityMap.flatMap { (_, entityList) -> entityList }
    val controlEntityList: List<HomeAssistantEntity<HomeAssistantAttribute>>
        get() = entityMap[EntityCategory.CONTROL].orEmpty()
    val configEntityList: List<HomeAssistantEntity<HomeAssistantAttribute>>
        get() = entityMap[EntityCategory.CONFIG].orEmpty()
    val diagnosticEntityList: List<HomeAssistantEntity<HomeAssistantAttribute>>
        get() = entityMap[EntityCategory.DIAGNOSTIC].orEmpty()

    val imageUrl: String?
        get() = when {
            entityMap[EntityCategory.CONTROL].orEmpty().any { it.allowedDomain == AllowedDomain.LIGHT } -> null
            modelId.orEmpty().isNotBlank() -> "https://www.zigbee2mqtt.io/images/devices/$modelId.png" // TODO
            else -> null
        }
}
