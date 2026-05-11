package com.anadolstudio.template.feature.home.presentation

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.PowerSettingsNew
import androidx.compose.material.icons.outlined.ToggleOn
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.anadolstudio.compose.ui.theme.AppTheme
import com.anadolstudio.compose.ui.theme.Dimension
import com.anadolstudio.compose.ui.theme.preview.ThemePreviewParameter
import com.anadolstudio.compose.ui.view.button.FloatTextButton
import com.anadolstudio.compose.ui.view.snackbar.SnackbarHostState
import com.anadolstudio.template.base.view.HomeHubLoader
import com.anadolstudio.template.base.viewmodel.ObserveViewModelLifecycle
import com.anadolstudio.template.di.viewmodel.daggerViewModel
import com.anadolstudio.template.event.ObserveEvents
import com.anadolstudio.template.feature.home.data.model.EntityRegistryEntry
import com.anadolstudio.template.feature.home.data.model.HomeAssistantDevice
import com.anadolstudio.template.feature.home.data.model.HomeAssistantEntity
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
    val groups = remember(state.allEntities) { state.allEntities.toGroups() }

    LazyVerticalGrid(
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
        item(span = { GridItemSpan(maxLineSpan) }) {
            Text(
                    text = HOME_TITLE,
                    style = AppTheme.typography.textBook18,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.colorAccent,
                    modifier = Modifier.fillMaxWidth(),
            )
        }

        item {
            FloatTextButton(
                    text = "Test Button",
                    onClick = { controller.onTestButtonClicked() },
            )
        }


        if (state.progressState is ProgressState.Loading && state.allEntities.isEmpty()) {
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

        if (state.progressState is ProgressState.Error) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(
                        text = "Error: ${(state.progressState as ProgressState.Error).error?.message}",
                        color = AppTheme.colors.textSecondary,
                        style = AppTheme.typography.textBook14,
                )
            }
        }

        groups.forEach { group ->
            item(span = { GridItemSpan(maxLineSpan) }, key = "header_${group.id}") {
                GroupHeader(title = group.title, onClick = {})
            }

            items(group.entities, key = { entity -> "${group.id}/${entity.entityId}" }) { entity ->
                EntityCard(
                        entity = entity,
                        onToggleClicked = { },
                )
            }
        }

        state.devices.forEach { device ->
            item(span = { GridItemSpan(maxLineSpan) }, key = "device_${device.id}") {
                GroupHeader(title = device.name ?: device.id, onClick = {})
            }

            items(device.list, key = { entity -> "${device.id}/${entity.id}" }) { entity ->
                SwitchEntityCard(
                        entity = entity,
                        onToggleClicked = { /*onToggleClicked(entity)*/ },
                )
            }
        }
    }
}

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
        IconButton(onClick = onClick, modifier = Modifier.size(20.dp)) {
            Icon(
                    imageVector = Icons.Outlined.ChevronRight,
                    contentDescription = null,
                    tint = AppTheme.colors.textSecondary,
            )
        }
    }
}

@Composable
private fun EntityCard(
        entity: EntityRegistryEntry,
        onToggleClicked: () -> Unit,
) {
    Card(
            modifier = Modifier
                    .fillMaxWidth(),
    ) {
        Column(
                modifier = Modifier
                        .background(color = AppTheme.colors.colorPrimary)
                        .padding(Dimension.mediumMargin)
        ) {
            Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
            ) {
                Icon(
                        imageVector = entity.domainIcon(),
                        contentDescription = null,
                        modifier = Modifier.size(60.dp),
                        tint = AppTheme.colors.textPrimary,
                )
                Spacer(modifier = Modifier.weight(1f))
                IconButton(
                        onClick = onToggleClicked,
                        modifier = Modifier.size(24.dp),
                ) {
                    Icon(
                            imageVector = Icons.Outlined.PowerSettingsNew,
                            contentDescription = "Toggle",
                            tint = AppTheme.colors.colorAccent,
                    )
                }
            }

            Spacer(modifier = Modifier.height(Dimension.smallMargin))

            Text(
                    text = entity.displayName,
                    style = AppTheme.typography.captionMedium12,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.colorAccent,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                    text = entity.entityId,
                    style = AppTheme.typography.captionMedium12,
                    color = AppTheme.colors.colorAccent,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

private fun EntityRegistryEntry.domainIcon(): ImageVector = when (domain) {
    "light" -> Icons.Outlined.Lightbulb
    "switch", "input_boolean" -> Icons.Outlined.ToggleOn
    else -> Icons.Outlined.PowerSettingsNew
}

@Composable
private fun SwitchEntityCard(
        entity: HomeAssistantEntity,
        onToggleClicked: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
                modifier = Modifier
                        .background(color = AppTheme.colors.colorPrimary)
                        .padding(Dimension.mediumMargin),
        ) {
            Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
            ) {
                Icon(
                        imageVector = Icons.Outlined.ToggleOn,
                        contentDescription = null,
                        modifier = Modifier.size(60.dp),
                        tint = AppTheme.colors.textPrimary,
                )
                Spacer(modifier = Modifier.weight(1f))
                IconButton(
                        onClick = onToggleClicked,
                        modifier = Modifier.size(24.dp),
                ) {
                    Icon(
                            imageVector = Icons.Outlined.PowerSettingsNew,
                            contentDescription = "Toggle",
                            tint = AppTheme.colors.colorAccent,
                    )
                }
            }

            Spacer(modifier = Modifier.height(Dimension.smallMargin))

            Text(
                    text = entity.id,
                    style = AppTheme.typography.captionMedium12,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.colorAccent,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                    text = entity.services.joinToString(", "),
                    style = AppTheme.typography.captionMedium12,
                    color = AppTheme.colors.colorAccent,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

private data class EntityGroupUi(
        val id: String,
        val title: String,
        val entities: List<EntityRegistryEntry>,
)

private fun List<EntityRegistryEntry>.toGroups(): List<EntityGroupUi> =
        groupBy { it.areaId ?: NO_AREA_ID }
                .map { (areaId, entities) ->
                    EntityGroupUi(
                            id = areaId,
                            title = if (areaId == NO_AREA_ID) NO_AREA_TITLE else areaId,
                            entities = entities.sortedBy { it.displayName },
                    )
                }
                .sortedBy { it.title }

// region Previews

private fun previewEntity(
        entityId: String,
        name: String?,
        areaId: String?,
): EntityRegistryEntry = EntityRegistryEntry(
        entityId = entityId,
        platform = "mqtt",
        name = name,
        areaId = areaId,
)

private val previewEntities: List<EntityRegistryEntry> = listOf(
        previewEntity("switch.zal_main", "Выключатель", "Зал"),
        previewEntity("switch.zal_long", "Выключатель на длинном тексте", "Зал"),
        previewEntity("switch.zal_extra", "Выключатель", "Зал"),
        previewEntity("switch.kuhnia_1", "Выключатель", "Кухня"),
        previewEntity("switch.kuhnia_2", "Выключатель на длинном тексте", "Кухня"),
        previewEntity("switch.kuhnia_3", "Выключатель", "Кухня"),
        previewEntity("light.bedroom", "Лампа", "Спальня"),
)

private fun createPreviewController(): HomeController = object : HomeController {
    override fun onTestButtonClicked() = Unit
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun HomeScreenPreview(
        @PreviewParameter(ThemePreviewParameter::class) useDarkMode: Boolean,
) {
    AppTheme(useDarkMode) {
        HomeLayout(state = HomeState(allEntities = previewEntities), controller = createPreviewController())
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

@Preview(showBackground = true, widthDp = 140, heightDp = 140)
@Composable
private fun EntityCardPreview(
        @PreviewParameter(ThemePreviewParameter::class) useDarkMode: Boolean,
) {
    AppTheme(useDarkMode) {
        EntityCard(
                entity = previewEntity("switch.zal_long", "Выключатель на длинном тексте", "Зал"),
                onToggleClicked = {},
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

private val previewDevices: List<HomeAssistantDevice> = listOf(
        HomeAssistantDevice(
                id = "4f745823d042948d34938086261e40d7",
                name = "Выключатель Зал/Кухня",
                list = listOf(
                        HomeAssistantEntity(
                                id = "switch.vykliuchatel_zal_kukhnia_1",
                                domain = "switch",
                                services = setOf("turn_on", "turn_off", "toggle"),
                        ),
                        HomeAssistantEntity(
                                id = "switch.vykliuchatel_zal_kukhnia_kukhnia",
                                domain = "switch",
                                services = setOf("turn_on", "turn_off", "toggle"),
                        ),
                ),
        ),
        HomeAssistantDevice(
                id = "416f948a315f700d4ef3ea300f698d1e",
                name = "Выключатель на балконе",
                list = listOf(
                        HomeAssistantEntity(
                                id = "switch.0x603d61fffe758b32_1",
                                domain = "switch",
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
        HomeLayout(state = HomeState(devices = previewDevices), controller = createPreviewController())
    }
}

@Preview(showBackground = true, widthDp = 140, heightDp = 140)
@Composable
private fun SwitchEntityCardPreview(
        @PreviewParameter(ThemePreviewParameter::class) useDarkMode: Boolean,
) {
    AppTheme(useDarkMode) {
        SwitchEntityCard(
                entity = HomeAssistantEntity(
                        id = "switch.vykliuchatel_zal_kukhnia_1",
                        domain = "switch",
                        services = setOf("turn_on", "turn_off", "toggle"),
                ),
                onToggleClicked = {},
        )
    }
}

// endregion
