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

package org.m415x.materialcalc.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import org.jetbrains.compose.resources.StringResource

import materialscalculator.composeapp.generated.resources.Res
import materialscalculator.composeapp.generated.resources.*

/**
 * Enumeración que representa las pantallas de la aplicación.
 * Cada pantalla tiene un título y un indicador de si debe mostrar el botón de regreso.
 *
 * @property title Título de la pantalla.
 * @property showBackButton Indicador de si debe mostrar el botón de regreso.
 */
sealed class Screen(val title: StringResource, val showBackButton: Boolean) {
    data object Home : Screen(Res.string.home_title_main, false)
    data object Concrete : Screen(Res.string.home_category_concrete, true)
    data object Wall : Screen(Res.string.home_category_wall, true)
    data object Structure : Screen(Res.string.home_category_structure, true)
    data object Plaster : Screen(Res.string.home_category_plaster, true)

    data object Saved : Screen(Res.string.saved_title_main, false)
    data object Settings : Screen(Res.string.settings_title_main, false)
}

/**
 * Enumeración que representa las secciones del menú inferior.
 * Cada sección tiene un título, un icono y una pantalla asociada.
 *
 * @property title Título de la sección.
 * @property icon Icono de la sección.
 * @property screen Pantalla asociada a la sección.
 */
enum class BottomTab(
    val title: StringResource, // Cambiado de String a StringResource
    val icon: ImageVector,
    val screen: Screen // A qué pantalla nos lleva este tab
) {
    // Usamos los IDs de recursos directamente
    CALCULATOR(Res.string.nav_home, Icons.Default.Calculate, Screen.Home),
    SAVED(Res.string.nav_saved, Icons.Default.Save, Screen.Saved),
    SETTINGS(Res.string.nav_settings, Icons.Default.Settings, Screen.Settings)
}