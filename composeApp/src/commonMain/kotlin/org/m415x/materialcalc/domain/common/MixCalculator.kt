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
    const val DENSIDAD_CEMENTO = 1400.0

    // Coeficientes de Aporte (Volumen Real / Volumen Aparente)
    // Fuente: Chandias / Manuales de Construcción
    const val COEF_CEMENTO = 0.47
    const val COEF_CAL = 0.37 // Polvo
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

    fun calculateByParts(
        partesCemento: Double,
        partesCal: Double,
        partesArena: Double,
        partesPiedra: Double,
        partesAgua: Double // Generalmente es un porcentaje del cemento, pero si lo ponen por partes...
    ): ResultadoProporcion {
        // 1. Calcular el Volumen Real que genera esa suma de partes (ej: 1 balde + 3 baldes...)
        val volumenReal = (partesCemento * COEF_CEMENTO) +
                (partesCal * COEF_CAL) +
                (partesArena * COEF_ARENA) +
                (partesPiedra * COEF_PIEDRA) +
                (partesAgua * COEF_AGUA)

        if (volumenReal == 0.0) return ResultadoProporcion(0.0, 0.0, 0.0, 0.0, 0.0)

        // 2. Factor de Multiplicación para llegar a 1 m3 (1000 litros)
        // Cuántas veces entra esa "mezclita" en 1 metro cúbico real
        val factor = 1000.0 / volumenReal // litros

        // 3. Calcular cantidades por m3
        // Cemento: (Partes * Factor) nos da LITROS aparentes de cemento. Multiplicamos por densidad para KG.
        val cementoKg = (partesCemento * factor) * (DENSIDAD_CEMENTO / 1000.0)

        // Cal: Similar, asumimos densidad aprox 600kg/m3 si quisiéramos kg, pero simplifiquemos
        // Si la cal viene en bolsa de 25kg, y densidad ~500-600.
        val calKg = (partesCal * factor) * 0.6 // Aprox 600kg/m3 densidad aparente

        // Arena y Piedra: Queremos m3 aparentes (volumen de compra)
        val arenaM3 = (partesArena * factor) / 1000.0
        val piedraM3 = (partesPiedra * factor) / 1000.0

        // Agua: Relación A/C estimada
        val aguaLitros = (partesAgua * factor)

        return ResultadoProporcion(cementoKg, calKg, arenaM3, piedraM3, aguaLitros / cementoKg)
    }
}