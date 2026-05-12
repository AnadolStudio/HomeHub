package com.anadolstudio.template.feature.home.presentation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.PowerSettingsNew
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
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
import com.anadolstudio.template.feature.home.data.model.HomeAssistantEntity
import com.anadolstudio.template.feature.home.domain.model.Area
import com.anadolstudio.template.feature.home.domain.model.Device
import com.anadolstudio.template.feature.main.NavigationController
import com.anadolstudio.utils.states.ProgressState

private const val HOME_TITLE = "Домостроительня улица 4, к.3, кв.11"
private const val GRID_COLUMNS = 3
private const val NO_AREA_ID = "__no_area__"
private const val NO_AREA_TITLE = "Без комнаты"

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
        state: HomeState,
        controller: HomeController,
) {
    DevicesGrid(
            deviceMap = state.deviceMap,
            progressState = state.progressState,
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DevicesGrid(
        deviceMap: Map<String, List<Device>>,
        progressState: ProgressState,
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
                DeviceCard(
                        device = device,
                        onToggleClicked = { /*onToggleClicked(entity)*/ },
                )
            }
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

@Composable
private fun DeviceCard(
        device: Device,
        onToggleClicked: () -> Unit,
) {
    Column(
            modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = DEVICE_CARD_ELEVATION, shape = DEVICE_CARD_SHAPE)
                    .background(color = AppTheme.colors.colorPrimary)
                    .padding(Dimension.mediumMargin),
    ) {
        Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
        ) {
            DeviceImage(device = device)
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                    imageVector = Icons.Outlined.PowerSettingsNew,
                    contentDescription = "Toggle",
                    tint = AppTheme.colors.colorAccent,
                    modifier = Modifier
                            .size(24.dp)
                            .clickable(onClick = onToggleClicked),
            )
        }

        Spacer(modifier = Modifier.height(Dimension.smallMargin))

        Text(
                text = device.name,
                style = AppTheme.typography.captionMedium12,
                fontWeight = FontWeight.Bold,
                color = AppTheme.colors.colorAccent,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
                text = device.area?.name.orEmpty(),
                style = AppTheme.typography.captionMedium12,
                color = AppTheme.colors.colorAccent,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun DeviceImage(device: Device) {
    val modifier = Modifier.size(DEVICE_IMAGE_SIZE)
    val imageUrl = device.imageUrl

    if (imageUrl == null) {
        Icon(
                imageVector = Icons.Outlined.HelpOutline,
                contentDescription = null,
                modifier = modifier,
                tint = AppTheme.colors.textPrimary,
        )
        return
    }

    val context = LocalContext.current
    val sizePx = with(LocalDensity.current) { DEVICE_IMAGE_SIZE.roundToPx() }
    val fallbackPainter = rememberVectorPainter(Icons.Outlined.HelpOutline)
    val painter = rememberAsyncImagePainter(
            model = remember(imageUrl, sizePx) {
                ImageRequest.Builder(context)
                        .data(imageUrl)
                        .size(sizePx)
                        .crossfade(false)
                        .build()
            },
            placeholder = fallbackPainter,
            error = fallbackPainter,
    )
    Image(
            painter = painter,
            contentDescription = null,
            modifier = modifier,
    )
}

private val DEVICE_IMAGE_SIZE = 60.dp
private val DEVICE_CARD_SHAPE = RoundedCornerShape(12.dp)
private val DEVICE_CARD_ELEVATION = 4.dp

// region Previews

private fun createPreviewController(): HomeController = object : HomeController {
    override fun onTestButtonClicked() = Unit
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun HomeScreenPreview(
        @PreviewParameter(ThemePreviewParameter::class) useDarkMode: Boolean,
) {
    AppTheme(useDarkMode) {
        HomeLayout(
                state = HomeState(deviceMap = previewDevices.groupBy { it.area?.name.orEmpty() }),
                controller = createPreviewController())
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenLoadingPreview(
        @PreviewParameter(ThemePreviewParameter::class) useDarkMode: Boolean,
) {
    AppTheme(useDarkMode) {
        HomeLayout(state = HomeState(progressState = ProgressState.Loading), controller = createPreviewController())
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

private fun previewArea(name: String): Area = Area(
        areaId = name.lowercase(),
        name = name,
        humidityEntityIid = null,
        temperatureEntityId = null,
        aliases = emptyList(),
)

private val previewDevices: List<Device> = listOf(
        Device(
                id = "4f745823d042948d34938086261e40d7",
                name = "Выключатель Зал/Кухня",
                model = "Wall switch with 2 buttons",
                modelId = "ZNCJMB14LM",
                manufacturer = "Aqara",
                area = previewArea("Зал"),
                entityList = listOf(
                        HomeAssistantEntity(
                                id = "switch.vykliuchatel_zal_kukhnia_1",
                                services = setOf("turn_on", "turn_off", "toggle"),
                        ),
                        HomeAssistantEntity(
                                id = "switch.vykliuchatel_zal_kukhnia_kukhnia",
                                services = setOf("turn_on", "turn_off", "toggle"),
                        ),
                ),
        ),
        Device(
                id = "416f948a315f700d4ef3ea300f698d1e",
                name = "Выключатель на балконе",
                model = "Smart wall switch",
                modelId = "QBKG11LM",
                manufacturer = "Aqara",
                area = previewArea("Балкон"),
                entityList = listOf(
                        HomeAssistantEntity(
                                id = "switch.0x603d61fffe758b32_1",
                                services = setOf("turn_on", "turn_off", "toggle"),
                        ),
                ),
        ),
        Device(
                id = "8a1f7d29a04a4b3eb6c9e8410c7a6b22",
                name = "Лампа в спальне",
                model = "RGBW light bulb",
                modelId = "LED1624G9",
                manufacturer = "IKEA",
                area = previewArea("Спальня"),
                entityList = listOf(
                        HomeAssistantEntity(
                                id = "light.bedroom_main",
                                services = setOf("turn_on", "turn_off", "toggle"),
                        ),
                ),
        ),
)

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun HomeScreenDevicesPreview(
        @PreviewParameter(ThemePreviewParameter::class) useDarkMode: Boolean,
) {
    AppTheme(useDarkMode) {
        HomeLayout(
                state = HomeState(deviceMap = previewDevices.groupBy { it.area?.name.orEmpty() }),
                controller = createPreviewController(),
        )
    }
}

@Preview(showBackground = true, widthDp = 140, heightDp = 140)
@Composable
private fun DeviceCardPreview(
        @PreviewParameter(ThemePreviewParameter::class) useDarkMode: Boolean,
) {
    AppTheme(useDarkMode) {
        DeviceCard(
                device = previewDevices.first(),
                onToggleClicked = {},
        )
    }
}

// endregion
