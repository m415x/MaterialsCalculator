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

package org.m415x.materialcalc.domain.common

import org.m415x.materialcalc.domain.model.MaterialRecipe
import kotlin.math.ceil

/**
 * Clase auxiliar para devolver los resultados brutos calculados
 *
 * @property cementoKg Cantidad de cemento en Kg.
 * @property calKg Cantidad de cal en Kg.
 * @property arenaM3 Cantidad de arena en M3.
 * @property piedraM3 Cantidad de piedra en M3.
 * @property aguaLitros Cantidad de agua en litros.
 * @property cementoBolsas Cantidad de bolsas de cemento.
 * @property calBolsas Cantidad de bolsas de cal.
 */
data class MaterialQuantities(
    val cementoKg: Double,
    val calKg: Double,
    val arenaM3: Double,
    val piedraM3: Double,
    val aguaLitros: Double,
    val cementoBolsas: Int,
    val calBolsas: Int
)

/**
 * Función pura que calcula materiales base.
 *
 * @param volumenM3 Volumen geométrico real.
 * @param receta La dosificación a usar (Hormigón o Mortero).
 * @param desperdicio Porcentaje extra (ej: 0.10 para 10%).
 * @param pesoBolsaCemento Peso de una bolsa de cemento.
 * @param pesoBolsaCal Peso de una bolsa de cal.
 *
 * @return MaterialQuantities
 */
fun calculateWetMaterials(
    volumenM3: Double,
    receta: MaterialRecipe,
    desperdicio: Double,
    pesoBolsaCemento: Int,
    pesoBolsaCal: Int
): MaterialQuantities {

    // 1. Aplicamos desperdicio al volumen
    val volumenReal = volumenM3 * (1.0 + desperdicio)

    // 2. Calculamos brutos
    val cemKg = volumenReal * receta.cementoKg
    val calKg = volumenReal * receta.calKg
    val arena = volumenReal * receta.arenaM3
    val piedra = volumenReal * receta.piedraM3

    // El agua suele calcularse sobre el cemento (Relación A/C)
    // O sobre el total de secos, depende tu fórmula original.
    // Usaremos la lógica de tu código: KgCemento * Relacion
    val agua = cemKg * receta.relacionAgua

    return MaterialQuantities(
        cementoKg = cemKg,
        calKg = calKg,
        arenaM3 = arena,
        piedraM3 = piedra,
        aguaLitros = agua,
        cementoBolsas = ceil(cemKg / pesoBolsaCemento).toInt(),
        calBolsas = ceil(calKg / pesoBolsaCal).toInt()
    )
}