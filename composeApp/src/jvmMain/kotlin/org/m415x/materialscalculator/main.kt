package org.m415x.materialscalculator

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.russhwolf.settings.PreferencesSettings
import org.m415x.materialscalculator.data.repository.SettingsRepository
import java.util.prefs.Preferences

fun main() = application {
    // 1. Creamos settings basados en Preferencias de Java
    val preferences = Preferences.userRoot().node("material_calculator")
    val settings = PreferencesSettings(preferences)
    val repo = SettingsRepository(settings)

    Window(onCloseRequest = ::exitApplication, title = "Material Calculator") {
        // 2. Pasamos el repo
        App(settingsRepository = repo)
    }
}