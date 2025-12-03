package org.m415x.materialscalculator

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import org.m415x.materialscalculator.data.repository.SettingsRepository

@Composable
fun AndroidApp(settingsRepository: SettingsRepository) {
    App(settingsRepository) // Llama a la UI común
}
