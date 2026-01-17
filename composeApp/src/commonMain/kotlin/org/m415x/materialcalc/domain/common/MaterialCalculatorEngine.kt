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

package org.m415x.materialcalc.domain.common

import org.m415x.materialcalc.domain.model.WetMixRecipe
import kotlin.math.ceil

/**
 * Clase auxiliar para devolver los resultados brutos calculados
 *
 * @property cementKg Cantidad de cemento en Kg.
 * @property limeKg Cantidad de cal en Kg.
 * @property sandM3 Cantidad de arena en M3.
 * @property gravelM3 Cantidad de piedra en M3.
 * @property waterLiters Cantidad de agua en litros.
 * @property cementBags Cantidad de bolsas de cemento.
 * @property limeBags Cantidad de bolsas de cal.
 */
data class MaterialQuantities(
    val cementKg: Double,
    val limeKg: Double,
    val sandM3: Double,
    val gravelM3: Double,
    val waterLiters: Double,
    val cementBags: Int,
    val limeBags: Int
)

/**
 * Función pura que calcula materiales húmedos base.
 *
 * @param volumeM3 Volumen geométrico real.
 * @param recipe La dosificación a usar (Hormigón o Mortero).
 * @param waste Porcentaje extra (ej: 0.10 para 10%).
 * @param cementBagWeight Peso de una bolsa de cemento.
 * @param limeBagWeight Peso de una bolsa de cal.
 *
 * @return MaterialQuantities con los resultados.
 */
fun calculateWetMaterials(
    volumeM3: Double,
    recipe: WetMixRecipe,
    waste: Double,
    cementBagWeight: Int,
    limeBagWeight: Int
): MaterialQuantities {

    // 1. Aplicamos desperdicio al volumen
    val actualVolume = volumeM3 * (1.0 + waste)

    // 2. Calculamos brutos
    val cementKg = actualVolume * recipe.cementKg
    val limeKg = actualVolume * recipe.limeKg
    val sandM3 = actualVolume * recipe.sandM3
    val gravelM3 = actualVolume * recipe.gravelM3

    // El agua se calcula de manera diferente si hay cal o no.
    // Si waterCementRatio es 0.0, asumimos que waterLiters es el valor total por m3.
    // Si waterCementRatio > 0.0, calculamos el agua en base al cemento (Hormigón).
    val water = if (recipe.waterCementRatio > 0.0) {
        cementKg * recipe.waterCementRatio
    } else {
        actualVolume * recipe.waterLiters
    }

    return MaterialQuantities(
        cementKg = cementKg,
        limeKg = limeKg,
        sandM3 = sandM3,
        gravelM3 = gravelM3,
        waterLiters = water,
        cementBags = ceil(cementKg / cementBagWeight).toInt(),
        limeBags = ceil(limeKg / limeBagWeight).toInt()
    )
}