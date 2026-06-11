package com.anadolstudio.homehub.feature.home.presentation.components

import androidx.annotation.StringRes
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.anadolstudio.compose.ui.theme.AppTheme
import com.anadolstudio.compose.ui.theme.preview.ThemePreviewParameter
import com.anadolstudio.homehub.R
import com.anadolstudio.homehub.base.view.HomeHubFilterChip
import com.anadolstudio.homehub.feature.home.domain.model.AllowedDomain

@Composable
internal fun DomainChipRow(
        selectedDomain: AllowedDomain?,
        domains: List<AllowedDomain>,
        onDomainSelected: (AllowedDomain?) -> Unit,
        modifier: Modifier = Modifier,
        contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp),
        title: String? = stringResource(R.string.domain_filter_title),
) {
    val context = LocalContext.current
    val sortedDomains = remember(domains, context) {
        domains.sortedBy { context.getString(it.titleRes()) }
    }
    val items = listOf<AllowedDomain?>(null) + sortedDomains
    Column(
            modifier = modifier,
    ) {

        title?.let {
            Text(
                    text = it,
                    style = AppTheme.typography.captionMedium16,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.colorAccent,
                    modifier = Modifier.padding(contentPadding),
            )
        }

        Row(
                modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(contentPadding),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items.forEach { domain ->
                DomainChip(
                        domain = domain,
                        selected = domain == selectedDomain,
                        onDomainSelected = { onDomainSelected(it) }
                )
            }
        }
    }
}

@Composable
private fun DomainChip(
        domain: AllowedDomain?,
        selected: Boolean,
        onDomainSelected: (AllowedDomain?) -> Unit,
        defaultName: String = stringResource(R.string.area_filter_all),
) {
    val label = domain?.titleRes()?.let { stringResource(it) } ?: defaultName
    AnimatedContent(
            targetState = selected,
            contentAlignment = Alignment.Center,
            label = label,
            transitionSpec = { fadeIn().togetherWith(fadeOut()) }
    ) { targetValue ->
        HomeHubFilterChip(
                selected = targetValue,
                onClick = { onDomainSelected(domain) },
                label = { Text(text = label) },
        )
    }
}

@StringRes
internal fun AllowedDomain.titleRes(): Int = when (this) {
    AllowedDomain.SENSOR -> R.string.domain_sensor
    AllowedDomain.BINARY_SENSOR -> R.string.domain_binary_sensor
    AllowedDomain.SWITCH -> R.string.domain_switch
    AllowedDomain.LIGHT -> R.string.domain_light
    AllowedDomain.CLIMATE -> R.string.domain_climate
    AllowedDomain.ZONE_HOME -> R.string.domain_zone_home
    AllowedDomain.SELECT -> R.string.domain_select
    AllowedDomain.NUMBER -> R.string.domain_number
    AllowedDomain.PERSON -> R.string.domain_person
    AllowedDomain.AUTOMATION -> R.string.domain_automation
    AllowedDomain.SCENE -> R.string.domain_scene
    AllowedDomain.WEATHER -> R.string.domain_weather
    AllowedDomain.BUTTON -> R.string.domain_button
}

@Preview(showBackground = true)
@Composable
private fun DomainChipRowPreview(
        @PreviewParameter(ThemePreviewParameter::class) useDarkMode: Boolean,
) {
    AppTheme(useDarkMode) {
        Box(modifier = Modifier.background(color = AppTheme.colors.colorSecondary)) {
            DomainChipRow(
                    selectedDomain = null,
                    domains = listOf(
                            AllowedDomain.LIGHT,
                            AllowedDomain.SWITCH,
                            AllowedDomain.CLIMATE,
                    ),
                    onDomainSelected = {},
            )
        }
    }
}
