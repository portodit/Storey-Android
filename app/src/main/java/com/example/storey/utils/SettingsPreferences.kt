package com.example.storey.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = SettingsPreferences.PREFERENCES_NAME)

class SettingsPreferences private constructor(private val dataStore: DataStore<Preferences>) {
    fun getToken() =
        dataStore.data.map {
            it[stringPreferencesKey(TOKEN_PREFERENCES)] ?: PREFERENCE_DEFAULT_VALUE
        }

    suspend fun saveToken(
        token: String
    ) {
        dataStore.edit { prefs ->
            prefs[stringPreferencesKey(TOKEN_PREFERENCES)] = token
        }
    }

    suspend fun clearPreferences() {
        dataStore.edit { prefs ->
            prefs.clear()
        }
    }

    companion object {
        private const val TOKEN_PREFERENCES = "token_preferences"
        const val PREFERENCES_NAME = "settings_preferences"
        const val PREFERENCE_DEFAULT_VALUE = "preference_default_value"

        @Volatile
        private var INSTANCE: SettingsPreferences? = null

        fun getInstance(dataStore: DataStore<Preferences>) = INSTANCE ?: synchronized(this) {
            val instance = SettingsPreferences(dataStore)
            INSTANCE = instance
            instance
        }
    }
}