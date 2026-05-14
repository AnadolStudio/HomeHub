package com.anadolstudio.template.feature.automationList.presentation

import androidx.lifecycle.viewModelScope
import com.anadolstudio.template.base.viewmodel.StatefulViewModel
import com.anadolstudio.template.feature.home.domain.HAWebsocketRepository
import com.anadolstudio.template.feature.main.MainGraph.navigateToAutomationDetail
import com.anadolstudio.utils.states.LoadingContext
import com.anadolstudio.utils.states.lce.lceFlow
import com.anadolstudio.utils.states.lce.onEachContent
import com.anadolstudio.utils.states.lce.onEachProgressState
import javax.inject.Inject
import kotlinx.coroutines.flow.launchIn

internal class AutomationListViewModel @Inject constructor(
        private val websocketRepository: HAWebsocketRepository,
) : StatefulViewModel<AutomationListScreenState>(AutomationListScreenState()),
    AutomationListController {

    init {
        loadAutomationStates(LoadingContext.INIT_LOADING)
    }

    private fun loadAutomationStates(loadingContext: LoadingContext) {
        lceFlow { websocketRepository.getAllStates() }
                .onEachProgressState(
                        previousState = state.progressState,
                        loadingContext = loadingContext,
                        onNewProgressState = {
                            updateState { copy(progressState = it) }
                        }
                )
                .onEachContent { homeOverview ->
//                    updateState { copy(homeOverviewState = homeOverviewState.copy(homeState = homeOverview)) }
                }
                .launchIn(viewModelScope)
    }

    override fun onAutomationItemClicked() {
        navigateToAutomationDetail()
    }
}
