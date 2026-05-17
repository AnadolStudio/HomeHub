package com.anadolstudio.template.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.navigation.NavBackStackEntry
import com.anadolstudio.template.feature.main.NavigationController

/** Observes screen result. Designed to be used with `navigateUp` with result.
 * @param onValue Using method reference
 *
 * IMPORTANT: подписываемся на [NavigationController.currentBackStackEntryFlow], а не читаем
 * [NavigationController.currentBackStackEntry] напрямую — без подписки composable не пересоставится
 * при смене топа стека (bottomSheet закрылся → топ стал родительской entry → нужно
 * пересоздать observeAsState на правильную savedStateHandle). Иначе наблюдение остаётся
 * привязанным к handle того entry, который был топом в момент последней композиции,
 * и результат, записанный родителю на dispose, не подхватывается до следующего user-action.
 */
@Composable
internal fun <T : Any> ObserveResultValue(navigator: NavigationController, key: String, onValue: (T) -> Unit) {
    val currentEntry: NavBackStackEntry? by navigator
            .currentBackStackEntryFlow
            .collectAsState(initial = navigator.currentBackStackEntry)
    val savedStateHandle = currentEntry?.savedStateHandle ?: return
    val result by savedStateHandle.getLiveData<T?>(key).observeAsState()

    LaunchedEffect(result, onValue) {
        val value = savedStateHandle.get<T?>(key)
        if (value != null) {
            savedStateHandle[key] = null
            onValue(value)
        }
    }
}
