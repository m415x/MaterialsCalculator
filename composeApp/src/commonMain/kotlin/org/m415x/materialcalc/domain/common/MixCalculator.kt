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

// Helper object para calcular proporciones
object MixCalculator {
    // Densidades aproximadas (kg/m3)
    const val DENSIDAD_CEMENTO_APARENTE = 1400.0
    const val DENSIDAD_CAL_APARENTE = 600.0

    // Coeficientes de Aporte (Volumen Real / Volumen Aparente)
    // Fuente: Chandias / Manuales de Construcción
    const val COEF_CEMENTO = 0.47
    const val COEF_CAL = 0.37
    const val COEF_ARENA = 0.63
    const val COEF_PIEDRA = 0.51
    const val COEF_AGUA = 1.0

    data class ResultadoProporcion(
        val cementoKg: Double,
        val calKg: Double,
        val arenaM3: Double,
        val piedraM3: Double,
        val aguaLitros: Double
    )

    data class RecetaHormigonera(
        val baldesArena: Double,
        val baldesPiedra: Double,
        val baldesAgua: Double,
        val capacidadBaldeL: Double
    )

    fun calculateByParts(
        partesCemento: Double,
        partesCal: Double,
        partesArena: Double,
        partesPiedra: Double,
        relacionAguaCemento: Double
    ): ResultadoProporcion {

        // El agua se calcula en base al cemento (es lo más real en obra)
        val partesAgua = partesCemento * relacionAguaCemento

        // 1. Calcular el Volumen Real que genera esa suma de partes
        val volumenReal = (partesCemento * COEF_CEMENTO) +
                (partesCal * COEF_CAL) +
                (partesArena * COEF_ARENA) +
                (partesPiedra * COEF_PIEDRA) +
                (partesAgua * COEF_AGUA)

        if (volumenReal <= 0.0) return ResultadoProporcion(0.0, 0.0, 0.0, 0.0, 0.0)

        // 2. Factor: Cuántas veces entra esa mezcla en 1000 litros (1m3)
        val factor = 1000.0 / volumenReal // litros

        // 3. Cantidades Finales
        return ResultadoProporcion(
            cementoKg = (partesCemento * factor) * (DENSIDAD_CEMENTO_APARENTE / 1000.0),
            calKg = (partesCal * factor) * (DENSIDAD_CAL_APARENTE / 1000.0),
            arenaM3 = (partesArena * factor) / 1000.0,
            piedraM3 = (partesPiedra * factor) / 1000.0,
            aguaLitros = partesAgua * factor // Litros totales por m3
        )
    }

    fun calculateRecipeByBag(
        partesArena: Double,
        partesPiedra: Double,
        relacionAC: Double,
        capacidadBaldeL: Double = 20.0
    ): RecetaHormigonera {
        val litrosPorBolsaCemento = 35.7 // Volumen aparente de 50kg de cemento

        return RecetaHormigonera(
            baldesArena = (litrosPorBolsaCemento * partesArena) / capacidadBaldeL,
            baldesPiedra = (litrosPorBolsaCemento * partesPiedra) / capacidadBaldeL,
            baldesAgua = (50.0 * relacionAC) / capacidadBaldeL, // El agua es por peso de cemento (1kg = 1L)
            capacidadBaldeL = capacidadBaldeL
        )
    }
}