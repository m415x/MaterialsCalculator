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

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import com.russhwolf.settings.SharedPreferencesSettings

import org.m415x.materialcalc.data.repository.SettingsRepository
import org.m415x.materialcalc.ui.common.ActivityProvider
import org.m415x.materialcalc.ui.common.AndroidContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidContext.context = applicationContext

        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // REGISTRAR ACTIVIDAD
        ActivityProvider.currentActivity = java.lang.ref.WeakReference(this)

        // 1. Creamos las preferencias clásicas de Android
        val sharedPrefs = getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        // 2. Las envolvemos en la librería Multiplatform
        val settings = SharedPreferencesSettings(sharedPrefs)
        // 3. Creamos el repo
        val repo = SettingsRepository(settings)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            // Pasar el repo a la app
            AndroidApp(settingsRepository = repo)
        }
    }
}
