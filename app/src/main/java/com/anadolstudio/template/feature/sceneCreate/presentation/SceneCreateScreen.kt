package com.anadolstudio.template.feature.sceneCreate.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.anadolstudio.compose.ui.theme.AppTheme
import com.anadolstudio.compose.ui.theme.Dimension
import com.anadolstudio.compose.ui.theme.Shapes
import com.anadolstudio.compose.ui.theme.largeShimmer
import com.anadolstudio.compose.ui.view.button.OutlineButtonLarge
import com.anadolstudio.compose.ui.view.button.PrimaryButtonLarge
import com.anadolstudio.compose.ui.view.snackbar.SnackbarHostState
import com.anadolstudio.compose.ui.view.text.LargeTextField
import com.anadolstudio.template.di.viewmodel.daggerViewModel
import com.anadolstudio.template.event.ObserveEvents
import com.anadolstudio.template.feature.deviceDetail.presentation.DeviceDetailResult
import com.anadolstudio.template.feature.home.domain.model.DeviceImage
import com.anadolstudio.template.feature.home.domain.model.entity.HomeAssistantEntity
import com.anadolstudio.template.feature.home.domain.model.states.HomeAssistantAttribute
import com.anadolstudio.template.feature.home.presentation.components.DeviceImageView
import com.anadolstudio.template.feature.main.NavigationController
import com.anadolstudio.template.feature.sceneCreate.data.mapper.toSceneConfigPayload
import com.anadolstudio.template.feature.sceneCreate.domain.model.SceneEntityState
import com.anadolstudio.template.navigation.ObserveResultValue
import com.anadolstudio.utils.states.ProgressState
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

private val PICKER_DEVICE_IMAGE_SIZE = 48.dp

private val previewJson = Json {
    prettyPrint = true
    prettyPrintIndent = "  "
}

@Composable
internal fun SceneCreateScreen(
        navigator: NavigationController,
        snackbarHostState: SnackbarHostState,
        editSceneConfigId: String? = null,
        viewModel: SceneCreateViewModel = daggerViewModel(),
) {
    val state by viewModel.stateFlow.collectAsState()
    ObserveEvents(viewModel.events, snackbarHostState, navigator)

    LaunchedEffect(editSceneConfigId) {
        if (editSceneConfigId != null) viewModel.onEditModeRequested(editSceneConfigId)
    }

    // Снапшот должен прийти ПЕРЕД entity-результатом — declaration order даёт нужный порядок
    // обработки в LaunchedEffect (compose запускает их последовательно).
    ObserveResultValue<ArrayList<HomeAssistantEntity<HomeAssistantAttribute>>>(
            navigator = navigator,
            key = SCENE_DEVICE_SNAPSHOT_KEY,
    ) { snapshot ->
        viewModel.onSnapshotCaptured(snapshot)
    }

    ObserveResultValue<ArrayList<HomeAssistantEntity<HomeAssistantAttribute>>>(
            navigator = navigator,
            key = DeviceDetailResult.KEY,
    ) { entities ->
        viewModel.onDeviceConfigured(entities)
    }

    // Маркер для AutomationList — на dispose ставим в previousBackStackEntry,
    // AutomationList перезагрузит список сцен. Нужно, потому что WS state_changed
    // не порождает появление НОВОЙ entity в списке, только обновляет существующие.
    val previousBackStackEntry = remember { navigator.previousBackStackEntry }
    DisposableEffect(Unit) {
        onDispose {
            previousBackStackEntry?.savedStateHandle?.set(SCENE_LIST_NEEDS_REFRESH_KEY, true)
        }
    }

    BackHandler { viewModel.onCloseClicked() }

    SceneCreateLayout(state = state, controller = viewModel)
}

@Composable
private fun SceneCreateLayout(
        state: SceneCreateScreenState,
        controller: SceneCreateController,
) {
    Column(
            modifier = Modifier
                    .fillMaxSize()
                    .background(AppTheme.colors.colorSecondary)
                    .statusBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
    ) {
        Header(
                title = if (state.isEditMode) "Редактирование сцены" else "Создание сцены",
                onCloseClicked = controller::onCloseClicked,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
                modifier = Modifier
                        .fillMaxSize()
                        .shadow(2.dp, Shapes.largeShimmer)
                        .background(AppTheme.colors.colorPrimary)
                        .padding(vertical = Dimension.smallMargin)
        ) {
            LargeTextField(
                    value = state.name,
                    onValueChange = controller::onNameChanged,
                    labelText = "Название сцены",
                    isRequired = true,
                    showHint = false,
                    modifier = Modifier.fillMaxWidth(),
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        DevicesSection(
                devices = state.devices,
                onAddClicked = controller::onAddDeviceClicked,
                onDeviceEditClicked = controller::onDeviceEditClicked,
                onDeviceRemoved = controller::onDeviceRemoved,
        )

        Spacer(modifier = Modifier.height(24.dp))

        PreviewBlock(
                state = state,
                onToggle = controller::onPreviewToggled,
        )

        Spacer(modifier = Modifier.height(24.dp))

        state.validationError?.let { error ->
            Text(
                    text = error,
                    style = AppTheme.typography.captionBook14,
                    color = AppTheme.colors.colorError,
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        PrimaryButtonLarge(
                text = "Сохранить",
                onClick = controller::onSaveClicked,
                enabled = state.canSave,
                loading = state.progressState is ProgressState.Loading,
        )

        if (state.createdSceneEntityId != null) {
            Spacer(modifier = Modifier.height(12.dp))
            OutlineButtonLarge(
                    text = "Запустить сцену",
                    onClick = controller::onRunCreatedSceneClicked,
                    icon = rememberVectorPainter(Icons.Outlined.PlayArrow),
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Spacer(modifier = Modifier.navigationBarsPadding())
    }
}

@Composable
private fun Header(title: String, onCloseClicked: () -> Unit) {
    Row(
            modifier = Modifier.fillMaxWidth(),
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

@Composable
private fun DevicesSection(
        devices: Map<String, DeviceDraftCard>,
        onAddClicked: () -> Unit,
        onDeviceEditClicked: (String) -> Unit,
        onDeviceRemoved: (String) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
                text = "Устройства в сцене",
                style = AppTheme.typography.textMedium18,
                color = AppTheme.colors.colorAccent,
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (devices.isEmpty()) {
            Text(
                    text = "Пока нет устройств. Добавьте хотя бы одно, чтобы сохранить сцену.",
                    style = AppTheme.typography.captionBook14,
                    color = AppTheme.colors.textSecondary,
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                devices.values.forEach { card ->
                    DeviceCardView(
                            card = card,
                            onEditClicked = { onDeviceEditClicked(card.deviceId) },
                            onRemoveClicked = { onDeviceRemoved(card.deviceId) },
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlineButtonLarge(
                text = "Добавить устройство",
                onClick = onAddClicked,
                icon = rememberVectorPainter(Icons.Outlined.Add),
        )
    }
}

@Composable
private fun DeviceCardView(
        card: DeviceDraftCard,
        onEditClicked: () -> Unit,
        onRemoveClicked: () -> Unit,
) {
    Column(
            modifier = Modifier
                    .fillMaxWidth()
                    .shadow(2.dp, Shapes.largeShimmer)
                    .background(AppTheme.colors.colorPrimary)
                    .padding(horizontal = 12.dp, vertical = 12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            DeviceImageView(
                    image = when (val image = card.deviceImage) {
                        is DeviceImage.HaIconType -> image.copy(haIcon = image.haIcon.copy(tint = null))
                        else -> image
                    },
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
                        card.manufacturer?.takeIf { it.isNotBlank() },
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
                        contentDescription = "Изменить",
                        tint = AppTheme.colors.colorAccent,
                )
            }
            IconButton(onClick = onRemoveClicked) {
                Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Удалить",
                        tint = AppTheme.colors.colorAccent,
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            card.entities.forEachIndexed { index ,entityItem ->
                if (index!= 0) {
                    Divider(modifier = Modifier.fillMaxWidth(), color = AppTheme.colors.colorSecondary)
                }
                SceneEntityRow(item = entityItem)
            }
        }
    }
}

/**
 * Визуальный ряд entity — иконка + имя + текст target-состояния. Стиль наследует
 * [com.anadolstudio.template.feature.deviceDetail.presentation.DeviceDetailScreen.EntityValueRow]
 * (тот же контейнер colorPrimary + largeShimmer + 24dp icon), но без интерактивных контролов:
 * SceneCreate показывает финальное состояние, редактируется оно в DeviceDetail.
 */
@Composable
private fun SceneEntityRow(item: DeviceDraftEntityItem) {
    val tint = when (val state = item.state) {
        is SceneEntityState.Light -> state.rgbColor
                ?.takeIf { it.size == 3 && state.on }
                ?.let { Color(red = it[0], green = it[1], blue = it[2]) }
                ?: AppTheme.colors.colorAccent

        else -> AppTheme.colors.colorAccent
    }
    Row(
            modifier = Modifier
                    .fillMaxWidth()
                    .clip(shape = Shapes.largeShimmer)
                    .background(AppTheme.colors.colorPrimary)
                    .heightIn(min = LocalMinimumInteractiveComponentSize.current)
                    .padding(vertical = Dimension.mediumMargin, horizontal = Dimension.smallMargin),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimension.smallMargin),
    ) {
        Icon(
                painter = painterResource(item.drawableRes),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = tint,
        )
        Text(
                modifier = Modifier.weight(1f),
                text = item.displayName,
                style = AppTheme.typography.captionBook16,
                color = AppTheme.colors.colorAccent,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
        )
        Text(
                text = item.state.describeShort(),
                style = AppTheme.typography.captionBook16,
                color = AppTheme.colors.colorAccent,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun PreviewBlock(
        state: SceneCreateScreenState,
        onToggle: () -> Unit,
) {
    Column(
            modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(Dimension.smallMargin))
                    .background(AppTheme.colors.colorPrimary)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                    modifier = Modifier.weight(1f),
                    text = "Предпросмотр данных",
                    style = AppTheme.typography.textBook18,
                    color = AppTheme.colors.colorAccent,
            )
            IconButton(onClick = onToggle) {
                Icon(
                        imageVector = if (state.isPreviewExpanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                        contentDescription = null,
                        tint = AppTheme.colors.colorAccent,
                )
            }
        }
        AnimatedVisibility(visible = state.isPreviewExpanded) {
            Box(modifier = Modifier.padding(top = 8.dp)) {
                Text(
                        text = previewJson.encodeToString(
                                JsonObject.serializer(),
                                state.draft.toSceneConfigPayload(),
                        ),
                        style = AppTheme.typography.captionBook14.copy(fontFamily = FontFamily.Monospace),
                        color = AppTheme.colors.colorAccent,
                )
            }
        }
    }
}

private fun SceneEntityState.describeShort(): String = when (this) {
    is SceneEntityState.Light -> buildString {
        append(if (on) "вкл" else "выкл")
        if (on) {
            brightness?.let { append(" · ${(it * 100 / 255).coerceIn(0, 100)}%") }
            colorTempKelvin?.let { append(" · $it K") }
            rgbColor?.takeIf { it.size == 3 }?.let { append(" · RGB ${it.joinToString()}") }
        }
    }

    is SceneEntityState.Switch -> if (on) "вкл" else "выкл"
    is SceneEntityState.Number -> value.toString()
    is SceneEntityState.Select -> option
}
