package com.anadolstudio.template.feature.main

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.FragmentActivity
import com.anadolstudio.compose.ui.theme.AppTheme
import com.anadolstudio.compose.ui.theme.AppTypography
import com.anadolstudio.compose.ui.theme.AppTypography.Companion.DEFAULT_STYLE
import com.anadolstudio.template.di.DI
import com.anadolstudio.template.feature.common.ui.HomeHubFontFamily
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.google.accompanist.systemuicontroller.rememberSystemUiController

class SingleActivity : FragmentActivity() {

    private val viewModel: ActivityMainViewModel by viewModels { DI.viewModelsInjector.viewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            AppEntryPoint()
        }
    }

    private fun applyStatusBarColor(useDarkTheme: Boolean) {
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = !useDarkTheme
    }
}

@OptIn(ExperimentalMaterialNavigationApi::class)
@Composable
internal fun AppEntryPoint() {
    val context = LocalContext.current

    val systemUiController = rememberSystemUiController()
    val navigator = rememberNavigationController()

    val useDarkTheme = isSystemInDarkTheme()
    AppTheme(
            useDarkTheme = useDarkTheme,
            typography = AppTypography(
                    defaultStyle = DEFAULT_STYLE.copy(fontFamily = HomeHubFontFamily),
                            mediumWeight = FontWeight.Normal,
                            lightWeight = FontWeight.Bold,
            )
    ) {
        val useDarkIcons = !useDarkTheme
        systemUiController.setStatusBarColor(color = Color.Transparent, darkIcons = useDarkIcons)
        systemUiController.setNavigationBarColor(
                color = Color.Transparent,
                darkIcons = useDarkIcons,
                navigationBarContrastEnforced = false,
        )

        // todo Yes we have read text of deprecation and LocalImageLoader is exactly what we need.
        CompositionLocalProvider(
            content = { MainScreen(navigator) },
        )
    }
}
