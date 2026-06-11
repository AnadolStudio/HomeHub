package com.anadolstudio.homehub.feature.home.domain.model

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import com.anadolstudio.ha_resources.HaIcon
import com.anadolstudio.homehub.feature.home.domain.model.DeviceImage.ImageUrlType
import com.anadolstudio.homehub.feature.home.domain.model.entity.EntityCategory
import com.anadolstudio.homehub.feature.home.domain.model.entity.HomeAssistantEntity
import com.anadolstudio.homehub.feature.home.domain.model.states.HomeAssistantAttribute
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Immutable
@Parcelize
@Serializable
data class HomeAssistantDevice(
        val id: String,
        val name: String,
        val model: String,
        val modelId: String?,
        val manufacturer: String?,
        val area: Area?,
        val entityMap: Map<EntityCategory, List<HomeAssistantEntity<HomeAssistantAttribute>>>,
        val disabledBy: String? = null,
        val labels: List<String> = emptyList(),
) : Parcelable {

    val isBindToArea: Boolean get() = area != null

    val allEntityList: List<HomeAssistantEntity<HomeAssistantAttribute>>
        get() = entityMap.flatMap { (_, entityList) -> entityList }
    val targetEntityList: List<HomeAssistantEntity<HomeAssistantAttribute>>
        get() = entityMap[EntityCategory.TARGET].orEmpty()
    val configEntityList: List<HomeAssistantEntity<HomeAssistantAttribute>>
        get() = entityMap[EntityCategory.CONFIG].orEmpty()
    val diagnosticEntityList: List<HomeAssistantEntity<HomeAssistantAttribute>>
        get() = entityMap[EntityCategory.DIAGNOSTIC].orEmpty()

    val image: DeviceImage?
        get() {
            val lightEntity = entityMap[EntityCategory.TARGET].orEmpty()
                    .firstOrNull { it.allowedDomain == AllowedDomain.LIGHT }
            return when {
                lightEntity != null -> lightEntity.state.attributes.icon?.let { DeviceImage.HaIconType(it) }
                !modelId.isNullOrBlank() -> ImageUrlType("https://www.zigbee2mqtt.io/images/devices/$modelId.png")
                else -> null
            }
        }
}

sealed interface DeviceImage {
    data class ImageUrlType(val url: String) : DeviceImage
    data class HaIconType(val haIcon: HaIcon) : DeviceImage
}
