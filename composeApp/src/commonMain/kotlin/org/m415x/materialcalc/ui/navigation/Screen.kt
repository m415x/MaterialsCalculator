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
    data object Home : Screen(Res.string.title_home, false)
    data object Hormigon : Screen(Res.string.title_concrete, true)
    data object Muro : Screen(Res.string.title_wall, true)
    data object Estructura : Screen(Res.string.title_structure, true)
    data object Revoque : Screen(Res.string.title_plaster, true)

    data object Guardados : Screen(Res.string.title_saved, false)
    data object Configuracion : Screen(Res.string.title_settings, false)
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
    val title: String,
    val icon: ImageVector,
    val screen: Screen // A qué pantalla nos lleva este tab
) {
    CALCULATOR("Calcular", Icons.Default.Calculate, Screen.Home),
    SAVED("Guardados", Icons.Default.Save, Screen.Guardados),
    SETTINGS("Ajustes", Icons.Default.Settings, Screen.Configuracion)
}