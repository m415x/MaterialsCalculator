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

/**
 * Enumeración que representa los tipos de revoque estándar.
 *
 * @property displayName Nombre amigable para mostrar en la UI.
 */
enum class PlasterType(val displayName: String) {
    STD_JAHARRO("Tradicional (Jaharro)"),
    // A futuro: IGNIFUGO, MONOCAPA, etc.
}

/**
 * Empaqueta los resultados de forma ordenada.
 *
 * @property totalAreaM2 Superficie total (x1 o x2 caras)
 * @property cementBagKg Cantidad de bolsas de cemento
 * @property limeBagKg Cantidad de bolsas de cal
 * @property premixBagKg Cantidad de bolsas de premezcla fina
 * @property thickVolumeM3 Volumen grueso en metros cúbicos
 * @property thickCementKg Cantidad de cemento grueso en kilogramos
 * @property thickLimeKg Cantidad de cal gruesa en kilogramos
 * @property thickSandKg Cantidad de arena gruesa en metros cúbicos
 * @property thickWaterLiters Cantidad de agua gruesa en litros
 * @property thickPercentageWaste Porcentaje de desperdicio grueso
 * @property thickDosage Proporcion grueso
 * @property finePremixKg Cantidad de premezcla fina en kilogramos
 * @property fineLimeKg Cantidad de cal fina en kilogramos
 * @property fineSandM3 Cantidad de arena fina en metros cúbicos
 * @property finePercentageWaste Porcentaje de desperdicio fino
 * @property fineDosage Proporcion fina
 */
data class PlasterResult(
    val totalAreaM2: Double,
    val cementBagKg: Int,
    val limeBagKg: Int,
    val premixBagKg: Int,
    // --- REVOQUE GRUESO (Jaharro) ---
    val thickVolumeM3: Double,
    val thickCementKg: Double,
    val thickLimeKg: Double,
    val thickSandKg: Double,
    val thickWaterLiters: Double,
    val thickPercentageWaste: Double,
    val thickDosage: String,
    // --- REVOQUE FINO (Enlucido) ---
    // Opción 1: Premezclado (Bolsa lista)
    val finePremixKg: Double,
    // Opción 2: Tradicional (A la cal)
    val fineLimeKg: Double,
    val fineSandM3: Double,
    val finePercentageWaste: Double,
    val fineDosage: String
)