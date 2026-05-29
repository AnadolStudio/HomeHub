package com.anadolstudio.homehub.feature.sceneCreate.presentation

import androidx.lifecycle.viewModelScope
import com.anadolstudio.homehub.R
import com.anadolstudio.homehub.base.viewmodel.StatefulViewModel
import com.anadolstudio.homehub.event.navigateUp
import com.anadolstudio.homehub.event.showError
import com.anadolstudio.homehub.feature.home.domain.HARestRepository
import com.anadolstudio.homehub.feature.home.domain.HAWebsocketRepository
import com.anadolstudio.homehub.feature.home.domain.model.HomeAssistantDevice
import com.anadolstudio.homehub.feature.home.domain.model.scene.SceneConfig
import com.anadolstudio.homehub.feature.home.domain.model.states.HomeAssistantState
import com.anadolstudio.homehub.feature.main.MainGraph.navigateToDemoDeviceDetailFromSceneCreate
import com.anadolstudio.homehub.feature.main.MainGraph.navigateToSceneDevicePicker
import com.anadolstudio.homehub.feature.sceneCreate.util.slugify
import com.anadolstudio.homehub.util.mapIfContains
import com.anadolstudio.utils.states.LoadingContext
import com.anadolstudio.utils.states.lce.lceFlow
import com.anadolstudio.utils.states.lce.onEachContent
import com.anadolstudio.utils.states.lce.onEachError
import com.anadolstudio.utils.states.lce.onEachProgressState
import com.anadolstudio.utils.util.extentions.onFalse
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.launchIn

internal class SceneCreateViewModel @AssistedInject constructor(
        @Assisted editSceneConfigId: String?,
        private val restRepository: HARestRepository,
        private val websocketRepository: HAWebsocketRepository,
) : StatefulViewModel<SceneCreateScreenState>(
        SceneCreateScreenState(
                mode = if (editSceneConfigId == null) SceneCreateMode.CREATE else SceneCreateMode.EDIT,
                sceneConfigId = editSceneConfigId.orEmpty()
        )
),
    SceneCreateController {

    private val detachedScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        loadDeviceIfExist(editSceneConfigId)
    }

    private fun loadDeviceIfExist(editSceneConfigId: String?) {
        if (editSceneConfigId == null) return

        lceFlow {
            val sceneConfig = restRepository.getSceneConfig(editSceneConfigId)
            val entityIdSet = sceneConfig.entityStates.map { it.entityId }.toSet()
            val devices = websocketRepository.getDeviceList().filter { device ->
                device.allEntityList.any { entityIdSet.contains(it.entityId) }
            }

            LoadedScene(sceneConfig = sceneConfig, devices = devices)
        }
                .onEachProgressState(
                        previousState = state.progressState,
                        loadingContext = LoadingContext.INIT_LOADING,
                        onNewProgressState = { newProgress -> updateState { copy(progressState = newProgress) } },
                )
                .onEachContent { loadedScene ->
                    val sceneConfig = loadedScene.sceneConfig

                    val selectedDeviceDraftSet = loadedScene.devices
                            .map { device ->
                                val allEntityList = device.allEntityList.map { it.entityId }.toSet()
                                val entityStates = sceneConfig.entityStates.filter {
                                    allEntityList.contains(it.entityId)
                                }
                                device.toDeviceDraftCard(entityStates)
                            }
                            .toSet()

                    updateState {
                        copy(
                                name = sceneConfig.name,
                                icon = sceneConfig.icon,
                                selectedDeviceDraftSet = selectedDeviceDraftSet,
                                snapshotDevices = loadedScene.devices.toSet()
                        )
                    }
                }
                .launchIn(viewModelScope)
    }

    override fun onNameChanged(value: String) {
        val sceneConfigId = when (state.mode) {
            SceneCreateMode.CREATE -> slugify(value)
            SceneCreateMode.EDIT -> state.sceneConfigId
        }
        updateState { copy(name = value, sceneConfigId = sceneConfigId) }
    }

    override fun onAddClicked() = navigateToSceneDevicePicker(state.selectedDeviceDraftSet.map { it.id }.toSet())

    override fun onDeviceEditClicked(deviceDraft: DeviceDraftCard) {
        lceFlow { websocketRepository.getDevice(deviceId = deviceDraft.id, useCache = true) }
                .onEachContent { device ->
                    if (device != null) {
                        navigateToDemoDeviceDetailFromSceneCreate(device, emptySet())
                    }
                }
                .launchIn(viewModelScope)
    }

    override fun onDeviceRemoved(deviceDraft: DeviceDraftCard) = updateState {
        val deviceId = deviceDraft.id
        val newSelectedDevices = selectedDeviceDraftSet.toMutableSet().apply { removeIf { it.id == deviceId } }
        val newSnapshotDevices = snapshotDevices.toMutableSet().apply { removeIf { it.id == deviceId } }

        copy(selectedDeviceDraftSet = newSelectedDevices, snapshotDevices = newSnapshotDevices)
    }.also { onNewChanges() }

    override fun onEntityRemoved(deviceId: String, state: HomeAssistantState<*>) = updateState {
        val uselessDeviceIdSet = mutableSetOf<String>()
        val newSelectedDevices = selectedDeviceDraftSet
                .asSequence()
                .mapIfContains(
                        condition = { draft -> draft.id == deviceId },
                        provideNewElement = { draft ->
                            draft.copy(changeEntityStates = draft.changeEntityStates.filterNot { it.entityId == state.entityId })
                        }
                )
                .filter {
                    it.changeEntityStates.isNotEmpty().onFalse { uselessDeviceIdSet.add(it.id) }
                }

        copy(selectedDeviceDraftSet = newSelectedDevices.toSet())
    }.also { onNewChanges() }

    override fun onSnapshotAdded(device: HomeAssistantDevice) = updateState {
        val newDeviceDraft = device.toDeviceDraftCard(emptyList())
        copy(
                snapshotDevices = snapshotDevices
                        .mapIfContains(
                                condition = { it.id == device.id },
                                provideNewElement = { device }
                        )
                        .toSet()
                        .plus(device),
                selectedDeviceDraftSet = selectedDeviceDraftSet
                        .mapIfContains(
                                condition = { it.id == device.id },
                                provideNewElement = { newDeviceDraft }
                        )
                        .toSet()
                        .plus(newDeviceDraft)
        )
    }

    override fun onDeviceConfigured(selectedEntities: Set<String>) {
        if (selectedEntities.isEmpty()) {
            val uselessDevice = state.selectedDeviceDraftSet.filter { it.changeEntityStates.isEmpty() }
            updateState {
                copy(
                        selectedDeviceDraftSet = selectedDeviceDraftSet
                                .filterNot { card -> uselessDevice.any { it.id == card.id } }
                                .toSet(),
                        snapshotDevices = snapshotDevices
                                .filterNot { card -> uselessDevice.any { it.id == card.id } }
                                .toSet()
                )
            }
            return
        }

        val deviceDraft = state.selectedDeviceDraftSet
                .firstOrNull { deviceDraft ->
                    deviceDraft.allEntityIdToNameMap.keys.any { entityId -> selectedEntities.contains(entityId) }
                }
                ?: return showError(R.string.common_error)

        lceFlow {
            val allEntityMap = websocketRepository.getEntityList(useCache = true).associateBy { it.entityId }
            val actualEntityStates = mutableSetOf<HomeAssistantState<*>>()

            selectedEntities.forEach { id ->
                allEntityMap[id]?.let { actualEntity -> actualEntityStates.add(actualEntity.state) }
            }

            return@lceFlow actualEntityStates
        }
                .onEachContent { entityStates ->
                    val entityIdToStates = entityStates.associateBy { it.entityId }.toMutableMap()
                    val newStates = deviceDraft.changeEntityStates
                            .mapIfContains(
                                    condition = { state -> entityIdToStates[state.entityId] != null },
                                    provideNewElement = { state -> entityIdToStates.remove(state.entityId)!! }
                            )
                            .toMutableList()
                            .apply { addAll(entityIdToStates.values) }

                    val newDeviceDraft = deviceDraft.copy(changeEntityStates = newStates)
                    val newSelectedDevices = state.selectedDeviceDraftSet.mapIfContains(
                            condition = { draft -> draft.id == newDeviceDraft.id },
                            provideNewElement = { newDeviceDraft }
                    )
                    updateState { copy(selectedDeviceDraftSet = newSelectedDevices.toSet()) }
                    onNewChanges()
                }
                .onEachError { showError(it) }
                .launchIn(viewModelScope)
    }

    override fun onSaveClicked() {
        val validation = state.validate()

        if (validation != null) {
            showError(validation)
            return
        }

        lceFlow {
            val entityStates = state.selectedDeviceDraftSet.flatMap { it.changeEntityStates }
            restRepository.saveSceneConfig(state.name, state.sceneConfigId, entityStates)
        }
                .onEachContent { updateState { copy(hasChanged = true, isSaved = true) } }
                .onEachError { showError(it) }
                .launchIn(viewModelScope)
    }

    private fun reloadAllStates() {
        val usedEntities = state.selectedDeviceDraftSet
                .flatMap { it.changeEntityStates.map { state -> state.entityId } }
                .toSet()
        val entityStates = state.snapshotDevices
                .asSequence()
                .flatMap { it.allEntityList }
                .map { entity -> entity.state }
                .filter { state -> usedEntities.contains(state.entityId) }
                .toList()

        lceFlow { restRepository.applyScene(entityStates) }
                .launchIn(detachedScope)
    }

    private fun onNewChanges() = updateState { copy(hasChanged = true, isSaved = false) }

    override fun onCloseClicked() = navigateUp(SCENE_LIST_NEEDS_REFRESH_KEY to (state.hasChanged && state.isSaved))

    private data class LoadedScene(
            val sceneConfig: SceneConfig,
            val devices: List<HomeAssistantDevice>,
    )

    override fun onCleared() {
        if (state.isSaved) reloadAllStates()

        super.onCleared()
    }

    @AssistedFactory
    interface Factory {
        fun create(editSceneConfigId: String?): SceneCreateViewModel
    }
}
