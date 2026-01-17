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

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.russhwolf.settings.PreferencesSettings
import org.m415x.materialcalc.data.repository.SettingsRepository
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