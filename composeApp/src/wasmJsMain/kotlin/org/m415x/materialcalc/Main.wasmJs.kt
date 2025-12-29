/*
 * materialCalc
 * Copyright (C) 2025 M415X
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.m415x.materialcalc

import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.russhwolf.settings.Settings
import com.russhwolf.settings.ObservableSettings
import org.m415x.materialcalc.data.repository.SettingsRepository

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    /*
    // 1. Usamos la fábrica Settings() y forzamos el cast a ObservableSettings.
    // En Wasm, esto usará una implementación interna compatible.
    val settings: ObservableSettings = Settings() as ObservableSettings

    val repo = SettingsRepository(settings)

    // 2. Iniciamos la interfaz en el canvas del index.html
    val content: @Composable () -> Unit = {
        App(settingsRepository = repo)
    }

    ComposeViewport(
        viewportContainerId = "ComposeVisualizer",
        content = content
    )

     */
}