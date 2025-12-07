package org.m415x.materialscalculator.data.repository

import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.coroutines.getStringFlow
import com.russhwolf.settings.set
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

import org.m415x.materialscalculator.ui.theme.ContrastMode
import org.m415x.materialscalculator.ui.theme.ThemeMode

/**
 * Repositorio de configuración que maneja la persistencia de preferencias del usuario.
 *
 * @property settings Instancia de ObservableSettings para la persistencia de datos.
 */
@OptIn(ExperimentalSettingsApi::class)
class SettingsRepository(private val settings: ObservableSettings) {
    /**
     * Claves para la persistencia de datos.
     *
     * @property THEME_KEY Clave para el modo de tema.
     * @property CONTRAST_KEY Clave para el modo de contraste.
     */
    private val THEME_KEY = "theme_mode"
    private val CONTRAST_KEY = "contrast_mode"

    /**
     * Flujos de configuración.
     *
     * @property themeMode Flujo de configuración del modo de tema.
     * @property contrastMode Flujo de configuración del modo de contraste.
     */
    val themeMode: Flow<ThemeMode> = settings.getStringFlow(THEME_KEY, ThemeMode.System.name)
        .map { name ->
            try { ThemeMode.valueOf(name) } catch (e: Exception) { ThemeMode.System }
        }

    val contrastMode: Flow<ContrastMode> = settings.getStringFlow(CONTRAST_KEY, ContrastMode.Standard.name)
        .map { name ->
            try { ContrastMode.valueOf(name) } catch (e: Exception) { ContrastMode.Standard }
        }

    /**
     * Funciones de escritura.
     *
     * @property saveThemeMode Guarda el modo de tema.
     * @property saveContrastMode Guarda el modo de contraste.
     */
    fun saveThemeMode(mode: ThemeMode) {
        settings[THEME_KEY] = mode.name
    }

    fun saveContrastMode(mode: ContrastMode) {
        settings[CONTRAST_KEY] = mode.name
    }
}