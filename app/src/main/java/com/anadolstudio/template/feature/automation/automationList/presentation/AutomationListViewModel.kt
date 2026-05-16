package com.anadolstudio.template.feature.automation.automationList.presentation

import androidx.lifecycle.viewModelScope
import com.anadolstudio.template.base.viewmodel.StatefulViewModel
import com.anadolstudio.template.event.showError
import com.anadolstudio.template.feature.home.domain.HAWebsocketRepository
import com.anadolstudio.template.feature.home.domain.model.AllowedDomain.AUTOMATION
import com.anadolstudio.template.feature.home.domain.model.AllowedDomain.SCENE
import com.anadolstudio.template.feature.home.domain.model.entity.HomeAssistantEntity
import com.anadolstudio.template.feature.home.domain.model.entity.mapAttributes
import com.anadolstudio.template.feature.home.domain.model.events.HomeAssistantStateChangedEvent
import com.anadolstudio.template.feature.home.domain.model.services.SwitchService
import com.anadolstudio.template.feature.home.domain.model.states.AutomationAttributes
import com.anadolstudio.template.feature.home.domain.model.states.HomeAssistantAttribute
import com.anadolstudio.template.feature.home.domain.model.states.HomeAssistantState
import com.anadolstudio.template.feature.home.domain.model.states.SceneAttributes
import com.anadolstudio.template.feature.home.domain.model.states.toAutomation
import com.anadolstudio.template.feature.home.domain.model.states.toScene
import com.anadolstudio.template.feature.main.MainGraph.navigateToAutomationDetail
import com.anadolstudio.template.feature.main.MainGraph.navigateToSceneDetail
import com.anadolstudio.template.util.mapIfContains
import com.anadolstudio.utils.states.LoadingContext
import com.anadolstudio.utils.states.lce.lceFlow
import com.anadolstudio.utils.states.lce.lceStateFlow
import com.anadolstudio.utils.states.lce.onEachContent
import com.anadolstudio.utils.states.lce.onEachError
import com.anadolstudio.utils.states.lce.onEachProgressState
import javax.inject.Inject
import kotlinx.coroutines.flow.launchIn
import kotlinx.serialization.json.Json

internal class AutomationListViewModel @Inject constructor(
        private val websocketRepository: HAWebsocketRepository,
        private val json: Json,
) : StatefulViewModel<AutomationListScreenState>(AutomationListScreenState()),
    AutomationListController {

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
            sceneList.sortedBy { it.state.attributes.friendlyName } to
                    automationList.sortedBy { it.state.attributes.friendlyName }
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
        lceFlow {
            websocketRepository.subscribeToStateChangedEvents().collect { stateChangedEvent ->
                updateEntity(stateChangedEvent)
            }
        }.launchIn(viewModelScope)
    }

    private fun updateEntity(stateChangedEvent: HomeAssistantStateChangedEvent) {
        val entityId = stateChangedEvent.entityId
        val newState = stateChangedEvent.newState

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

    override fun onAutomationItemClicked() {
        navigateToAutomationDetail()
    }

    override fun onSceneItemClicked() {
        navigateToSceneDetail()
    }

    override fun onTabSelected(tab: AutomationTab) = updateState { copy(currentTab = tab) }

    override fun onCreateClicked() {
        when (state.currentTab) {
            AutomationTab.AUTOMATIONS -> navigateToAutomationDetail()
            AutomationTab.SCENES -> navigateToSceneDetail()
        }
    }

    override fun onAutomationItemEnableChanged(entity: HomeAssistantEntity<HomeAssistantAttribute>) {
        lceStateFlow {
            websocketRepository.callService(
                    entityId = entity.entityId,
                    domain = entity.domain,
                    service = SwitchService.Toggle.toStringService(),
            )
        }.launchIn(viewModelScope)
    }
}
