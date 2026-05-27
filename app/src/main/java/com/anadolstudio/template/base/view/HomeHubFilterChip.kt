package com.anadolstudio.template.base.view

import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.SelectableChipElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.anadolstudio.compose.ui.theme.AppTheme
import com.anadolstudio.compose.ui.theme.Dimmens

/**
 * Стандартный для приложения FilterChip: цвета на основе [AppTheme.colors] и фиксированная
 * elevation из [Dimmens]. Используется в местах, где нужны выборные чипы (фильтры зон,
 * табы режимов и т.п.) — чтобы стилизация не дублировалась по экранам.
 */
@Composable
internal fun HomeHubFilterChip(
        selected: Boolean,
        onClick: () -> Unit,
        label: @Composable () -> Unit,
        modifier: Modifier = Modifier,
) {
    FilterChip(
            selected = selected,
            onClick = onClick,
            label = label,
            modifier = modifier,
            colors = FilterChipDefaults.filterChipColors(
                    containerColor = AppTheme.colors.colorPrimary,
                    labelColor = AppTheme.colors.colorAccent,
                    selectedContainerColor = AppTheme.colors.colorAccent,
                    selectedLabelColor = AppTheme.colors.colorPrimary,
            ),
            border = null,
            elevation = SelectableChipElevation(
                    elevation = Dimmens.baseElevation,
                    pressedElevation = Dimmens.pressedElevation,
                    focusedElevation = Dimmens.baseElevation,
                    hoveredElevation = Dimmens.baseElevation,
                    draggedElevation = Dimmens.baseElevation,
                    disabledElevation = 0.dp,
            ),
    )
}
