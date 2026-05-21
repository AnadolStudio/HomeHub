package com.anadolstudio.template.feature.sceneCreate.presentation

import androidx.lifecycle.viewModelScope
import com.anadolstudio.template.R
import com.anadolstudio.template.base.viewmodel.StatefulViewModel
import com.anadolstudio.template.event.navigateUp
import com.anadolstudio.template.event.showError
import com.anadolstudio.template.event.showMessage
import com.anadolstudio.template.feature.common.domain.ResourceRepository
import com.anadolstudio.template.feature.home.domain.HARestRepository
import com.anadolstudio.template.feature.home.domain.HAWebsocketRepository
import com.anadolstudio.template.feature.home.domain.model.AllowedDomain
import com.anadolstudio.template.feature.home.domain.model.HomeAssistantDevice
import com.anadolstudio.template.feature.home.domain.model.entity.HomeAssistantEntity
import com.anadolstudio.template.feature.home.domain.model.entity.mapAttributes
import com.anadolstudio.template.feature.home.domain.model.services.SimpleToggleableService
import com.anadolstudio.template.feature.home.domain.model.states.HomeAssistantAttribute
import com.anadolstudio.template.feature.home.domain.model.states.toScene
import com.anadolstudio.template.feature.main.MainGraph.navigateToDeviceDetailFromSceneCreate
import com.anadolstudio.template.feature.main.MainGraph.navigateToSceneDevicePicker
import com.anadolstudio.template.feature.sceneCreate.data.mapper.parseSceneConfig
import com.anadolstudio.template.feature.sceneCreate.domain.isSupportedInScene
import com.anadolstudio.template.feature.sceneCreate.domain.toSceneEntityState
import com.anadolstudio.template.feature.sceneCreate.util.slugify
import com.anadolstudio.utils.states.LoadingContext
import com.anadolstudio.utils.states.lce.lceFlow
import com.anadolstudio.utils.states.lce.onEachContent
import com.anadolstudio.utils.states.lce.onEachError
import com.anadolstudio.utils.states.lce.onEachProgressState
import javax.inject.Inject
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

internal class SceneCreateViewModel @Inject constructor(
        private val restRepository: HARestRepository,
        private val websocketRepository: HAWebsocketRepository,
        private val json: Json,
        private val resources: ResourceRepository,
) : StatefulViewModel<SceneCreateScreenState>(SceneCreateScreenState()),
    SceneCreateController {

    /**
     * Снимок entity-состояний устройства ДО открытия DeviceDetail.
     * Ключ — deviceId, значение — entityId → entity. Используется в [onDeviceConfigured]
     * для выбора только различающихся сущностей (см. ТЗ + пользовательское требование).
     */
    private val pendingSnapshots: MutableMap<String, Map<String, HomeAssistantEntity<HomeAssistantAttribute>>> =
            mutableMapOf()

    /** Защита от повторной загрузки если экран пересоставится (rotation, recomposition). */
    private var editModeLoadStarted: Boolean = false

    override fun onEditModeRequested(sceneConfigId: String) {
        if (editModeLoadStarted) return
        editModeLoadStarted = true

        updateState {
            copy(
                    sceneConfigId = sceneConfigId,
                    isIdManuallyEdited = true,
                    isEditMode = true,
            )
        }

        lceFlow { loadExistingScene(sceneConfigId) }
                .onEachProgressState(
                        previousState = state.progressState,
                        loadingContext = LoadingContext.INIT_LOADING,
                        onNewProgressState = { newProgress -> updateState { copy(progressState = newProgress) } },
                )
                .onEachContent { loaded ->
                    if (loaded != null) {
                        updateState {
                            copy(
                                    name = loaded.name,
                                    icon = loaded.icon,
                                    devices = loaded.devices,
                            )
                        }
                    }
                }
                .onEachError { showError(it) }
                .launchIn(viewModelScope)
    }

    private suspend fun loadExistingScene(sceneConfigId: String): LoadedScene? {
        val configJson = restRepository.getSceneConfig(sceneConfigId)
        val parsed = configJson.parseSceneConfig()

        if (parsed.entities.isEmpty()) {
            return LoadedScene(name = parsed.name, icon = parsed.icon, devices = emptyMap())
        }

        // Чтобы построить карточки по устройствам, нужно для каждого entity знать deviceId +
        // display-инфу. Тащим весь список сущностей один раз и индексируем.
        val entityIndex: Map<String, HomeAssistantEntity<HomeAssistantAttribute>> =
                websocketRepository.getEntityList().associateBy { it.entityId }

        val itemsByDevice: MutableMap<String, MutableList<DeviceDraftEntityItem>> = mutableMapOf()
        val orphanItems: MutableList<DeviceDraftEntityItem> = mutableListOf()

        for ((entityId, state) in parsed.entities) {
            val entity = entityIndex[entityId]
            val item = DeviceDraftEntityItem(
                    entityId = entityId,
                    displayName = entity?.name?.takeIf { it.isNotBlank() } ?: entityId,
                    drawableRes = entity?.state?.icon?.drawableRes ?: 0,
                    state = state,
            )
            val deviceId = entity?.deviceId
            if (deviceId.isNullOrBlank()) {
                orphanItems += item
            } else {
                itemsByDevice.getOrPut(deviceId) { mutableListOf() } += item
            }
        }

        val devices: MutableMap<String, DeviceDraftCard> = mutableMapOf()
        for ((deviceId, items) in itemsByDevice) {
            val device = runCatching { websocketRepository.getDevice(deviceId, useCache = true) }.getOrNull()
            devices[deviceId] = DeviceDraftCard(
                    deviceId = deviceId,
                    name = device?.name?.takeIf { it.isNotBlank() } ?: deviceId,
                    manufacturer = device?.manufacturer,
                    model = device?.model?.takeIf { it.isNotBlank() },
                    areaName = device?.area?.name,
                    entities = items,
                    deviceImage = device?.image
            )
        }
        // Сущности без device (например, пользовательские helpers) кладём в синтетическую карточку,
        // чтобы они были видны в UI и не потерялись при пересохранении.
        if (orphanItems.isNotEmpty()) {
            devices[ORPHAN_DEVICE_KEY] = DeviceDraftCard(
                    deviceId = ORPHAN_DEVICE_KEY,
                    name = resources.getString(R.string.scene_create_orphan_device_name),
                    manufacturer = null,
                    model = null,
                    areaName = null,
                    entities = orphanItems,
                    deviceImage = null
            )
        }

        return LoadedScene(name = parsed.name, icon = parsed.icon, devices = devices)
    }

    override fun onNameChanged(value: String) {
        updateState {
            copy(name = value,   sceneConfigId = if (isIdManuallyEdited) sceneConfigId else slugify(value))
        }
    }

    override fun onSceneIdChanged(value: String) {
        updateState {
            copy(sceneConfigId = slugify(value), isIdManuallyEdited = true,)
        }
    }

    override fun onAddDeviceClicked() {
        navigateToSceneDevicePicker()
    }

    override fun onDeviceEditClicked(deviceId: String) {
        // Снапшот для Edit-flow берём прямо из репозитория, потому что SceneCreate сам обладает
        // VM-инстансом и нет нужды передавать через savedStateHandle.
        viewModelScope.launch {
            val device = runCatching { websocketRepository.getDevice(deviceId, useCache = false) }
                    .getOrNull() ?: return@launch
            pendingSnapshots[deviceId] = device.allEntityList.associateBy { it.entityId }
            navigateToDeviceDetailFromSceneCreate(device)
        }
    }

    override fun onDeviceRemoved(deviceId: String) {
        updateState { copy(devices = devices - deviceId) }
        pendingSnapshots.remove(deviceId)
    }

    override fun onPreviewToggled() {
        updateState { copy(isPreviewExpanded = !isPreviewExpanded) }
    }

    override fun onSnapshotCaptured(entities: List<HomeAssistantEntity<HomeAssistantAttribute>>) {
        val deviceId = entities.firstOrNull()?.deviceId ?: return
        pendingSnapshots[deviceId] = entities.associateBy { it.entityId }
    }

    override fun onDeviceConfigured(entities: List<HomeAssistantEntity<HomeAssistantAttribute>>) {
        val deviceId = entities.firstOrNull()?.deviceId ?: return

        // Ре-фетчим устройство свежим: state в DeviceDetail VM мог отстать от HA на момент onDispose.
        lceFlow { websocketRepository.getDevice(deviceId, useCache = false) }
                .onEachContent { device -> if (device != null) digestDevice(device) }
                .onEachError { showError(it) }
                .launchIn(viewModelScope)
    }

    private fun digestDevice(device: HomeAssistantDevice) {
        val freshEntities = device.allEntityList
        val snapshot = pendingSnapshots.remove(device.id)

        val items = freshEntities
                .filter { it.isSupportedInScene() }
                .mapNotNull { entity ->
                    val newState = entity.toSceneEntityState() ?: return@mapNotNull null
                    val oldState = snapshot?.get(entity.entityId)?.toSceneEntityState()
                    // Берём только если состояние различается (либо снапшота нет — fallback).
                    if (snapshot != null && oldState == newState) return@mapNotNull null
                    DeviceDraftEntityItem(
                            entityId = entity.entityId,
                            displayName = entity.name.ifBlank { entity.entityId },
                            drawableRes = entity.state.icon.drawableRes,
                            state = newState,
                    )
                }

        if (items.isEmpty()) {
            // Юзер открыл устройство, но не изменил ни одной поддерживаемой сущности.
            // Не пополняем draft и не дёргаем существующую карточку.
            return
        }

        val card = DeviceDraftCard(
                deviceId = device.id,
                name = device.name,
                manufacturer = device.manufacturer,
                model = device.model.takeIf { it.isNotBlank() },
                areaName = device.area?.name,
                entities = items,
                deviceImage = device.image
        )

        updateState { copy(devices = devices + (device.id to card)) }
    }

    override fun onSaveClicked() {
        val validation = state.validate()

        if (validation != null) {
            updateState { copy(validationError = validation) }
            showError(validation)
            return
        }

        val draft = state.draft
        lceFlow {
            val ok = restRepository.saveSceneConfig(draft)
            if (!ok) return@lceFlow false to null

            val sceneEntityId = findCreatedSceneEntityId(draft.sceneConfigId)
            true to sceneEntityId
        }
                .onEachProgressState(
                        previousState = state.progressState,
                        loadingContext = LoadingContext.INIT_LOADING,
                        onNewProgressState = { newProgress -> updateState { copy(progressState = newProgress) } },
                )
                .onEachContent { result ->
                    val (ok, sceneEntityId) = result ?: return@onEachContent
                    if (!ok) {
                        showError(R.string.scene_create_error_save_failed)
                        return@onEachContent
                    }
                    if (sceneEntityId == null) {
                        showError(R.string.scene_create_error_not_found)
                    }
                    updateState { copy(createdSceneEntityId = sceneEntityId, validationError = null) }
                }
                .onEachError { showError(it) }
                .launchIn(viewModelScope)
    }

    private suspend fun findCreatedSceneEntityId(sceneConfigId: String): String? {
        val sceneEntities = websocketRepository.getEntityList()
                .asSequence()
                .filter { it.allowedDomain == AllowedDomain.SCENE }
                .map { entity -> entity.mapAttributes { jsonObject -> jsonObject.toScene(json) } }
                .toList()
        return sceneEntities.firstOrNull { it.state.attributes.id == sceneConfigId }?.entityId
    }

    override fun onRunCreatedSceneClicked() {
        val sceneEntityId = state.createdSceneEntityId ?: return
        lceFlow {
            websocketRepository.callService(
                    entityId = sceneEntityId,
                    domain = AllowedDomain.SCENE.prefix,
                    service = SimpleToggleableService.On,
            )
        }
                .onEachContent { isSuccess ->
                    if (!isSuccess) showMessage("не удалось выполнить ${sceneEntityId}/${AllowedDomain.SCENE.prefix}")
                }
                .launchIn(viewModelScope)
    }

    override fun onCloseClicked() = navigateUp()

    private data class LoadedScene(
            val name: String,
            val icon: String?,
            val devices: Map<String, DeviceDraftCard>,
    )

    private companion object {
        const val ORPHAN_DEVICE_KEY = "__orphan__"
    }
}
