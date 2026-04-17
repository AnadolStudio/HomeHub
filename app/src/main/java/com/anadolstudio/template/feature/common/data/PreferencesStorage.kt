package com.anadolstudio.template.feature.common.data

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate

@Suppress("TooManyFunctions")
class PreferencesStorage(private val preferences: SharedPreferences) {

    private companion object {
        const val NIGHT_MODE = "NIGHT_MODE"
        const val BASE_URL = "BASE_URL"
        const val ACCESS_TOKEN = "ACCESS_TOKEN"
        const val REFRESH_TOKEN = "REFRESH_TOKEN"
        const val TOKEN_TYPE = "TOKEN_TYPE"
        const val TOKEN_EXPIRES_IN = "TOKEN_EXPIRES_IN"
    }

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


    private inline fun SharedPreferences.modify(action: SharedPreferences.Editor.() -> Unit) {
        with(edit()) {
            action(this)
            apply()
        }
    }
}
