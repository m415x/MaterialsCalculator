package org.m415x.materialscalculator.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

import org.m415x.materialscalculator.ui.theme.ContrastMode
import org.m415x.materialscalculator.ui.theme.ThemeMode

class SettingsRepository(private val dataStore: DataStore<Preferences>) {

    // Definimos las claves (Keys)
    private val THEME_KEY = stringPreferencesKey("theme_mode")
    private val CONTRAST_KEY = stringPreferencesKey("contrast_mode")

    // --- LECTURA (Flows) ---

    val themeMode: Flow<ThemeMode> = dataStore.data.map { preferences ->
        val savedName = preferences[THEME_KEY]
        // Convertimos String -> Enum de forma segura
        try {
            if (savedName != null) ThemeMode.valueOf(savedName) else ThemeMode.System
        } catch (e: IllegalArgumentException) {
            ThemeMode.System // Valor por defecto si falla
        }
    }

    val contrastMode: Flow<ContrastMode> = dataStore.data.map { preferences ->
        val savedName = preferences[CONTRAST_KEY]
        try {
            if (savedName != null) ContrastMode.valueOf(savedName) else ContrastMode.Standard
        } catch (e: IllegalArgumentException) {
            ContrastMode.Standard
        }
    }

    // --- ESCRITURA (Suspend Functions) ---

    suspend fun saveThemeMode(mode: ThemeMode) {
        dataStore.edit { preferences ->
            preferences[THEME_KEY] = mode.name // Guardamos el nombre "Light", "Dark", etc.
        }
    }

    suspend fun saveContrastMode(mode: ContrastMode) {
        dataStore.edit { preferences ->
            preferences[CONTRAST_KEY] = mode.name
        }
    }
}