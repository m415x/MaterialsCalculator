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

package org.m415x.materialcalc.ui.screen.settings

/**
 * Enumeración que define las secciones del menú de configuración.
 */
enum class SettingsSection {
    MENU,           // El menú principal de opciones
    APPEARANCE,     // Tema y Contraste
    GLOBAL_PARAMS,  // Bolsas, Baldes, Desperdicios
    MATERIALS_DB,   // Editor de Ladrillos y Hierros (Futuro)
    PRICES          // Precios (Futuro)
}