package com.anadolstudio.homehub.feature.automation.automationList.presentation

import androidx.lifecycle.viewModelScope
import com.anadolstudio.homehub.R
import com.anadolstudio.homehub.base.viewmodel.RestartableJob
import com.anadolstudio.homehub.base.viewmodel.StatefulViewModel
import com.anadolstudio.homehub.event.showError
import com.anadolstudio.homehub.event.showMessage
import com.anadolstudio.homehub.feature.home.domain.HARestRepository
import com.anadolstudio.homehub.feature.home.domain.HAWebsocketRepository
import com.anadolstudio.homehub.feature.home.domain.model.AllowedDomain.AUTOMATION
import com.anadolstudio.homehub.feature.home.domain.model.AllowedDomain.SCENE
import com.anadolstudio.homehub.feature.home.domain.model.entity.HomeAssistantEntity
import com.anadolstudio.homehub.feature.home.domain.model.entity.mapAttributes
import com.anadolstudio.homehub.feature.home.domain.model.events.HomeAssistantStateChangedEvent
import com.anadolstudio.homehub.feature.home.domain.model.services.SceneService
import com.anadolstudio.homehub.feature.home.domain.model.services.SimpleToggleableService
import com.anadolstudio.homehub.feature.home.domain.model.states.AutomationAttributes
import com.anadolstudio.homehub.feature.home.domain.model.states.HomeAssistantAttribute
import com.anadolstudio.homehub.feature.home.domain.model.states.HomeAssistantState
import com.anadolstudio.homehub.feature.home.domain.model.states.SceneAttributes
import com.anadolstudio.homehub.feature.home.domain.model.states.toAutomation
import com.anadolstudio.homehub.feature.home.domain.model.states.toScene
import com.anadolstudio.homehub.feature.main.MainGraph.navigateToAutomationDetail
import com.anadolstudio.homehub.feature.main.MainGraph.navigateToSceneCreate
import com.anadolstudio.homehub.feature.main.MainGraph.navigateToSceneEdit
import com.anadolstudio.homehub.util.mapIfContains
import com.anadolstudio.utils.states.LoadingContext
import com.anadolstudio.utils.states.lce.lceFlow
import com.anadolstudio.utils.states.lce.mapToLce
import com.anadolstudio.utils.states.lce.onEachContent
import com.anadolstudio.utils.states.lce.onEachError
import com.anadolstudio.utils.states.lce.onEachProgressState
import javax.inject.Inject
import kotlinx.coroutines.flow.launchIn
import kotlinx.serialization.json.Json

internal class AutomationListViewModel @Inject constructor(
        private val websocketRepository: HAWebsocketRepository,
        private val restRepository: HARestRepository,
        private val json: Json,
) : StatefulViewModel<AutomationListScreenState>(AutomationListScreenState()),
    AutomationListController {

    private var stateChangedJob by RestartableJob()

    init {
        loadAutomationStates(LoadingContext.INIT_LOADING)
    }

    private fun loadAutomationStates(loadingContext: LoadingContext) {
        lceFlow {
            val sceneList = mutableListOf<HomeAssistantEntity<SceneAttributes>>()
            val automationList = mutableListOf<HomeAssistantEntity<AutomationAttributes>>()
            websocketRepository.getEntityList().forEach { entity ->
                if (entity.allowedDomain != SCENE && entity.allowedDomain != AUTOMATION) return@forEach

                when (entity.allowedDomain) {
                    AUTOMATION -> automationList.add(entity.mapAttributes { it.toAutomation(json) })
                    SCENE -> sceneList.add(entity.mapAttributes { it.toScene(json) })
                    else -> Unit
                }
            }
            sceneList.sortedBy { it.name } to
                    automationList.sortedBy { it.name }
        }
                .onEachProgressState(
                        previousState = state.progressState,
                        loadingContext = loadingContext,
                        onNewProgressState = { updateState { copy(progressState = it) } }
                )
                .onEachContent { (sceneList, automationList) ->
                    updateState { copy(sceneList = sceneList, automationList = automationList) }

                    subscribeToStateChangedEvents()
                }
                .onEachError { showError(it) }
                .launchIn(viewModelScope)
    }

    private fun subscribeToStateChangedEvents() {
        stateChangedJob = websocketRepository.subscribeToStateChangedEvents()
                .mapToLce()
                .onEachContent { event ->
                    when (event) {
                        is HomeAssistantStateChangedEvent.Remove -> removeEntity(event.entityId)
                        is HomeAssistantStateChangedEvent.Update -> updateEntity(event)
                    }
                }
                .launchIn(viewModelScope)
    }

    private fun removeEntity(entityId: String) {
        val automationList = state.automationList.filterNot { entity -> entity.entityId == entityId }
        val sceneList = state.sceneList.filterNot { entity -> entity.entityId == entityId }

        updateState { copy(automationList = automationList, sceneList = sceneList) }
    }

    private fun updateEntity(stateChangedEvent: HomeAssistantStateChangedEvent.Update) {
        val newState = stateChangedEvent.newState
        val entityId = stateChangedEvent.entityId

        when (newState.attributes) {
            is AutomationAttributes -> updateState {
                val newList = automationList.mapIfContains(
                        condition = { it.entityId == entityId },
                        provideNewElement = { entity ->
                            entity.copy(state = newState as HomeAssistantState<AutomationAttributes>)
                        }
                )
                copy(automationList = newList)
            }

            is SceneAttributes -> updateState {
                val newList = sceneList.mapIfContains(
                        condition = { it.entityId == entityId },
                        provideNewElement = { entity ->
                            entity.copy(state = newState as HomeAssistantState<SceneAttributes>)
                        }
                )
                copy(sceneList = newList)
            }

            else -> return
        }
    }

    override fun onAutomationItemClicked(automation: HomeAssistantEntity<AutomationAttributes>) {
        navigateToAutomationDetail(automation.entityId)
    }

    override fun onSceneListRefreshRequested() {
        loadAutomationStates(LoadingContext.RETRY)
    }

    override fun onSceneStart(scene: HomeAssistantEntity<SceneAttributes>) {
        lceFlow {
            websocketRepository.callService(
                    entityId = scene.entityId,
                    domain = SCENE.prefix,
                    service = SceneService.On,
            )
        }
                .onEachContent { isSuccess -> if (!isSuccess) showError(R.string.common_error) }
                .launchIn(viewModelScope)
    }

    override fun onSceneItemClicked(scene: HomeAssistantEntity<SceneAttributes>) {
        // Постоянная сцена (созданная через /api/config/scene/config) хранит свой config-id
        // в attributes.id. Если его нет (runtime-сцена от scene.create), берём из entity_id
        // как `scene.<config_id>` и пробуем. Если и тут пусто — сцена не редактируется.
        val sceneConfigId = scene.state.attributes.id
                ?: scene.entityId.substringAfter("scene.", missingDelimiterValue = "")
        if (sceneConfigId.isBlank()) {
            showError("Эта сцена не может быть отредактирована")
            return
        }
        navigateToSceneEdit(sceneConfigId)
    }

    override fun onTabSelected(tab: AutomationTab) = updateState { copy(currentTab = tab) }

    override fun onCreateClicked() {
        when (state.currentTab) {
            AutomationTab.AUTOMATIONS -> navigateToAutomationDetail()
            AutomationTab.SCENES -> navigateToSceneCreate()
        }
    }

    override fun onAutomationItemDeleteClicked(automation: HomeAssistantEntity<AutomationAttributes>) {
        // TODO: подключить реальное удаление автоматизации через HA API
        showMessage("Удаление автоматизации ${automation.name} пока не реализовано")
    }

    override fun onSceneItemDeleteClicked(scene: HomeAssistantEntity<SceneAttributes>) {
        val sceneId = scene.state.attributes.id ?: return

        lceFlow { restRepository.deleteSceneConfig(sceneId) }
                .onEachContent { isSuccess ->
                    if (!isSuccess) showError(R.string.common_error)
                }
                .onEachError(::showError)
                .launchIn(viewModelScope)
    }

    override fun onAutomationItemEnableChanged(entity: HomeAssistantEntity<HomeAssistantAttribute>) {
        lceFlow {
            websocketRepository.callService(
                    entityId = entity.entityId,
                    domain = entity.domain,
                    service = SimpleToggleableService.Toggle,
            )
        }
                .onEachContent { isSuccess ->
                    if (!isSuccess) showMessage("не удалось выполнить ${entity.entityId}/${entity.domain}")
                }
                .launchIn(viewModelScope)
    }
}
