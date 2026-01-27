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

package org.m415x.materialcalc.domain.model

import org.m415x.materialcalc.ui.theme.ColorPalette
import org.m415x.materialcalc.ui.theme.ContrastMode
import org.m415x.materialcalc.ui.theme.ThemeMode

/**
 * Estado global de la configuración de la aplicación.
 * Agrupa todos los valores que se leen del repositorio para pasarlos a la UI.
 */
data class AppSettingsState(
    // Apariencia
    val themeMode: ThemeMode = ThemeMode.System,
    val contrastMode: ContrastMode = ContrastMode.Standard,
    val colorPalette: ColorPalette = ColorPalette.Default,
    val isOutdoorMode: Boolean = false,

    // Presentación
    val bagCementKg: Int = 25,
    val bagLimeKg: Int = 25,
    val bagPremixKg: Int = 25,

    // Volúmenes
    val bucketVolL: Double = 10.0,
    val barrowVolL: Double = 90.0,
    val mixerVolL: Double = 80.0,

    // Desperdicios (%)
    val wasteConcretePct: Double = 5.0,
    val wasteMortarPct: Double = 15.0,
    val wasteBrickPct: Double = 5.0,
    val wasteIronMainPct: Double = 10.0,
    val wasteIronStirrupPct: Double = 5.0,
    val wasteIronMeshPct: Double = 15.0,
    val wastePlasterPct: Double = 10.0,

    // Configuración Técnica
    val fineThicknessMm: Double = 3.0,
    val requestFocusOnStart: Boolean = false,

    // Valores por Defecto (IDs)
    val defaultBrickId: String = "LADRILLON",
    val defaultConcreteGenId: String = "H13",
    val defaultConcreteStrId: String = "H17",
    val defaultPlasterId: String = "STD_JAHARRO",

    // Listas Personalizadas
    val customBricks: List<CustomBrick> = emptyList(),
    val customIrons: List<CustomIron> = emptyList(),
    val customRecipes: List<CustomRecipe> = emptyList(),

    // Elementos Ocultos (IDs)
    val hiddenBrickIds: Set<String> = emptySet(),
    val hiddenIronIds: Set<String> = emptySet(),
    val hiddenRecipeIds: Set<String> = emptySet(),

    // Precios
    val priceSettings: PriceSettings = PriceSettings()
)
