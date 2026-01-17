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

import org.m415x.materialcalc.domain.utils.ConstructionConstants

/**
 * Clase auxiliar para calcular proporciones.
 *
 * @property ResultProportion Clase auxiliar para devolver los resultados brutos calculados.
 */
object MixCalculator {

    data class ResultProportion(
        val cementKg: Double,
        val limeKg: Double,
        val sandM3: Double,
        val gravelM3: Double,
        val waterLiters: Double
    )

    data class MixParts(
        val partCement: Double,
        val partLime: Double,
        val partSand: Double,
        val partGravel: Double
    )

    /**
     * Función pura que calcula proporciones técnicas (kg/m3) a partir de partes (baldes).
     *
     * @param cementParts Partes de cemento.
     * @param limeParts Partes de cal.
     * @param sandParts Partes de arena.
     * @param gravelParts Partes de piedra.
     * @param waterCementRatio Relación agua/cemento.
     *
     * @return ResultProportion con los resultados.
     */
    fun calculateByParts(
        cementParts: Double,
        limeParts: Double,
        sandParts: Double,
        gravelParts: Double,
        waterCementRatio: Double
    ): ResultProportion {

        // El agua se calcula en base al cemento (es lo más real en obra)
        // Si hay cal, el agua se estima en base a la arena (aprox 25% del volumen de arena para llegar a ~250L/m3)
        val waterParts = if (limeParts > 0) {
            sandParts * 0.25
        } else {
            if (waterCementRatio > 0) cementParts * waterCementRatio else 0.0
        }

        // 1. Calcular el Volumen Real que genera esa suma de partes
        val actualVolume = (cementParts * ConstructionConstants.COEF_CEMENT) +
                (limeParts * ConstructionConstants.COEF_LIME) +
                (sandParts * ConstructionConstants.COEF_SAND) +
                (gravelParts * ConstructionConstants.COEF_GRAVEL) +
                (waterParts * ConstructionConstants.COEF_WATER)

        if (actualVolume <= 0.0) return ResultProportion(0.0, 0.0, 0.0, 0.0, 0.0)

        // 2. Factor: Cuántas veces entra esa mezcla en 1000 litros (1m3)
        val factor = 1000.0 / actualVolume

        // 3. Cantidades Finales
        return ResultProportion(
            cementKg = (cementParts * factor) * (ConstructionConstants.APPARENT_CEMENT_DENSITY / 1000.0),
            limeKg = (limeParts * factor) * (ConstructionConstants.APPARENT_LIME_DENSITY / 1000.0),
            sandM3 = (sandParts * factor) / 1000.0,
            gravelM3 = (gravelParts * factor) / 1000.0,
            waterLiters = waterParts * factor
        )
    }

    /**
     * Función inversa: Calcula las partes (baldes) a partir de la dosificación técnica (kg/m3).
     * Normaliza asumiendo Cemento = 1 parte.
     *
     * @param cementKg Cemento en kg.
     * @param limeKg Cal en kg.
     * @param sandM3 Arena en m3.
     * @param gravelM3 Piedra en m3.
     *
     * @return MixParts con las partes normalizadas.
     */
    fun calculatePartsFromTechnical(
        cementKg: Double,
        limeKg: Double,
        sandM3: Double,
        gravelM3: Double
    ): MixParts {
        if (cementKg <= 0.0) return MixParts(0.0, 0.0, 0.0, 0.0)

        // 1. Convertir todo a volumen aparente (m3)
        val volCem = cementKg / ConstructionConstants.APPARENT_CEMENT_DENSITY
        val volLime = limeKg / ConstructionConstants.APPARENT_LIME_DENSITY
        val volSand = sandM3 // Ya está en m3
        val volGravel = gravelM3 // Ya está en m3

        // 2. Normalizar respecto al cemento (Cemento = 1)
        return MixParts(
            partCement = 1.0,
            partLime = volLime / volCem,
            partSand = volSand / volCem,
            partGravel = volGravel / volCem
        )
    }
}