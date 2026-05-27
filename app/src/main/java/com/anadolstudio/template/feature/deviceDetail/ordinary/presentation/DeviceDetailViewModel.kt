package com.anadolstudio.template.feature.deviceDetail.ordinary.presentation

import androidx.lifecycle.viewModelScope
import com.anadolstudio.template.event.showError
import com.anadolstudio.template.feature.common.domain.ResourceRepository
import com.anadolstudio.template.feature.deviceDetail.base.BaseDeviceDetailViewModel
import com.anadolstudio.template.feature.home.domain.HARestRepository
import com.anadolstudio.template.feature.home.domain.HAWebsocketRepository
import com.anadolstudio.template.feature.home.domain.model.AllowedDomain
import com.anadolstudio.template.feature.home.domain.model.HomeAssistantDevice
import com.anadolstudio.template.feature.home.domain.model.entity.HomeAssistantEntity
import com.anadolstudio.template.feature.home.domain.model.entity.mapAttributes
import com.anadolstudio.template.feature.home.domain.model.states.AutomationAttributes
import com.anadolstudio.template.feature.home.domain.model.states.HomeAssistantAttribute
import com.anadolstudio.template.feature.home.domain.model.states.HomeAssistantState
import com.anadolstudio.template.feature.home.domain.model.states.SceneAttributes
import com.anadolstudio.template.feature.home.domain.model.states.SensorAttributes
import com.anadolstudio.template.feature.home.domain.model.states.toAutomation
import com.anadolstudio.template.feature.home.domain.model.states.toScene
import com.anadolstudio.utils.states.LoadingContext
import com.anadolstudio.utils.states.lce.lceFlow
import com.anadolstudio.utils.states.lce.onEachContent
import com.anadolstudio.utils.states.lce.onEachError
import com.anadolstudio.utils.states.lce.onEachProgressState
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.serialization.json.Json

private const val HISTORY_HOURS_LOOKBACK = 24L
private const val HISTORY_DISPLAY_LIMIT = 15

internal class DeviceDetailViewModel @AssistedInject constructor(
        @Assisted device: HomeAssistantDevice,
        resource: ResourceRepository,
        websocketRepository: HAWebsocketRepository,
        private val restRepository: HARestRepository,
        private val json: Json,
) : BaseDeviceDetailViewModel<OrdinaryDeviceDetailState>(
        device = device,
        extraState = OrdinaryDeviceDetailState(),
        resource = resource,
        websocketRepository = websocketRepository
), DeviceDetailController {

    private var historyJob: Job? = null

    override fun onInitLoad() {
        super.onInitLoad()
        loadRelations(loadingContext = LoadingContext.REFRESH)
        loadHistory(state.device, loadingContext = LoadingContext.REFRESH)
    }

    private fun loadRelations(loadingContext: LoadingContext) {
        lceFlow { loadRelations(state.device) }
                .onEachProgressState(
                        previousState = state.extraState.progressState,
                        loadingContext = loadingContext,
                        onNewProgressState = { newState ->
                            updateState { copy(extraState = extraState.copy(progressState = newState)) }
                        },
                )
                .onEachContent { relations ->
                    val extraState = state.extraState.copy(
                            sceneList = relations.scenes,
                            automationList = relations.automations
                    )

                    updateState { copy(extraState = extraState) }
                }
                .launchIn(viewModelScope)
    }

    private suspend fun loadRelations(device: HomeAssistantDevice): DeviceRelations {
        val deviceEntityIds = device.allEntityList.map { it.entityId }.toSet()
        val allEntities = websocketRepository.getEntityList()

        val scenes = allEntities
                .asSequence()
                .filter { it.allowedDomain == AllowedDomain.SCENE }
                .map { entity -> entity.mapAttributes { jsonObject -> jsonObject.toScene(json) } }
                .filter { scene ->
                    scene.state.attributes.includeEntityIdList.any { id -> id in deviceEntityIds }
                }
                .sortedBy { it.name }
                .toList()

        val automations = allEntities
                .asSequence()
                .filter { it.allowedDomain == AllowedDomain.AUTOMATION }
                .map { entity -> entity.mapAttributes { jsonObject -> jsonObject.toAutomation(json) } }
                .filter { automation -> automation.referencesAnyOf(deviceEntityIds) }
                .sortedBy { it.name }
                .toList()

        return DeviceRelations(scenes = scenes, automations = automations)
    }

    private fun loadHistory(device: HomeAssistantDevice, loadingContext: LoadingContext) {
        historyJob?.cancel()
        val entityIds = device.allEntityList.map { it.entityId }

        historyJob = lceFlow {
            if (entityIds.isEmpty()) {
                return@lceFlow emptyList<DeviceHistoryEntry>() to 0
            }

            val startTime = OffsetDateTime.now().minusHours(HISTORY_HOURS_LOOKBACK)
            val historyByEntity = restRepository.getHistory(
                    timestamp = startTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                    filterEntityId = entityIds.joinToString(","),
                    endTime = null,
                    minimalResponse = false,
                    noAttributes = false,
                    significantChangesOnly = true,
            )
            buildHistoryEntries(device, historyByEntity)
        }
                .onEachProgressState(
                        previousState = state.extraState.historyState.progressState,
                        loadingContext = loadingContext,
                        onNewProgressState = { newProgress ->
                            val extraState = extraState.copy(
                                    historyState = extraState.historyState.copy(progressState = newProgress)
                            )
                            updateState { copy(extraState = extraState) }
                        },
                )
                .onEachContent { (entries, total) ->
                    val extraState = extraState.copy(
                            historyState = extraState.historyState.copy(entries = entries, totalCount = total)
                    )
                    updateState { copy(extraState = extraState) }
                }
                .onEachError {
                    showError(it)
                }
                .launchIn(viewModelScope)
    }

    private fun buildHistoryEntries(
            device: HomeAssistantDevice,
            historyByEntity: List<List<HomeAssistantState<HomeAssistantAttribute>>>,
    ): Pair<List<DeviceHistoryEntry>, Int> {
        val attributesByEntity = device.allEntityList.associateBy { it.entityId }

        val flatEntries = historyByEntity
                .flatten()
                .mapNotNull { historyState ->
                    val entity = attributesByEntity[historyState.entityId] ?: return@mapNotNull null
                    val attribute = entity.state.attributes
                    val rawValue = historyState.allowedState.value
                    val displayValue = when (attribute) {
                        is SensorAttributes -> {
                            val unit = attribute.unitOfMeasurement
                            if (unit.isBlank()) rawValue else "$rawValue $unit"
                        }

                        else -> rawValue
                    }
                    val timestamp =  historyState.lastChanged ?: return@mapNotNull null

                    DeviceHistoryEntry(
                            entityId = historyState.entityId,
                            friendlyName = entity.name.takeIf { it.isNotBlank() } ?: historyState.entityId,
                            value = displayValue,
                            timestamp = timestamp,
                            drawableRes = entity.state.icon.drawableRes,
                    )
                }
                .sortedWith(compareByDescending { it.timestamp })

        val displayList = flatEntries.take(HISTORY_DISPLAY_LIMIT)
        return displayList to flatEntries.size
    }

    override fun onRetryClicked() = loadRelations(loadingContext = LoadingContext.RETRY)

    override fun onHistoryRetryClicked() = loadHistory(state.device, loadingContext = LoadingContext.RETRY)

    private fun HomeAssistantEntity<AutomationAttributes>.referencesAnyOf(entityIds: Set<String>): Boolean {
        if (entityIds.isEmpty()) return false
        val attributesAsString = state.attributes.jsonAttributes.toString()
        return entityIds.any { id -> attributesAsString.contains("\"$id\"") }
    }

    private data class DeviceRelations(
            val scenes: List<HomeAssistantEntity<SceneAttributes>>,
            val automations: List<HomeAssistantEntity<AutomationAttributes>>,
    )

    @AssistedFactory
    interface Factory {
        fun create(device: HomeAssistantDevice): DeviceDetailViewModel
    }
}
