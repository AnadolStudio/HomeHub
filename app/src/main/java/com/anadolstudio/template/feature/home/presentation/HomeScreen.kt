package com.anadolstudio.template.feature.home.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.anadolstudio.compose.ui.theme.AppTheme
import com.anadolstudio.compose.ui.view.button.PrimaryButtonLarge
import com.anadolstudio.compose.ui.view.snackbar.SnackbarHostState
import com.anadolstudio.template.base.view.HomeHubLoader
import com.anadolstudio.template.core.websocket.WebSocketConnectionState
import com.anadolstudio.template.di.viewmodel.daggerViewModel
import com.anadolstudio.template.event.ObserveEvents
import com.anadolstudio.template.feature.home.data.model.EntityDomain
import com.anadolstudio.template.feature.home.data.model.EntityRegistryEntry
import com.anadolstudio.template.feature.main.NavigationController
import com.anadolstudio.utils.states.ProgressState

@Composable
internal fun HomeScreen(
        navigator: NavigationController,
        snackbarHostState: SnackbarHostState,
        viewModel: HomeViewModel = daggerViewModel(),
) {
    val state by viewModel.stateFlow.collectAsState()
    ObserveEvents(viewModel.events, snackbarHostState, navigator)

    HomeLayout(
            state = state,
            onGetApiStatusClicked = viewModel::onGetApiStatusClicked,
            onGetEntitiesClicked = viewModel::onGetEntitiesClicked,
            onDomainFilterClicked = viewModel::onDomainFilterClicked,
    )
}

@Composable
private fun HomeLayout(
        state: HomeState,
        onGetApiStatusClicked: () -> Unit,
        onGetEntitiesClicked: () -> Unit,
        onDomainFilterClicked: (EntityDomain?) -> Unit,
) {
    Column(
            modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
                    .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        ConnectionStateLabel(state.connectionState)

        Spacer(modifier = Modifier.height(16.dp))

        PrimaryButtonLarge(
                text = "Get API Status",
                onClick = onGetApiStatusClicked,
        )

        val apiStatusMessage = state.apiStatusMessage
        if (apiStatusMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                    text = apiStatusMessage,
                    style = AppTheme.typography.textBook18,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        val isConnected = state.connectionState is WebSocketConnectionState.ConnectedAuthenticated
        PrimaryButtonLarge(
                text = "Get Entities (WebSocket)",
                onClick = onGetEntitiesClicked,
                enabled = isConnected,
        )

        if (state.progressState is ProgressState.Loading) {
            Spacer(modifier = Modifier.height(16.dp))
            HomeHubLoader(modifier = Modifier)
        }

        if (state.progressState is ProgressState.Error) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                    text = "Error: ${(state.progressState as ProgressState.Error).error?.message}",
                    color = AppTheme.colors.textSecondary,
            )
        }

        if (state.allEntities.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))

            DomainFilterChips(
                    selectedDomain = state.selectedDomain,
                    onDomainSelected = onDomainFilterClicked,
            )

            Spacer(modifier = Modifier.height(8.dp))

            val filtered = state.filteredEntities
            Text(
                    text = "Entities (${filtered.size} / ${state.allEntities.size}):",
                    style = AppTheme.typography.textBook18,
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(filtered, key = { it.entityId }) { entity ->
                    EntityRegistryItem(entity = entity)
                }
            }
        }
    }
}

@Composable
private fun DomainFilterChips(
        selectedDomain: EntityDomain?,
        onDomainSelected: (EntityDomain?) -> Unit,
) {
    LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            FilterChip(
                    selected = selectedDomain == null,
                    onClick = { onDomainSelected(null) },
                    label = { Text(text = "All") },
            )
        }
        items(EntityDomain.entries.toList()) { domain ->
            FilterChip(
                    selected = selectedDomain == domain,
                    onClick = { onDomainSelected(domain) },
                    label = { Text(text = domain.label) },
            )
        }
    }
}

@Composable
private fun ConnectionStateLabel(connectionState: WebSocketConnectionState) {
    val label = when (connectionState) {
        is WebSocketConnectionState.Disconnected -> "Disconnected"
        is WebSocketConnectionState.Connecting -> "Connecting..."
        is WebSocketConnectionState.ConnectedUnauthenticated -> "Connected (no auth)"
        is WebSocketConnectionState.Authenticating -> "Authenticating..."
        is WebSocketConnectionState.ConnectedAuthenticated -> "Connected"
        is WebSocketConnectionState.Reconnecting -> "Reconnecting (${connectionState.attempt})..."
        is WebSocketConnectionState.Failed -> "Failed: ${connectionState.reason.message}"
    }
    Text(text = "WS: $label", style = AppTheme.typography.textBook18)
}

@Composable
private fun EntityRegistryItem(entity: EntityRegistryEntry) {
    Card(
            modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                    text = entity.displayName,
                    style = AppTheme.typography.textBook18,
            )
            Text(
                    text = "${entity.entityId} · ${entity.platform}",
                    style = AppTheme.typography.textBook14,
                    color = AppTheme.colors.textSecondary,
            )
        }
    }
}
