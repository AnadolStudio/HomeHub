package com.anadolstudio.homehub.feature.home.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BrokenImage
import androidx.compose.material.icons.outlined.DeviceUnknown
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.anadolstudio.compose.ui.theme.AppTheme
import com.anadolstudio.homehub.feature.home.domain.model.DeviceImage
import com.anadolstudio.homehub.util.toPainter

internal val DEVICE_IMAGE_VIEW_DEFAULT_SIZE = 80.dp

@Composable
internal fun DeviceImageView(
        image: DeviceImage?,
        modifier: Modifier = Modifier,
        imageSize: Dp = DEVICE_IMAGE_VIEW_DEFAULT_SIZE,
) {
    val defaultIcon = Icons.Outlined.DeviceUnknown

    when (image) {
        is DeviceImage.HaIconType -> {
            Icon(
                    painter = image.haIcon.toPainter(),
                    contentDescription = null,
                    modifier = modifier,
                    tint = AppTheme.colors.colorAccent,
            )
        }

        is DeviceImage.ImageUrlType -> {
            val context = LocalContext.current
            val sizePx = with(LocalDensity.current) { imageSize.roundToPx() }
            val fallbackPainter = rememberVectorPainter(defaultIcon)
            val painter = rememberAsyncImagePainter(
                    model = remember(image.url, sizePx) {
                        ImageRequest.Builder(context)
                                .data(image.url)
                                .size(sizePx)
                                .crossfade(false)
                                .build()
                    },
                    placeholder = fallbackPainter,
                    error = rememberVectorPainter(Icons.Outlined.BrokenImage),
            )

            val fallbackTint = AppTheme.colors.colorAccent
            val colorFilter by remember(painter, fallbackTint) {
                derivedStateOf {
                    when (painter.state) {
                        is AsyncImagePainter.State.Empty,
                        is AsyncImagePainter.State.Loading,
                        is AsyncImagePainter.State.Error -> ColorFilter.tint(fallbackTint)

                        is AsyncImagePainter.State.Success -> null
                    }
                }
            }

            Image(
                    painter = painter,
                    contentDescription = null,
                    modifier = modifier,
                    contentScale = ContentScale.Fit,
                    colorFilter = colorFilter,
            )
        }

        else -> Icon(
                imageVector = defaultIcon,
                contentDescription = null,
                modifier = modifier,
                tint = AppTheme.colors.colorAccent,
        )
    }
}
