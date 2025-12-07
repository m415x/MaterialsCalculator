package org.m415x.materialscalculator

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.*
import com.russhwolf.settings.StorageSettings
import org.m415x.materialscalculator.data.repository.SettingsRepository

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    // 1. Configuramos Settings (Quitamos el tipo explícito para dejar que infiera)
    val settings = StorageSettings()
    val repo = SettingsRepository(settings)

    // 2. Arrancamos la ventana
    CanvasBasedWindow(title = "Material Calculator") {
        App(settingsRepository = repo)
    }
}