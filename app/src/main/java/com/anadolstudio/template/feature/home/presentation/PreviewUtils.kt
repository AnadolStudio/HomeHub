package com.anadolstudio.template.feature.home.presentation

import com.anadolstudio.template.feature.home.domain.model.Area
import com.anadolstudio.template.feature.home.domain.model.HomeAssistantDevice
import com.anadolstudio.template.feature.home.domain.model.HomeAssistantEntity
import com.anadolstudio.template.feature.home.domain.model.states.AllowedState
import com.anadolstudio.template.feature.home.domain.model.states.HomeAssistantState
import com.anadolstudio.template.feature.home.domain.model.states.SimpleState
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

     fun previewState(entityId: String, state: AllowedState): HomeAssistantState = SimpleState(
            entityId = entityId,
            state = state,
            jsonAttributes = JsonObject(emptyMap()),
            lastChanged = OffsetDateTime.MIN,
            lastUpdated = null,
    )

     fun previewEntity(entityId: String, state: AllowedState = AllowedState.Unknown): HomeAssistantEntity = HomeAssistantEntity(
            entityId = entityId,
            services = setOf("turn_on", "turn_off", "toggle"),
            allowedState = state,
            stateData = previewState(entityId, state),
    )

     val previewDevices: List<HomeAssistantDevice> = listOf(
            HomeAssistantDevice(
                    id = "4f745823d042948d34938086261e40d7",
                    name = "Выключатель Зал/Кухня",
                    model = "Wall switch with 2 buttons",
                    modelId = "ZNCJMB14LM",
                    manufacturer = "Aqara",
                    area = previewArea("Зал"),
                    entityList = listOf(
                            previewEntity("switch.vykliuchatel_zal_kukhnia_1"),
                            previewEntity("switch.vykliuchatel_zal_kukhnia_kukhnia", AllowedState.On),
                    ),
            ),
            HomeAssistantDevice(
                    id = "416f948a315f700d4ef3ea300f698d1e",
                    name = "Выключатель на балконе",
                    model = "Smart wall switch",
                    modelId = "QBKG11LM",
                    manufacturer = "Aqara",
                    area = previewArea("Балкон"),
                    entityList = listOf(
                            previewEntity("switch.0x603d61fffe758b32_1"),
                    ),
            ),
            HomeAssistantDevice(
                    id = "8a1f7d29a04a4b3eb6c9e8410c7a6b22",
                    name = "Лампа в спальне",
                    model = "RGBW light bulb",
                    modelId = "LED1624G9",
                    manufacturer = "IKEA",
                    area = previewArea("Спальня"),
                    entityList = listOf(
                            previewEntity("light.bedroom_main"),
                    ),
            ),
    )
}
