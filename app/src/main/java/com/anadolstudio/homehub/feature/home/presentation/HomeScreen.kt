package com.anadolstudio.homehub.feature.home.presentation

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import com.anadolstudio.compose.ui.theme.AppTheme
import com.anadolstudio.compose.ui.theme.Dimmens
import com.anadolstudio.compose.ui.theme.preview.ThemePreviewParameter
import com.anadolstudio.compose.ui.view.snackbar.SnackbarHostState
import com.anadolstudio.compose.ui.view.state.Loader
import com.anadolstudio.compose.ui.view.stub.ErrorStub
import com.anadolstudio.homehub.R
import com.anadolstudio.homehub.base.view.HomeHubLoader
import com.anadolstudio.homehub.base.viewmodel.ObserveViewModelLifecycle
import com.anadolstudio.homehub.di.viewmodel.daggerViewModel
import com.anadolstudio.homehub.event.ObserveEvents
import com.anadolstudio.homehub.feature.home.domain.model.Area
import com.anadolstudio.homehub.feature.home.domain.model.DeviceImage
import com.anadolstudio.homehub.feature.home.domain.model.HomeAssistantDevice
import com.anadolstudio.homehub.feature.home.domain.model.entity.HomeAssistantEntity
import com.anadolstudio.homehub.feature.home.domain.model.services.HomeAssistantService
import com.anadolstudio.homehub.feature.home.domain.model.services.SimpleToggleableService
import com.anadolstudio.homehub.feature.home.domain.model.states.HomeAssistantAttribute
import com.anadolstudio.homehub.feature.home.presentation.components.AreaChipRow
import com.anadolstudio.homehub.feature.home.presentation.components.DEVICE_IMAGE_MAX_SIZE
import com.anadolstudio.homehub.feature.home.presentation.components.DeviceCard
import com.anadolstudio.homehub.feature.home.presentation.components.deviceCardRequiredWidth
import com.anadolstudio.homehub.feature.main.NavigationController
import com.anadolstudio.utils.states.ProgressState
import kotlin.math.ceil
import kotlin.math.roundToInt

private val HEADER_MAX_HEIGHT = 320.dp
private val BADGE_HEIGHT = 56.dp
private val BADGE_BOTTOM_INSET = 12.dp
private val CONTENT_CORNER_RADIUS = 32.dp
private val CONTENT_HEADER_OVERLAP = CONTENT_CORNER_RADIUS
private val DEVICE_CELL_MIN_WIDTH = 100.dp
private val DEVICE_CELL_MAX_WIDTH = 300.dp

private const val KEY_AREA_CHIPS = "area_chips"
private const val KEY_PREFIX_HEADER = "header_"
private const val KEY_PREFIX_DEVICES = "devices_"
private const val CONTENT_TYPE_AREA_CHIPS = "area_chips"
private const val CONTENT_TYPE_GROUP_HEADER = "group_header"
private const val CONTENT_TYPE_DEVICE_ROW = "device_row"

@Composable
internal fun HomeScreen(
        navigator: NavigationController,
        snackbarHostState: SnackbarHostState,
        viewModel: HomeViewModel = daggerViewModel(),
) {
    val state by viewModel.stateFlow.collectAsState()
    ObserveEvents(viewModel.events, snackbarHostState, navigator)
    ObserveViewModelLifecycle(viewModel)

    HomeLayout(state = state, controller = viewModel)
}

@Composable
private fun HomeLayout(
        state: HomeScreenState,
        controller: HomeController,
) {
    val progressState = remember(state) { state.progressState }

    val density = LocalDensity.current
    val maxHeaderPx = with(density) { HEADER_MAX_HEIGHT.toPx() }
    val headerOffsetPx = remember { mutableFloatStateOf(0f) }

    // Асимметричное поведение CollapsingToolbar:
    // — палец вверх (delta < 0, сжатие шапки): шапка ест дельту первой через onPreScroll;
    // — палец вниз (delta > 0, раскрытие шапки): LazyGrid скроллится первым, и только
    //   когда он упрётся в верх и отдаст остаток дельты — шапка раскрывается через onPostScroll.
    val nestedScrollConnection = remember(maxHeaderPx) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                if (delta >= 0f) return Offset.Zero
                val newOffset = (headerOffsetPx.floatValue + delta).coerceIn(-maxHeaderPx, 0f)
                val consumed = newOffset - headerOffsetPx.floatValue
                headerOffsetPx.floatValue = newOffset
                return Offset(0f, consumed)
            }

            override fun onPostScroll(
                    consumed: Offset,
                    available: Offset,
                    source: NestedScrollSource,
            ): Offset {
                val delta = available.y
                if (delta <= 0f) return Offset.Zero
                val newOffset = (headerOffsetPx.floatValue + delta).coerceIn(-maxHeaderPx, 0f)
                val consumedHere = newOffset - headerOffsetPx.floatValue
                headerOffsetPx.floatValue = newOffset
                return Offset(0f, consumedHere)
            }
        }
    }

    val overlapPx = with(density) { CONTENT_HEADER_OVERLAP.roundToPx() }

    Box(
            modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(nestedScrollConnection),
    ) {
        HeaderPlaceholder(
                modifier = Modifier
                        .fillMaxWidth()
                        .height(HEADER_MAX_HEIGHT),
        )

        Row(
                horizontalArrangement = Arrangement.spacedBy(
                        space = Dimmens.smallMargin,
                        alignment = Alignment.End,
                ),
                modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = Dimmens.mainMargin)
                        .offset {
                            IntOffset(
                                    x = 0,
                                    y = (headerOffsetPx.floatValue + maxHeaderPx).toInt() -
                                            with(density) {
                                                (BADGE_HEIGHT + BADGE_BOTTOM_INSET + CONTENT_HEADER_OVERLAP)
                                                        .roundToPx()
                                            },
                            )
                        },
        ) {
            Badge(
                    text = stringResource(R.string.automation_button),
                    onClick = { controller.onAutomationClicked() }
            )
            Badge(
                    vector = Icons.Outlined.History,
                    onClick = { controller.onHistoryClicked() }
            )
            Badge(
                    vector = Icons.Outlined.Add,
                    onClick = { controller.onAddClicked() }
            )
        }

        Column(
                modifier = Modifier
                        .fillMaxSize()
                        .layout { measurable, constraints ->
                            val topPx = (maxHeaderPx + headerOffsetPx.floatValue - overlapPx)
                                    .coerceAtLeast(0f)
                                    .roundToInt()
                            val height = (constraints.maxHeight - topPx).coerceAtLeast(0)
                            val placeable = measurable.measure(
                                    constraints.copy(minHeight = height, maxHeight = height),
                            )
                            layout(constraints.maxWidth, constraints.maxHeight) {
                                placeable.place(0, topPx)
                            }
                        }
                        .clip(RoundedCornerShape(topStart = CONTENT_CORNER_RADIUS, topEnd = CONTENT_CORNER_RADIUS))
                        .background(color = AppTheme.colors.colorSecondary),
        ) {

            if (state.homeName != null) {
                Text(
                        text = state.homeName,
                        style = AppTheme.typography.textBook23,
                        fontWeight = FontWeight.Bold,
                        color = AppTheme.colors.colorAccent,
                        modifier = Modifier
                                .fillMaxWidth()
                                .statusBarsPadding()
                                .padding(horizontal = Dimmens.mainMargin)
                                .padding(bottom = Dimmens.smallMargin, top = Dimmens.smallMargin),
                )
            }

            when (progressState) {
                ProgressState.Refresh, ProgressState.Content -> HomeContent(state = state, controller = controller)
                is ProgressState.Error -> HomeError(progressState)
                ProgressState.Loading -> HomeLoading()
                else -> Unit
            }
        }
    }
}

@Composable
private fun HeaderPlaceholder(modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                        .data(R.mipmap.background_home)
                        .crossfade(false)
                        .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
private fun Badge(
        onClick: () -> Unit,
        text: String? = null,
        vector: ImageVector? = null,
) {
    if (text == null && vector == null) return

    Box(
            modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .height(BADGE_HEIGHT)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onClick)
                    .background(AppTheme.colors.colorPrimary)
                    .padding(horizontal = Dimmens.mediumMargin),
            contentAlignment = Alignment.Center,
    ) {
        vector?.let {
            Icon(
                    imageVector = it,
                    tint = AppTheme.colors.colorAccent,
                    contentDescription = null
            )
        }

        text?.let {
            Text(
                    text = it,
                    style = AppTheme.typography.textBook14,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.textPrimary,
            )
        }

    }
}

@Composable
private fun HomeLoading() {
    Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
    ) {
        HomeHubLoader(modifier = Modifier)
    }
}

@Composable
private fun HomeError(progressState: ProgressState.Error) {
    Box(
            modifier = Modifier
                    .fillMaxSize(),
            contentAlignment = Alignment.Center
    ) {
        ErrorStub(
                errorTitle = "Заголовок ошибки", //  TODO
                errorMessage = progressState.error?.message.orEmpty(),
                buttonTitle = "Название кнопки",
                onRefreshClick = {},
                fillMaxSize = false,
        )
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
private fun HomeContent(
        state: HomeScreenState,
        controller: HomeController,
) {
    val deviceMap = state.filteredAreaToDeviceMap

    val listState = rememberLazyGridState()
    val isRefreshing = state.progressState == ProgressState.Refresh

    val context = LocalContext.current
    val density = LocalDensity.current
    val imageSizePx = with(density) { DEVICE_IMAGE_MAX_SIZE.roundToPx() }
    val textMeasurer = rememberTextMeasurer()
    val entityTextStyle = AppTheme.typography.captionMedium12
    val availableAreas = remember(state.deviceState.availableAreas.isNotEmpty()) {
        state.deviceState.availableAreas.isNotEmpty()
    }

    LaunchedEffect(deviceMap) {
        val urls = deviceMap.values
                .asSequence()
                .flatten()
                .mapNotNull { (it.image as? DeviceImage.ImageUrlType)?.url }
                .distinct()
                .toList()

        if (urls.isEmpty()) return@LaunchedEffect
        val imageLoader = context.imageLoader

        urls.forEach { url ->
            imageLoader.enqueue(
                    ImageRequest.Builder(context)
                            .data(url)
                            .size(imageSizePx)
                            .build()
            )
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val cellSpacing = Dimmens.smallMargin
        val gridInnerWidth = maxWidth - Dimmens.mainMargin * 2

        val deviceSpans = remember(deviceMap, gridInnerWidth) {
            val columns = ((gridInnerWidth + cellSpacing) / (DEVICE_CELL_MIN_WIDTH + cellSpacing))
                    .toInt()
                    .coerceAtLeast(1)
            val cellWidth = (gridInnerWidth - cellSpacing * (columns - 1)) / columns
            deviceMap.values.flatten().associate { device ->
                val required = deviceCardRequiredWidth(
                        entityList = device.targetEntityList,
                        textMeasurer = textMeasurer,
                        textStyle = entityTextStyle,
                        density = density,
                )
                device.id to ceil(required.value / cellWidth.value).toInt().coerceIn(1, columns)
            }
        }

        LazyVerticalGrid(
                state = listState,
                columns = GridCells.Adaptive(minSize = DEVICE_CELL_MIN_WIDTH),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                        start = Dimmens.mainMargin,
                        end = Dimmens.mainMargin,
                        bottom = Dimmens.largeMargin,
                ),
                horizontalArrangement = Arrangement.spacedBy(cellSpacing),
                verticalArrangement = Arrangement.spacedBy(Dimmens.mediumMargin),
        ) {
            if (availableAreas) {
                item(
                        key = KEY_AREA_CHIPS,
                        span = { GridItemSpan(maxLineSpan) },
                        contentType = CONTENT_TYPE_AREA_CHIPS,
                ) {
                    AreaChipRow(
                            modifier = Modifier
                                    .animateItem()
                                    .padding(bottom = Dimmens.smallMargin),
                            selectedAreaId = state.selectedAreaId,
                            areas = state.deviceState.availableAreas,
                            onAreaSelected = controller::onAreaSelected,
                            contentPadding = PaddingValues(horizontal = 0.dp),
                    )
                }
            }

            deviceMap.forEach { (areaName, deviceList) ->
                item(
                        key = "$KEY_PREFIX_HEADER$areaName",
                        span = { GridItemSpan(maxLineSpan) },
                        contentType = CONTENT_TYPE_GROUP_HEADER,
                ) {
                    Column {
                        GroupHeader(
                                title = areaName,
                                onClick = { controller.onAreaClicked() },
                        )
                        Spacer(modifier = Modifier.height(Dimmens.smallMargin))
                    }
                }

                items(
                        items = deviceList,
                        key = { device -> device.id },
                        span = { device ->
                            GridItemSpan((deviceSpans[device.id] ?: 1).coerceAtMost(maxLineSpan))
                        },
                        contentType = { device ->
                            "$CONTENT_TYPE_DEVICE_ROW-${device.targetEntityList.size.coerceAtMost(3)}"
                        },
                ) { device ->
                    DeviceCard(
                            modifier = Modifier
                                    .animateItem()
                                    .widthIn(max = DEVICE_CELL_MAX_WIDTH)
                                    .fillMaxWidth(),
                            device = device,
                            controller = controller,
                    )
                }
            }
        }

        RefreshIndicator(
                modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = Dimmens.largeMargin)
                        .shadow(elevation = 2.dp, shape = CircleShape)
                        .background(AppTheme.colors.colorPrimary, CircleShape),
                isRefreshing = isRefreshing
        )
    }
}

@Composable
private fun RefreshIndicator(modifier: Modifier, isRefreshing: Boolean) {
    AnimatedVisibility(
            visible = isRefreshing,
            modifier = modifier,
    ) {
        Loader(
                modifier = Modifier
                        .padding(Dimmens.smallMargin)
                        .size(24.dp),
                color = AppTheme.colors.colorAccent,
                strokeWidth = 3.dp
        )
    }
}

@Composable
private fun DeviceCard(
        device: HomeAssistantDevice,
        controller: HomeController,
        modifier: Modifier = Modifier,
) {
    DeviceCard(
            modifier = modifier,
            title = device.name,
            image = device.image,
            entityList = device.targetEntityList,
            onInnerEntityClicked = { controller.onEntityClicked(it, SimpleToggleableService.Toggle) },
            onDeviceClicked = { controller.onDeviceClicked(device) },
    )
}

@Composable
private fun GroupHeader(
        title: String,
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
) {
    Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier
                    .fillMaxWidth()
                    .padding(top = Dimmens.smallMargin),
    ) {
        Text(
                text = title,
                style = AppTheme.typography.textBook18,
                color = AppTheme.colors.colorAccent,
                fontWeight = FontWeight.SemiBold,
        )
        Spacer(modifier = Modifier.size(Dimmens.extraSmallMargin))
        Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = AppTheme.colors.textSecondary,
                modifier = Modifier
                        .size(20.dp)
                        .clickable(onClick = onClick),
        )
    }
}

// region Previews

private fun createPreviewController(): HomeController = object : HomeController {
    override fun onEntityClicked(
            entity: HomeAssistantEntity<HomeAssistantAttribute>, service: HomeAssistantService<*>,
    ) = Unit

    override fun onAreaClicked() = Unit
    override fun onDeviceClicked(device: HomeAssistantDevice) = Unit
    override fun onAutomationClicked() = Unit
    override fun onAddClicked() = Unit
    override fun onHistoryClicked() = Unit
    override fun onAreaSelected(area: Area?) = Unit
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun HomeScreenPreview(
        @PreviewParameter(ThemePreviewParameter::class) useDarkMode: Boolean,
) {
    AppTheme(useDarkMode) {
        val devices = PreviewUtils.previewDevices.toSet()
        HomeLayout(
                state = HomeScreenState(
                        deviceState = DeviceState(
                                deviceSet = devices,
                                availableAreas = devices.mapNotNull { it.area }.distinctBy { it.areaId },
                        ),
                ),
                controller = createPreviewController()
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenLoadingPreview(
        @PreviewParameter(ThemePreviewParameter::class) useDarkMode: Boolean,
) {
    AppTheme(useDarkMode) {
        HomeLayout(
                state = HomeScreenState(),
                controller = createPreviewController()
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun GroupHeaderPreview(
        @PreviewParameter(ThemePreviewParameter::class) useDarkMode: Boolean,
) {
    AppTheme(useDarkMode) {
        GroupHeader(title = "Зал", onClick = {})
    }
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun HomeScreenDevicesPreview(
        @PreviewParameter(ThemePreviewParameter::class) useDarkMode: Boolean,
) {
    AppTheme(useDarkMode) {
        val devices = PreviewUtils.previewDevices.toSet()
        val areas = devices.mapNotNull { it.area }.distinctBy { it.areaId }
        HomeLayout(
                state = HomeScreenState(
                        deviceState = DeviceState(
                                deviceSet = devices,
                                availableAreas = areas,
                        ),
                        selectedAreaId = areas.firstOrNull()?.areaId,
                ),
                controller = createPreviewController(),
        )
    }
}

// endregion
