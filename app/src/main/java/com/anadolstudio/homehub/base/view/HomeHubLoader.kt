package com.anadolstudio.homehub.base.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.anadolstudio.compose.ui.theme.AppTheme
import com.anadolstudio.compose.ui.view.state.Loader

@Composable
internal fun HomeHubLoader(
        modifier: Modifier = Modifier,
        underLoadingView: @Composable () -> Unit = {}
) {
    Column(modifier = modifier) {
        Loader(
                modifier = Modifier
                        .padding(vertical = 24.dp)
                        .align(Alignment.CenterHorizontally),
                color = AppTheme.colors.colorAccent
        )

        underLoadingView.invoke()
    }
}
