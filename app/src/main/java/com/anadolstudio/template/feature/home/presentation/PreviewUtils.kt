package com.anadolstudio.template.feature.home.presentation

import com.anadolstudio.template.feature.home.domain.model.Area
import com.anadolstudio.template.feature.home.domain.model.HomeAssistantDevice
import com.anadolstudio.template.feature.home.domain.model.entity.EntityCategory
import com.anadolstudio.template.feature.home.domain.model.entity.HomeAssistantEntity
import com.anadolstudio.template.feature.home.domain.model.states.AllowedState
import com.anadolstudio.template.feature.home.domain.model.states.HomeAssistantAttribute
import com.anadolstudio.template.feature.home.domain.model.states.HomeAssistantState
import com.anadolstudio.template.feature.home.domain.model.states.SimpleAttribute
import java.time.OffsetDateTime
import kotlinx.serialization.json.JsonObject

internal object PreviewUtils {

    fun previewArea(name: String): Area = Area(
            areaId = name.lowercase(),
            name = name,
            humidityEntityIid = null,
            temperatureEntityId = null,
            aliases = emptyList(),
    )

    fun previewState(entityId: String, state: AllowedState): HomeAssistantState<HomeAssistantAttribute> =
            HomeAssistantState(
                    entityId = entityId,
                    attributes = SimpleAttribute(
                            jsonAttributes = JsonObject(emptyMap()),
                            friendlyName = entityId,
                    ),
                    lastChanged = OffsetDateTime.MIN,
                    allowedState = state,
                    lastUpdated = null,
            )

    fun previewEntity(
            entityId: String,
            state: AllowedState = AllowedState.Unknown,
    ): HomeAssistantEntity<HomeAssistantAttribute> =
            HomeAssistantEntity(
                    entityId = entityId,
                    deviceId = "someId",
                    services = setOf("turn_on", "turn_off", "toggle"),
                    entityCategory = EntityCategory.CONTROL,
                    state = previewState(entityId, state),
            )

    val previewDevices: List<HomeAssistantDevice> = listOf(
            HomeAssistantDevice(
                    id = "4f745823d042948d34938086261e40d7",
                    name = "Выключатель Зал/Кухня",
                    model = "Wall switch with 2 buttons",
                    modelId = "ZNCJMB14LM",
                    manufacturer = "Aqara",
                    area = previewArea("Зал"),
                    entityMap = mapOf(
                            EntityCategory.CONTROL to listOf(
                                    previewEntity("switch.name_1"),
                                    previewEntity("switch.name_2", AllowedState.On),
                            ),
                    ),
            ),
            HomeAssistantDevice(
                    id = "416f948a315f700d4ef3ea300f698d1e",
                    name = "Выключатель на балконе",
                    model = "Smart wall switch",
                    modelId = "QBKG11LM",
                    manufacturer = "Aqara",
                    area = previewArea("Балкон"),
                    entityMap = mapOf(
                            EntityCategory.DIAGNOSTIC to listOf(previewEntity("switch.name_1")),
                    ),
            ),
            HomeAssistantDevice(
                    id = "8a1f7d29a04a4b3eb6c9e8410c7a6b22",
                    name = "Лампа в спальне",
                    model = "RGBW light bulb",
                    modelId = "LED1624G9",
                    manufacturer = "IKEA",
                    area = previewArea("Спальня"),
                    entityMap = mapOf(
                            EntityCategory.CONFIG to listOf(previewEntity("switch.name_1")),
                    ),
            ),
    )
}
