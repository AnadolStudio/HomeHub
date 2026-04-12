package com.anadolstudio.compose.ui.view.checkbox

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.anadolstudio.compose.ui.theme.AppTypography
import com.anadolstudio.compose.ui.theme.Dimension
import com.anadolstudio.compose.ui.view.text.Text

@Composable
fun Checkbox(
    isEnabled: Boolean,
    text: String,
    onClick: () -> Unit,
    isEnabledIcon: Painter ,
    isDisabledIcon: Painter,
) {
    BaseCheckbox(
        isEnabled = isEnabled,
        text = {
            Text(
                text = text,
                style = AppTypography.captionBook16,
            )
        },
        onClick = onClick,
        isEnabledIcon = isEnabledIcon,
        isDisabledIcon = isDisabledIcon,
    )
}

@Composable
fun Checkbox(
    isEnabled: Boolean,
    text: AnnotatedString,
    onClick: () -> Unit,
    isEnabledIcon: Painter,
    isDisabledIcon: Painter,
) {
    BaseCheckbox(
        isEnabled = isEnabled,
        text = {
            Text(
                text = text,
                style = AppTypography.captionBook16,
            )
        },
        onClick = onClick,
        isEnabledIcon = isEnabledIcon,
        isDisabledIcon = isDisabledIcon,
    )
}

@Composable
private fun BaseCheckbox(
    isEnabled: Boolean,
    text: @Composable () -> Unit,
    isEnabledIcon: Painter,
    isDisabledIcon: Painter,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .clickable(
                onClick = onClick,
                interactionSource = interactionSource,
                indication = null,
            )
            .fillMaxWidth(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .padding(vertical = 12.dp, horizontal = Dimension.mainMargin)
        ) {
            val icon = if (isEnabled) isEnabledIcon else isDisabledIcon

            Image(
                painter = icon,
                contentDescription = null,
                modifier = Modifier.indication(
                    interactionSource = interactionSource,
                    indication = rememberRipple(bounded = false, radius = RippleRadius),
                )
            )

            text.invoke()
        }
    }
}

private val RippleRadius = 20.dp

