package com.anadolstudio.template.feature.home.presentation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import coil.imageLoader
import coil.request.ImageRequest
import com.anadolstudio.compose.ui.theme.AppTheme
import com.anadolstudio.compose.ui.theme.Dimension
import com.anadolstudio.compose.ui.theme.preview.ThemePreviewParameter
import com.anadolstudio.compose.ui.view.snackbar.SnackbarHostState
import com.anadolstudio.template.base.view.HomeHubLoader
import com.anadolstudio.template.base.viewmodel.ObserveViewModelLifecycle
import com.anadolstudio.template.di.viewmodel.daggerViewModel
import com.anadolstudio.template.event.ObserveEvents
import com.anadolstudio.template.feature.home.domain.model.AllowedComponent
import com.anadolstudio.template.feature.home.domain.model.HomeAssistantDevice
import com.anadolstudio.template.feature.home.domain.model.HomeAssistantEntity
import com.anadolstudio.template.feature.home.domain.model.services.HomeAssistantService
import com.anadolstudio.template.feature.home.domain.model.services.SwitchService
import com.anadolstudio.template.feature.home.presentation.components.BaseDeviceCard
import com.anadolstudio.template.feature.home.presentation.components.SwitchCard
import com.anadolstudio.template.feature.main.NavigationController
import com.anadolstudio.utils.states.ProgressState

private const val HOME_TITLE = "Домостроительня улица 4, к.3, кв.11"
private const val GRID_COLUMNS = 3
private const val NO_AREA_ID = "__no_area__"
private const val NO_AREA_TITLE = "Без комнаты"
private val DEVICE_IMAGE_SIZE = 100.dp

@Composable
internal fun HomeScreen(
        navigator: NavigationController,
        snackbarHostState: SnackbarHostState,
        viewModel: HomeViewModel = daggerViewModel(),
) {
    val state by viewModel.stateFlow.collectAsState()
    ObserveEvents(viewModel.events, snackbarHostState, navigator)
    ObserveViewModelLifecycle(viewModel)

    HomeLayout(
            state = state,
            controller = viewModel,
    )
}

@Composable
private fun HomeLayout(
        state: HomeScreenState,
        controller: HomeController,
) {
    DevicesGrid(
            deviceMap = state.deviceMap,
            progressState = state.progressState,
            controller = controller,
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DevicesGrid(
        deviceMap: Map<String, List<HomeAssistantDevice>>,
        progressState: ProgressState,
        controller: HomeController,
) {
    val entries = remember(deviceMap) { deviceMap.entries.toList() }
    val gridState = rememberLazyGridState()

    val context = LocalContext.current
    val imageSizePx = with(LocalDensity.current) { DEVICE_IMAGE_SIZE.roundToPx() }

    LaunchedEffect(deviceMap) {
        val urls = deviceMap.values
                .asSequence()
                .flatten()
                .mapNotNull { it.imageUrl }
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

    LazyVerticalGrid(
            state = gridState,
            columns = GridCells.Fixed(GRID_COLUMNS),
            modifier = Modifier
                    .background(color = AppTheme.colors.colorSecondary)
                    .fillMaxSize()
                    .systemBarsPadding(),
            contentPadding = PaddingValues(
                    start = Dimension.mainMargin,
                    end = Dimension.mainMargin,
                    top = Dimension.mainMargin,
                    bottom = Dimension.largeMargin,
            ),
            horizontalArrangement = Arrangement.spacedBy(Dimension.mediumMargin),
            verticalArrangement = Arrangement.spacedBy(Dimension.mediumMargin),
    ) {
        item(
                span = { GridItemSpan(maxLineSpan) },
                key = HOME_TITLE
        ) {
            Text(
                    text = HOME_TITLE,
                    style = AppTheme.typography.textBook18,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.colorAccent,
                    modifier = Modifier.fillMaxWidth(),
            )
        }


        if (progressState is ProgressState.Loading && entries.isEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Box(
                        modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = Dimension.extraLargeMargin),
                        contentAlignment = Alignment.Center,
                ) {
                    HomeHubLoader(modifier = Modifier)
                }
            }
        }

        if (progressState is ProgressState.Error) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(
                        text = "Error: ${progressState.error?.message}",
                        color = AppTheme.colors.textSecondary,
                        style = AppTheme.typography.textBook14,
                )
            }
        }

        entries.forEach { (areaName, deviceList) ->
            item(span = { GridItemSpan(maxLineSpan) }, key = areaName) {
                GroupHeader(title = areaName, onClick = { })
            }

            items(
                    items = deviceList,
                    key = { device -> device.id },
                    contentType = { DEVICE_CARD_CONTENT_TYPE },
            ) { device ->
                DeviceCard(device, controller)
            }
        }
    }
}

@Composable
private fun DeviceCard(device: HomeAssistantDevice, controller: HomeController) {
    when (device.componentType) {
        AllowedComponent.SWITCH -> {
            SwitchCard(
                    title = device.name,
                    description = null,
                    imageUrl = device.imageUrl,
                    switchEntityList = device.entityList
                            .filter { it.componentType == AllowedComponent.SWITCH },
                    onInnerEntityClicked = { controller.onEntityClicked(it, SwitchService.Toggle) },
                    onDeviceClicked = { controller.onDeviceClicked(device) },
            )
        }

        else -> {
            BaseDeviceCard(
                    title = device.name,
                    description = null,
                    imageUrl = device.imageUrl,
                    onDeviceClicked = { controller.onDeviceClicked(device) },
            )
        }
    }
}

private const val DEVICE_CARD_CONTENT_TYPE = "device_card"

@Composable
private fun GroupHeader(title: String, onClick: () -> Unit) {
    Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Dimension.smallMargin),
    ) {
        Text(
                text = title,
                style = AppTheme.typography.textBook18,
                color = AppTheme.colors.colorAccent,
                fontWeight = FontWeight.SemiBold,
        )
        Spacer(modifier = Modifier.size(Dimension.extraSmallMargin))
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
    override fun onTestButtonClicked() = Unit
    override fun onEntityClicked(entity: HomeAssistantEntity, service: HomeAssistantService) = Unit
    override fun onDeviceClicked(device: HomeAssistantDevice) = Unit
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun HomeScreenPreview(
        @PreviewParameter(ThemePreviewParameter::class) useDarkMode: Boolean,
) {
    AppTheme(useDarkMode) {
        HomeLayout(
                state = HomeScreenState(deviceMap = PreviewUtils.previewDevices.groupBy { it.area?.name.orEmpty() }),
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
                state = HomeScreenState(progressState = ProgressState.Loading),
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
        HomeLayout(
                state = HomeScreenState(deviceMap = PreviewUtils.previewDevices.groupBy { it.area?.name.orEmpty() }),
                controller = createPreviewController(),
        )
    }
}

// endregion
