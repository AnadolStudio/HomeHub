package com.anadolstudio.homehub.feature.common.data

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onStart

@Suppress("TooManyFunctions")
class PreferencesStorage(private val preferences: SharedPreferences) {

    private companion object {
        const val NIGHT_MODE = "NIGHT_MODE"
        const val BASE_URL = "BASE_URL"
        const val ACCESS_TOKEN = "ACCESS_TOKEN"
        const val REFRESH_TOKEN = "REFRESH_TOKEN"
        const val TOKEN_TYPE = "TOKEN_TYPE"
        const val TOKEN_EXPIRES_IN = "TOKEN_EXPIRES_IN"
        const val IS_REGISTER_USER_SHOWN = "IS_REGISTER_USER_SHOWN"
    }

    val isAuthenticatedFlow: Flow<Boolean> = preferences.observeKey(ACCESS_TOKEN) { accessToken != null }

    var nightMode: Int
        set(value) = preferences.modify { putInt(NIGHT_MODE, value) }
        get() = preferences.getInt(NIGHT_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)

    var baseUrl: String?
        set(value) = preferences.modify { putString(BASE_URL, value) }
        get() = preferences.getString(BASE_URL, null)

    var accessToken: String?
        set(value) = preferences.modify { putString(ACCESS_TOKEN, value) }
        get() = preferences.getString(ACCESS_TOKEN, null)

    var accessTokenExpiresIn: Long
        set(value) = preferences.modify { putLong(TOKEN_EXPIRES_IN, value) }
        get() = preferences.getLong(TOKEN_EXPIRES_IN, 0L)

    var refreshToken: String?
        set(value) = preferences.modify { putString(REFRESH_TOKEN, value) }
        get() = preferences.getString(REFRESH_TOKEN, null)

    var tokenType: String?
        set(value) = preferences.modify { putString(TOKEN_TYPE, value) }
        get() = preferences.getString(TOKEN_TYPE, null)

    var isRegisterUserShown: Boolean
        set(value) = preferences.modify { putBoolean(IS_REGISTER_USER_SHOWN, value) }
        get() = preferences.getBoolean(IS_REGISTER_USER_SHOWN, false)

    fun clearAuthData() {
        baseUrl = null
        accessToken = null
        refreshToken = null
        tokenType = null
        accessTokenExpiresIn = 0L
        isRegisterUserShown = false
    }

    private inline fun SharedPreferences.modify(action: SharedPreferences.Editor.() -> Unit) {
        with(edit()) {
            action(this)
            apply()
        }
    }

    private fun <T> SharedPreferences.observeKey(
            key: String,
            currentValue: () -> T,
    ): Flow<T> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, changedKey ->
            // changedKey == null означает clear() всей SharedPreferences — пушим текущее значение.
            if (changedKey == null || changedKey == key) {
                trySend(currentValue())
            }
        }
        registerOnSharedPreferenceChangeListener(listener)
        awaitClose { unregisterOnSharedPreferenceChangeListener(listener) }
    }
            .onStart { emit(currentValue()) }
            .distinctUntilChanged()
}
