package com.anadolstudio.template.feature.sceneCreate.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.anadolstudio.compose.ui.theme.AppTheme
import com.anadolstudio.compose.ui.theme.Dimmens
import com.anadolstudio.compose.ui.theme.Shapes
import com.anadolstudio.compose.ui.theme.largeShimmer
import com.anadolstudio.compose.ui.view.button.OutlineButtonLarge
import com.anadolstudio.compose.ui.view.button.PrimaryButtonLarge
import com.anadolstudio.compose.ui.view.snackbar.SnackbarHostState
import com.anadolstudio.compose.ui.view.stub.ErrorStub
import com.anadolstudio.compose.ui.view.text.LargeTextField
import com.anadolstudio.template.R
import com.anadolstudio.template.base.view.HomeHubLoader
import com.anadolstudio.template.di.viewmodel.assistedViewModel
import com.anadolstudio.template.di.viewmodel.rememberViewModelFactory
import com.anadolstudio.template.event.ObserveEvents
import com.anadolstudio.template.feature.deviceDetail.demo.DemoDeviceDetailResult
import com.anadolstudio.template.feature.home.domain.model.HomeAssistantDevice
import com.anadolstudio.template.feature.home.domain.model.states.AllowedState
import com.anadolstudio.template.feature.home.domain.model.states.HomeAssistantState
import com.anadolstudio.template.feature.home.domain.model.states.LightAttribute
import com.anadolstudio.template.feature.home.presentation.components.DeviceImageView
import com.anadolstudio.template.feature.main.NavigationController
import com.anadolstudio.template.navigation.ObserveResultValue
import com.anadolstudio.utils.states.ProgressState
import kotlin.math.roundToInt
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json

private val PICKER_DEVICE_IMAGE_SIZE = 48.dp

@OptIn(ExperimentalSerializationApi::class)
private val previewJson = Json {
    prettyPrint = true
    prettyPrintIndent = "  "
}

@Composable
internal fun SceneCreateScreen(
        navigator: NavigationController,
        snackbarHostState: SnackbarHostState,
        editSceneConfigId: String? = null,
) {
    if (LocalLifecycleOwner.current.lifecycle.currentState == Lifecycle.State.DESTROYED) return

    val factory = rememberViewModelFactory<SceneCreateViewModel.Factory>()
    val viewModel = assistedViewModel { factory.create(editSceneConfigId) }

    val state by viewModel.stateFlow.collectAsState()
    ObserveEvents(viewModel.events, snackbarHostState, navigator)

    ObserveResultValue<HomeAssistantDevice>(
            navigator = navigator,
            key = SCENE_DEVICE_SNAPSHOT_KEY,
    ) { snapshot ->
        viewModel.onSnapshotAdded(snapshot)
    }

    ObserveResultValue<Set<String>>(
            navigator = navigator,
            key = DemoDeviceDetailResult.KEY,
    ) { selectedEntities ->
        viewModel.onDeviceConfigured(selectedEntities)
    }

    BackHandler { viewModel.onCloseClicked() }

    SceneCreateLayout(state = state, controller = viewModel)
}

@Composable
private fun SceneCreateLayout(
        state: SceneCreateScreenState,
        controller: SceneCreateController,
) {
    val progressState = remember(state) { state.progressState }

    Column(
            modifier = Modifier
                    .fillMaxSize()
                    .background(AppTheme.colors.colorSecondary)
                    .statusBarsPadding(),
    ) {
        Header(
                title = stringResource(
                        if (state.isEditMode) R.string.scene_create_title_edit else R.string.scene_create_title_create
                ),
                onCloseClicked = controller::onCloseClicked,
        )

        when (progressState) {
            ProgressState.Content -> SceneCreateContent(state = state, controller = controller)
            is ProgressState.Error -> SceneCreateError(progressState)
            ProgressState.Loading -> SceneCreateLoading()
            else -> Unit
        }
    }
}

@Composable
private fun SceneCreateContent(
        state: SceneCreateScreenState,
        controller: SceneCreateController,
) {
    LazyColumn(
            modifier = Modifier.fillMaxSize(),
    ) {
        item(key = "top_spacer") { Spacer(modifier = Modifier.height(16.dp)) }

        item(key = "name_field") {
            Row(
                    modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Dimmens.mainMargin)
                            .shadow(2.dp, Shapes.largeShimmer)
                            .background(AppTheme.colors.colorPrimary)
                            .padding(vertical = Dimmens.smallMargin)

            ) {
                LargeTextField(
                        value = state.name,
                        onValueChange = controller::onNameChanged,
                        labelText = stringResource(R.string.scene_create_label_name),
                        isRequired = true,
                        showHint = false,
                        modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = Dimmens.mediumMargin),
                )
            }
        }

        item(key = "name_to_devices_spacer") { Spacer(modifier = Modifier.height(24.dp)) }

        devicesSection(
                devices = state.selectedDeviceDraftSet,
                onAddClicked = controller::onAddClicked,
                onDeviceEditClicked = controller::onDeviceEditClicked,
                onDeviceRemoved = controller::onDeviceRemoved,
                onEntityRemoved = controller::onEntityRemoved,
        )

        item(key = "devices_to_save_spacer") { Spacer(modifier = Modifier.height(24.dp)) }

        item(key = "save_button") {
            PrimaryButtonLarge(
                    modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Dimmens.mainMargin)
                            .animateItem(),
                    elevation = ButtonDefaults.elevation(),
                    text = stringResource(R.string.scene_create_button_save),
                    onClick = controller::onSaveClicked,
                    enabled = state.canSave,
                    loading = state.progressState is ProgressState.Loading,
            )
        }

        item(key = "bottom_spacer") {
            Spacer(
                    modifier = Modifier
                            .height(16.dp)
                            .navigationBarsPadding()
            )
        }
    }
}

@Composable
private fun SceneCreateLoading() {
    Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
    ) {
        HomeHubLoader(modifier = Modifier)
    }
}

@Composable
private fun SceneCreateError(progressState: ProgressState.Error) {
    Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
    ) {
        ErrorStub(
                errorTitle = stringResource(R.string.common_error),
                errorMessage = progressState.error?.message.orEmpty(),
                buttonTitle = null,
                onRefreshClick = null,
        )
    }
}

@Composable
private fun Header(title: String, onCloseClicked: () -> Unit) {
    Row(
            modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onCloseClicked) {
            Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = null,
                    tint = AppTheme.colors.colorAccent,
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
                text = title,
                style = AppTheme.typography.textMedium18,
                color = AppTheme.colors.colorAccent,
        )
    }
}

private fun LazyListScope.devicesSection(
        devices: Set<DeviceDraftCard>,
        onAddClicked: () -> Unit,
        onDeviceEditClicked: (DeviceDraftCard) -> Unit,
        onDeviceRemoved: (DeviceDraftCard) -> Unit,
        onEntityRemoved: (deviceId: String, entityId: HomeAssistantState<*>) -> Unit,
) {
    item(key = "devices_section_title") {
        Text(
                modifier = Modifier.padding(horizontal = Dimmens.mainMargin),
                text = stringResource(R.string.scene_create_section_devices),
                style = AppTheme.typography.textMedium18,
                color = AppTheme.colors.colorAccent,
        )
    }

    item(key = "devices_section_title_spacer") { Spacer(modifier = Modifier.height(8.dp)) }

    item(key = "devices_section_add_button") {
        OutlineButtonLarge(
                modifier = Modifier.padding(horizontal = Dimmens.mainMargin),
                text = stringResource(R.string.scene_create_button_add_device),
                onClick = onAddClicked,
                icon = rememberVectorPainter(Icons.Outlined.Add),
        )
    }

    item(key = "devices_section_add_button_spacer") { Spacer(modifier = Modifier.height(12.dp)) }

    if (devices.isEmpty()) {
        item(key = "devices_section_empty") {
            Text(
                    modifier = Modifier
                            .padding(horizontal = Dimmens.mainMargin)
                            .animateItem(),
                    text = stringResource(R.string.scene_create_empty_devices),
                    style = AppTheme.typography.captionBook14,
                    color = AppTheme.colors.textSecondary,
            )
        }
    } else {
        items(
                items = devices.reversed(),
                key = { card -> card.id },
        ) { card ->
            DeviceCardView(
                    modifier = Modifier
                            .padding(horizontal = Dimmens.mainMargin)
                            .padding(bottom = 12.dp)
                            .animateItem(),
                    card = card,
                    onEditClicked = { onDeviceEditClicked(card) },
                    onRemoveClicked = { onDeviceRemoved(card) },
                    onEntityRemoved = { state -> onEntityRemoved(card.id, state) },
            )
        }
    }
}

@Composable
private fun DeviceCardView(
        card: DeviceDraftCard,
        onEditClicked: () -> Unit,
        onRemoveClicked: () -> Unit,
        onEntityRemoved: (state: HomeAssistantState<*>) -> Unit,
        modifier: Modifier = Modifier,
) {
    Column(
            modifier = modifier
                    .fillMaxWidth()
                    .shadow(2.dp, Shapes.largeShimmer)
                    .background(AppTheme.colors.colorPrimary)
                    .padding(horizontal = 12.dp, vertical = 12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            DeviceImageView(
                    image = card.deviceImage,
                    modifier = Modifier.size(PICKER_DEVICE_IMAGE_SIZE),
                    imageSize = PICKER_DEVICE_IMAGE_SIZE,
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                        text = card.name,
                        style = AppTheme.typography.textMedium18,
                        color = AppTheme.colors.colorAccent,
                )
                val subtitleParts = listOfNotNull(
                        "id - ${card.id.takeLast(4)}",
                        card.model?.takeIf { it.isNotBlank() },
                        card.areaName,
                )
                if (subtitleParts.isNotEmpty()) {
                    Text(
                            text = subtitleParts.joinToString(separator = " · "),
                            style = AppTheme.typography.captionBook14,
                            color = AppTheme.colors.textSecondary,
                    )
                }
            }
            IconButton(onClick = onEditClicked) {
                Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = stringResource(R.string.scene_create_action_edit),
                        tint = AppTheme.colors.colorAccent,
                )
            }
            IconButton(onClick = onRemoveClicked) {
                Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = stringResource(R.string.scene_create_action_delete),
                        tint = AppTheme.colors.colorAccent,
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        Column(
                modifier = Modifier.animateContentSize(),
                verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            card.changeEntityStates.forEachIndexed { index, entityState ->
                if (index != 0) {
                    Divider(modifier = Modifier.fillMaxWidth(), color = AppTheme.colors.colorSecondary)
                }
                SceneEntityRow(
                        state = entityState,
                        name = card.allEntityIdToNameMap[entityState.entityId],
                        onDeleteClicked = { onEntityRemoved(entityState) },
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SceneEntityRow(
        state: HomeAssistantState<*>,
        name: String?,
        onDeleteClicked: (() -> Unit),
) {
    val tint = when (val attributes = state.attributes) {
        is LightAttribute -> attributes.rgbColor
                .takeIf { it.size == 3 && state.allowedState is AllowedState.On }
                ?.let { Color(red = it[0], green = it[1], blue = it[2]) }
                ?: AppTheme.colors.colorAccent

        else -> AppTheme.colors.colorAccent
    }

    val actionWidthPx = with(LocalDensity.current) { ENTITY_DELETE_ACTION_WIDTH.toPx() }
    val velocityThresholdPx = with(LocalDensity.current) { 100.dp.toPx() }
    val scope = rememberCoroutineScope()

    val swipeState = remember {
        AnchoredDraggableState(
                initialValue = SwipeRevealState.Closed,
                anchors = DraggableAnchors {
                    SwipeRevealState.Closed at 0f
                    SwipeRevealState.Open at -actionWidthPx
                },
                positionalThreshold = { totalDistance -> totalDistance * 0.5f },
                velocityThreshold = { velocityThresholdPx },
                snapAnimationSpec = spring(),
                decayAnimationSpec = exponentialDecay(),
        )
    }
    val isOpen = swipeState.currentValue == SwipeRevealState.Open

    Box(
            modifier = Modifier.background(AppTheme.colors.colorError, Shapes.largeShimmer),
            contentAlignment = Alignment.CenterEnd,
    ) {
        Box(
                modifier = Modifier
                        .heightIn(min = LocalMinimumInteractiveComponentSize.current)
                        .width(ENTITY_DELETE_ACTION_WIDTH)
                        .clickable(enabled = isOpen) {
                            scope.launch { swipeState.animateToValue(SwipeRevealState.Closed) }
                            onDeleteClicked()
                        },
                contentAlignment = Alignment.Center,
        ) {
            Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = stringResource(R.string.scene_create_action_delete),
                    tint = AppTheme.colors.colorPrimary,
            )
        }
        SceneEntityRowContent(
                state = state,
                name = name,
                tint = tint,
                modifier = Modifier
                        .offset { IntOffset(swipeState.requireOffset().roundToInt(), 0) }
                        .anchoredDraggable(state = swipeState, orientation = Orientation.Horizontal)
                        .clickable(enabled = isOpen) {
                            scope.launch { swipeState.animateToValue(SwipeRevealState.Closed) }
                        },
        )
    }
}

@Composable
private fun SceneEntityRowContent(
        state: HomeAssistantState<*>,
        name: String?,
        tint: Color,
        modifier: Modifier = Modifier,
) {
    Row(
            modifier = modifier
                    .fillMaxWidth()
                    .background(color = AppTheme.colors.colorPrimary)
                    .heightIn(min = LocalMinimumInteractiveComponentSize.current)
                    .padding(vertical = Dimmens.mediumMargin, horizontal = Dimmens.smallMargin),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimmens.smallMargin),
    ) {
        Icon(
                painter = painterResource(state.icon.drawableRes),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = tint,
        )
        Text(
                modifier = Modifier.weight(1f),
                text = name ?: state.attributes.friendlyName,
                style = AppTheme.typography.captionBook16,
                color = AppTheme.colors.colorAccent,
                overflow = TextOverflow.Ellipsis,
                maxLines = 2,
        )
        Text(
                text = state.describeShort(),
                style = AppTheme.typography.captionBook16,
                color = AppTheme.colors.colorAccent,
                maxLines = 2,
        )
    }
}

private val ENTITY_DELETE_ACTION_WIDTH = 72.dp

private enum class SwipeRevealState { Closed, Open }

@OptIn(ExperimentalFoundationApi::class)
private suspend fun <T> AnchoredDraggableState<T>.animateToValue(targetValue: T) {
    anchoredDrag(targetValue = targetValue) { anchors, target ->
        val to = anchors.positionOf(target)
        if (!to.isNaN()) {
            val from = this@animateToValue.requireOffset()
            animate(initialValue = from, targetValue = to) { value, _ -> dragTo(value) }
        }
    }
}

@Composable
private fun HomeAssistantState<*>.describeShort(): String {
    return when (attributes) {
        is LightAttribute -> buildString {
            append(allowedState.value)

            if (allowedState is AllowedState.On) {
                attributes.brightness?.let { append(" · ${(it * 100 / 255).coerceIn(0, 100)}%") }
                attributes.colorKelvin?.let { append(" · $it K") }
                attributes.rgbColor.takeIf { it.size == 3 }?.let { append(" · RGB ${it.joinToString()}") }
            }
        }

        else -> allowedState.value
    }
}
