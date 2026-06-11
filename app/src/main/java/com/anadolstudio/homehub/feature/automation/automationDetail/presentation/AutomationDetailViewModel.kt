package com.anadolstudio.homehub.feature.automation.automationDetail.presentation

import androidx.lifecycle.viewModelScope
import com.anadolstudio.homehub.R
import com.anadolstudio.homehub.base.viewmodel.StatefulViewModel
import com.anadolstudio.homehub.event.showError
import com.anadolstudio.homehub.event.showMessage
import com.anadolstudio.homehub.feature.automation.automationDetail.presentation.logic_block.ConditionEntity
import com.anadolstudio.homehub.feature.automation.automationDetail.presentation.logic_block.ConditionValue
import com.anadolstudio.homehub.feature.automation.automationDetail.presentation.logic_block.LogicNode
import com.anadolstudio.homehub.feature.automation.automationDetail.presentation.logic_block.LogicOperator
import com.anadolstudio.homehub.feature.automation.automationDetail.presentation.logic_block.MAX_LOGIC_DEPTH
import com.anadolstudio.homehub.feature.automation.automationDetail.presentation.logic_block.MoveResult
import com.anadolstudio.homehub.feature.automation.automationDetail.presentation.logic_block.addCondition
import com.anadolstudio.homehub.feature.automation.automationDetail.presentation.logic_block.deleteNode
import com.anadolstudio.homehub.feature.automation.automationDetail.presentation.logic_block.depthOf
import com.anadolstudio.homehub.feature.automation.automationDetail.presentation.logic_block.findLeaf
import com.anadolstudio.homehub.feature.automation.automationDetail.presentation.logic_block.moveByFlatIndex
import com.anadolstudio.homehub.feature.automation.automationDetail.presentation.logic_block.toggleCollapsed
import com.anadolstudio.homehub.feature.automation.automationDetail.presentation.logic_block.updateEntityValue
import com.anadolstudio.homehub.feature.automation.automationDetail.presentation.logic_block.updateLeafEntities
import com.anadolstudio.homehub.feature.automation.common.presentation.AutomationMode
import com.anadolstudio.homehub.feature.home.data.model.automation.AutomationActionResponse
import com.anadolstudio.homehub.feature.home.data.model.automation.AutomationTriggerResponse
import com.anadolstudio.homehub.feature.home.domain.HAWebsocketRepository
import com.anadolstudio.homehub.feature.home.domain.model.HomeAssistantDevice
import com.anadolstudio.homehub.feature.home.domain.model.states.HomeAssistantState
import com.anadolstudio.homehub.feature.home.domain.model.states.NumberAttribute
import com.anadolstudio.homehub.feature.main.MainGraph.navigateToAutomationModePicker
import com.anadolstudio.homehub.feature.main.MainGraph.navigateToConditionPicker
import com.anadolstudio.homehub.feature.main.MainGraph.navigateToDevicePicker
import com.anadolstudio.homehub.feature.main.MainGraph.navigateToEntityPicker
import com.anadolstudio.homehub.feature.main.MainGraph.navigateToServiceDeviceDetail
import com.anadolstudio.homehub.feature.main.MainGraph.navigateToServiceDevicePicker
import com.anadolstudio.homehub.feature.sceneCreate.presentation.DeviceDraftCard
import com.anadolstudio.homehub.feature.sceneCreate.presentation.toDeviceDraftCard
import com.anadolstudio.homehub.util.mapIfContains
import com.anadolstudio.utils.states.LoadingContext
import com.anadolstudio.utils.states.lce.lceFlow
import com.anadolstudio.utils.states.lce.onEachContent
import com.anadolstudio.utils.states.lce.onEachError
import com.anadolstudio.utils.states.lce.onEachProgressState
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.launchIn

internal class AutomationDetailViewModel @AssistedInject constructor(
        @Assisted private val automationId: String?,
        private val repository: HAWebsocketRepository,
) : StatefulViewModel<AutomationDetailScreenState>(AutomationDetailScreenState()),
    AutomationDetailController {

    init {
        if (automationId != null) load(LoadingContext.INIT_LOADING)
    }

    private fun load(loadingContext: LoadingContext) {
        lceFlow {
            val config = repository.getAutomationConfig(requireNotNull(automationId))
            val devices = repository.getDeviceList(useCache = true)
            config to devices
        }
                .onEachProgressState(
                        previousState = state.progressState,
                        loadingContext = loadingContext,
                        onNewProgressState = { newProgress -> updateState { copy(progressState = newProgress) } },
                )
                .onEachContent { (config, devices) ->
                    val triggerDevices = config.triggers
                            .mapNotNull { trigger -> devices.firstOrNull { d -> d.allEntityList.any { it.entityId in trigger.entityId } } }
                            .distinctBy { it.id }
                    updateState {
                        copy(
                                name = config.alias.orEmpty(),
                                mode = config.mode.toAutomationMode(),
                                triggers = config.triggers.toTriggerUiList(devices),
                                triggerSnapshots = triggerDevices,
                                services = config.actions.toServiceDraftList(devices),
                        )
                    }
                }
                .onEachError { showError(it) }
                .launchIn(viewModelScope)
    }

    override fun onNameChanged(name: String) = updateState { copy(name = name) }

    override fun onModeClicked() = navigateToAutomationModePicker(state.mode)

    override fun onModeChanged(mode: AutomationMode) = updateState { copy(mode = mode) }

    override fun onCloseClicked() = Unit

    override fun onSaveClicked() = Unit

    override fun onAddTriggerClicked() =
            navigateToDevicePicker(state.triggers.mapNotNull { it.deviceId }.toSet())

    override fun onTriggerDeviceAdded(device: HomeAssistantDevice) {
        // The trigger card is created only after entities are chosen — here we just remember the
        // device so the follow-up entity selection can be applied to it.
        pendingEntitiesTarget = EntitiesTarget.TRIGGER
        pendingTriggerDevice = device
        updateState {
            copy(triggerSnapshots = if (triggerSnapshots.any { it.id == device.id }) triggerSnapshots else triggerSnapshots + device)
        }
    }

    override fun onEntitiesConfigured(entityIds: Set<String>) = when (pendingEntitiesTarget) {
        EntitiesTarget.TRIGGER -> applyTriggerEntities(entityIds)
        EntitiesTarget.CONDITION -> applyConditionEntities(entityIds)
    }

    private fun applyTriggerEntities(entityIds: Set<String>) = updateState {
        val device = pendingTriggerDevice ?: return@updateState this

        // No entities selected → this object is not a trigger (drop it if it existed).
        if (entityIds.isEmpty()) return@updateState copy(triggers = triggers.filterNot { it.deviceId == device.id })

        val names = device.allEntityList.filter { it.entityId in entityIds }.joinToString { it.name }
        val newTriggers = if (triggers.any { it.deviceId == device.id }) {
            triggers.map { trigger ->
                if (trigger.deviceId != device.id) {
                    trigger
                } else {
                    trigger.copy(entityIds = entityIds.toList(), subtitle = names.ifBlank { trigger.subtitle })
                }
            }
        } else {
            triggers + device.toTriggerUi().copy(
                    entityIds = entityIds.toList(),
                    subtitle = names.ifBlank { device.area?.name },
            )
        }
        copy(triggers = newTriggers)
    }

    private fun applyConditionEntities(entityIds: Set<String>) = updateState {
        val device = pendingConditionDevice ?: return@updateState this
        val leafId = pendingConditionLeafId

        // No entities selected → not a condition (drop it if editing an existing one).
        if (entityIds.isEmpty()) {
            return@updateState if (leafId != null) copy(conditionTree = conditionTree.deleteNode(leafId)) else this
        }

        val existingLeaf = leafId?.let { conditionTree.findLeaf(it) }
        val entities = device.allEntityList
                .filter { it.entityId in entityIds }
                .map { entity ->
                    ConditionEntity(
                            state = entity.state,
                            name = entity.name,
                            value = existingLeaf?.entities?.firstOrNull { it.entityId == entity.entityId }?.value
                                    ?: defaultConditionValue(entity.state),
                    )
                }

        if (leafId != null) {
            copy(conditionTree = conditionTree.updateLeafEntities(leafId, entities))
        } else {
            val leaf = LogicNode.Leaf(
                    id = "new_${System.nanoTime()}",
                    deviceId = device.id,
                    deviceName = device.name,
                    image = device.image,
                    entities = entities,
            )
            copy(conditionTree = conditionTree.addCondition(pendingConditionBlockId, leaf))
        }
    }

    override fun onAddServiceClicked() =
            navigateToServiceDevicePicker(state.services.map { it.id }.toSet())

    override fun onServiceDeviceAdded(device: HomeAssistantDevice) = updateState {
        if (services.any { it.id == device.id }) this else copy(services = services + device.toDeviceDraftCard(emptyList()))
    }

    override fun onServiceConfigured(selectedEntityIds: Set<String>) {
        if (selectedEntityIds.isEmpty()) return
        val deviceDraft = state.services.firstOrNull { draft ->
            draft.allEntityIdToNameMap.keys.any { it in selectedEntityIds }
        } ?: return

        lceFlow {
            val allEntityMap = repository.getEntityList(useCache = true).associateBy { it.entityId }
            selectedEntityIds.mapNotNull { allEntityMap[it]?.state }.toSet()
        }
                .onEachContent { entityStates ->
                    val entityIdToStates = entityStates.associateBy { it.entityId }.toMutableMap()
                    val newStates = deviceDraft.changeEntityStates
                            .mapIfContains(
                                    condition = { entityIdToStates[it.entityId] != null },
                                    provideNewElement = { entityIdToStates.remove(it.entityId)!! },
                            )
                            .toMutableList()
                            .apply { addAll(entityIdToStates.values) }
                    val newDraft = deviceDraft.copy(changeEntityStates = newStates)
                    updateState { copy(services = services.map { if (it.id == newDraft.id) newDraft else it }) }
                }
                .onEachError { showError(it) }
                .launchIn(viewModelScope)
    }

    override fun onTriggerEditClicked(trigger: TriggerUi) {
        val device = state.triggerSnapshots.firstOrNull { it.id == trigger.deviceId } ?: return
        pendingEntitiesTarget = EntitiesTarget.TRIGGER
        pendingTriggerDevice = device
        navigateToEntityPicker(device = device, selectedEntitySet = trigger.entityIds.toSet())
    }

    override fun onServiceEditClicked(service: DeviceDraftCard) {
        lceFlow { repository.getDevice(deviceId = service.id, useCache = true) }
                .onEachContent { device ->
                    if (device != null) {
                        navigateToServiceDeviceDetail(device, service.changeEntityStates.map { it.entityId }.toSet())
                    }
                }
                .onEachError { showError(it) }
                .launchIn(viewModelScope)
    }

    override fun onServiceEntityRemoved(deviceId: String, state: HomeAssistantState<*>) = updateState {
        val newServices = services.mapNotNull { card ->
            if (card.id != deviceId) {
                card
            } else {
                val newStates = card.changeEntityStates.filterNot { it.entityId == state.entityId }
                if (newStates.isEmpty()) null else card.copy(changeEntityStates = newStates)
            }
        }
        copy(services = newServices)
    }

    override fun onTriggerDeleteClicked(trigger: TriggerUi) = updateState {
        copy(triggers = triggers.filterNot { it.id == trigger.id })
    }

    override fun onServiceDeleteClicked(service: DeviceDraftCard) = updateState {
        copy(services = services.filterNot { it.id == service.id })
    }

    override fun onAddConditionClicked(blockId: String?) {
        val parentDepth = parentDepthOf(blockId)
        // At minimum a leaf must fit to add anything at all.
        if (parentDepth + 1 > MAX_LOGIC_DEPTH) {
            showMessage(R.string.logic_block_error_too_deep)
            return
        }
        pendingConditionBlockId = blockId
        // A block reserves one extra level for its content; if it can't fit, block options are disabled.
        navigateToConditionPicker(blocksEnabled = parentDepth + 2 <= MAX_LOGIC_DEPTH)
    }

    override fun onConditionBlockChosen(operator: LogicOperator) = updateState {
        copy(conditionTree = conditionTree.addCondition(pendingConditionBlockId, newBlock(operator)))
    }

    override fun onConditionDeviceChosen(device: HomeAssistantDevice) {
        // Mirror triggers: a device is chosen, then entities; the leaf is built in applyConditionEntities.
        pendingEntitiesTarget = EntitiesTarget.CONDITION
        pendingConditionDevice = device
        pendingConditionLeafId = null
        updateState {
            copy(conditionSnapshots = if (conditionSnapshots.any { it.id == device.id }) conditionSnapshots else conditionSnapshots + device)
        }
    }

    override fun onConditionLeafClicked(leafId: String) {
        val leaf = state.conditionTree.findLeaf(leafId) ?: return
        val device = state.conditionSnapshots.firstOrNull { it.id == leaf.deviceId } ?: return
        pendingEntitiesTarget = EntitiesTarget.CONDITION
        pendingConditionDevice = device
        pendingConditionLeafId = leafId
        navigateToEntityPicker(device = device, selectedEntitySet = leaf.entities.map { it.entityId }.toSet())
    }

    override fun onConditionEntityValueChanged(leafId: String, entityId: String, value: ConditionValue) = updateState {
        copy(conditionTree = conditionTree.updateEntityValue(leafId, entityId, value))
    }

    private fun parentDepthOf(blockId: String?): Int = blockId?.let { state.conditionTree.depthOf(it) } ?: -1

    private fun defaultConditionValue(state: HomeAssistantState<*>): ConditionValue =
            if (state.attributes is NumberAttribute) ConditionValue.Numeric() else ConditionValue.StateValue(state.allowedState.value)

    override fun onConditionMoved(from: Int, to: Int, draggingId: String?) {
        when (val result = state.conditionTree.moveByFlatIndex(from, to, draggingId)) {
            is MoveResult.Moved -> updateState { copy(conditionTree = result.tree) }
            MoveResult.IntoOwnSubtree -> rejectMove(R.string.logic_block_error_into_itself)
            MoveResult.TooDeep -> rejectMove(R.string.logic_block_error_too_deep)
        }
    }

    private fun rejectMove(messageRes: Int) {
        showMessage(messageRes)
        updateState { copy(conditionResetTick = conditionResetTick + 1) }
    }

    override fun onConditionDeleted(id: String) = updateState {
        copy(conditionTree = conditionTree.deleteNode(id))
    }

    override fun onConditionCollapseToggled(id: String) = updateState {
        copy(conditionTree = conditionTree.toggleCollapsed(id))
    }

    private enum class EntitiesTarget { TRIGGER, CONDITION }

    private var pendingEntitiesTarget: EntitiesTarget = EntitiesTarget.TRIGGER

    private var pendingConditionBlockId: String? = null

    private var pendingConditionDevice: HomeAssistantDevice? = null

    private var pendingConditionLeafId: String? = null

    private var pendingTriggerDevice: HomeAssistantDevice? = null

    private fun newBlock(operator: LogicOperator) =
            LogicNode.Block(id = "new_${System.nanoTime()}", operator = operator)

    @AssistedFactory
    interface Factory {
        fun create(automationId: String?): AutomationDetailViewModel
    }
}

private fun List<AutomationTriggerResponse>.toTriggerUiList(
        devices: List<HomeAssistantDevice>,
): List<TriggerUi> = mapIndexed { index, trigger ->
    val entityId = trigger.entityId.firstOrNull()
    val device = devices.firstOrNull { device -> device.allEntityList.any { it.entityId == entityId } }
    TriggerUi(
            id = "trigger_$index",
            title = device?.name ?: entityId ?: trigger.trigger?.replaceFirstChar { it.uppercase() }.orEmpty().ifBlank { "Trigger" },
            subtitle = if (device != null) entityId else trigger.trigger,
            deviceId = device?.id,
            image = device?.image,
            entityIds = trigger.entityId,
    )
}

private fun HomeAssistantDevice.toTriggerUi(): TriggerUi = TriggerUi(
        id = "trigger_$id",
        title = name,
        subtitle = area?.name,
        deviceId = id,
        image = image,
)

private fun List<AutomationActionResponse>.toServiceDraftList(
        devices: List<HomeAssistantDevice>,
): List<DeviceDraftCard> {
    val actionEntityIds = flatMap { it.target?.entityId.orEmpty() }.toSet()
    return devices.mapNotNull { device ->
        val entities = device.allEntityList.filter { it.entityId in actionEntityIds }
        if (entities.isEmpty()) null else device.toDeviceDraftCard(entities.map { it.state })
    }
}

private fun String?.toAutomationMode(): AutomationMode = when (this) {
    "restart" -> AutomationMode.RESTART
    "queued" -> AutomationMode.QUEUED
    "parallel" -> AutomationMode.PARALLEL
    else -> AutomationMode.SINGLE
}
