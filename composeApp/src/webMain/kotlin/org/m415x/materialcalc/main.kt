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

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.Settings
import org.m415x.materialcalc.data.repository.SettingsRepository

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    // 1. Creamos el objeto Settings estándar para Web
    // En JS, Settings() utiliza por defecto el localStorage del navegador
    val settings: Settings = Settings()

    // 2. IMPORTANTE: Tu repositorio espera un ObservableSettings.
    // Debemos asegurarnos de que lo sea. Si no lo es, se puede envolver,
    // pero la implementación estándar en JS suele ser compatible.
    val observableSettings = settings as ObservableSettings

    val repo = SettingsRepository(observableSettings)

    // 3. Configuramos el título del documento
    document.title = "Material Calculator"

    // 4. Arrancamos la ventana de Compose
    ComposeViewport(document.body!!) {
        App(settingsRepository = repo)
    }
}