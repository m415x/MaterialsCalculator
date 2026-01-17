/*
 * materialCalc
 * Copyright (C) 2026 M415X
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

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.russhwolf.settings.StorageSettings
import kotlinx.browser.document
import materialscalculator.composeapp.generated.resources.Res
import materialscalculator.composeapp.generated.resources.app_name
import org.jetbrains.compose.resources.stringResource
import org.m415x.materialcalc.data.ObservableStorageSettings
import org.m415x.materialcalc.data.repository.SettingsRepository

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    // 1. Configuramos Settings
    val settings = ObservableStorageSettings(StorageSettings())
    val repo = SettingsRepository(settings)

    // 2. Arrancamos la ventana
    ComposeViewport(document.body!!) {
        // Obtenemos el título desde los recursos compartidos
        val title = stringResource(Res.string.app_name)

        // Actualizamos el título del navegador cuando el recurso esté disponible
        LaunchedEffect(title) {
            document.title = title
        }

        App(settingsRepository = repo)
    }
}