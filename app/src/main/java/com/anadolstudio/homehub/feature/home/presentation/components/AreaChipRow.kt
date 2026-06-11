package com.anadolstudio.homehub.feature.home.presentation.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.anadolstudio.compose.ui.theme.AppTheme
import com.anadolstudio.compose.ui.theme.preview.ThemePreviewParameter
import com.anadolstudio.homehub.R
import com.anadolstudio.homehub.base.view.HomeHubFilterChip
import com.anadolstudio.homehub.feature.home.domain.model.Area
import com.anadolstudio.homehub.feature.home.presentation.PreviewUtils

@Composable
internal fun AreaChipRow(
        selectedAreaId: String?,
        areas: List<Area>,
        onAreaSelected: (Area?) -> Unit,
        modifier: Modifier = Modifier,
        contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp),
        title: String? = stringResource(R.string.device_detail_field_area),
) {
    val items = listOf<Area?>(null) + areas
    Column(
            modifier = modifier
    ) {

        title?.let {
            Text(
                    text = it,
                    style = AppTheme.typography.captionMedium16,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.colorAccent,
                    modifier = Modifier
                            .padding(contentPadding),
            )
        }

        Row(
                modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(contentPadding),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items.forEach { area ->
                AreaChip(
                        area = area,
                        selected = area?.areaId == selectedAreaId,
                        onAreaSelected = { onAreaSelected(it) }
                )
            }
        }
    }
}

@Composable
private fun AreaChip(
        area: Area?,
        selected: Boolean,
        onAreaSelected: (Area?) -> Unit,
        defaultName: String = stringResource(R.string.area_filter_all),
) {
    AnimatedContent(
            targetState = selected,
            contentAlignment = Alignment.Center,
            label = area?.name.orEmpty(),
            transitionSpec = { fadeIn().togetherWith(fadeOut()) }
    ) { targetValue ->
        HomeHubFilterChip(
                selected = targetValue,
                onClick = { onAreaSelected(area) },
                label = { Text(text = area?.name ?: defaultName) },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AreaChipRowPreview(
        @PreviewParameter(ThemePreviewParameter::class) useDarkMode: Boolean,
) {
    AppTheme(useDarkMode) {
        Box(modifier = Modifier.background(color = AppTheme.colors.colorSecondary)) {
            AreaChipRow(
                    selectedAreaId = null,
                    areas = listOf(
                            PreviewUtils.previewArea("Зал"),
                            PreviewUtils.previewArea("Спальня"),
                            PreviewUtils.previewArea("Балкон"),
                    ),
                    onAreaSelected = {},
            )
        }
    }
}
