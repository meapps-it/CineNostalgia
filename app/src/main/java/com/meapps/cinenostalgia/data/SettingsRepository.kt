package com.meapps.cinenostalgia.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {
    private val fontScaleKey = floatPreferencesKey("font_scale")

    val fontScale: Flow<Float> = context.settingsDataStore.data.map { preferences ->
        (preferences[fontScaleKey] ?: 1f).coerceIn(MIN_FONT_SCALE, MAX_FONT_SCALE)
    }

    suspend fun setFontScale(value: Float) {
        context.settingsDataStore.edit { it[fontScaleKey] = value.coerceIn(MIN_FONT_SCALE, MAX_FONT_SCALE) }
    }

    companion object {
        const val MIN_FONT_SCALE = 0.80f
        const val MAX_FONT_SCALE = 1.40f
    }
}
