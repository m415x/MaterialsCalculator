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

package org.m415x.materialcalc.domain.usecase

import org.m415x.materialcalc.domain.common.calculateWetMaterials
import org.m415x.materialcalc.domain.model.Aperture
import org.m415x.materialcalc.domain.model.BrickProps
import org.m415x.materialcalc.domain.model.MortarDosing
import org.m415x.materialcalc.domain.model.WallResult
import org.m415x.materialcalc.domain.utils.calculateNetSurface
import kotlin.math.ceil

/**
 * Calcula los materiales para un muro.
 */
class CalculateWallUseCase {

    /**
     * Calcula los materiales para un muro.
     *
     * @param lengthMeters Largo del muro en metros.
     * @param heightMeters Alto del muro en metros.
     * @param brickProps Propiedades del ladrillo.
     * @param mortarDosing Receta de mortero.
     * @param openingList Lista de aberturas en el muro.
     * @param cementBagWeightKg Peso de la bolsa de cemento en kg.
     * @param limeBagWeightKg Peso de la bolsa de cal en kg.
     * @param percentageBrickWaste Porcentaje de desperdicio en ladrillos.
     * @param percentageMortarWaste Porcentaje de desperdicio en mortero.
     * @return Resultado del cálculo.
     */
    operator fun invoke(
        lengthMeters: Double,
        heightMeters: Double,
        brickProps: BrickProps,
        mortarDosing: MortarDosing,
        openingList: List<Aperture>,
        cementBagWeightKg: Int,
        limeBagWeightKg: Int,
        percentageBrickWaste: Double,
        percentageMortarWaste: Double
    ): WallResult {

        // 1. GEOMETRÍA (ÁREA NETA)
        val netSurface = calculateNetSurface(
            length = lengthMeters,
            height = heightMeters,
            openingsList = openingList
        )

        // 2. CÁLCULO DE LADRILLOS (Unidades Físicas)
        val brickSurfaceWithJoint =
            (brickProps.length + brickProps.gasketThickness) * (brickProps.height + brickProps.gasketThickness)
        val bricksM2 = 1.0 / brickSurfaceWithJoint
        val totalTheoreticalBricks = netSurface * bricksM2
        val actualQuantityBricks = ceil(totalTheoreticalBricks * (1 + percentageBrickWaste)).toInt()

        // 3. CÁLCULO DE MORTERO (Mezcla Húmeda)
        val wallVolumeM3 = netSurface * brickProps.width
        val volumeSolidBricks =
            totalTheoreticalBricks * (brickProps.length * brickProps.height * brickProps.width)
        val geometricMortarVolume = (wallVolumeM3 - volumeSolidBricks).coerceAtLeast(0.0)

        val mathMortar = calculateWetMaterials(
            volumeM3 = geometricMortarVolume,
            recipe = mortarDosing,
            waste = percentageMortarWaste,
            cementBagWeight = cementBagWeightKg,
            limeBagWeight = limeBagWeightKg
        )

        // 4. RESULTADO FINAL
        return WallResult(
            netAreaM2 = netSurface,
            quantityBricks = actualQuantityBricks,
            percentageBrickWaste = percentageBrickWaste,
            mortarM3 = geometricMortarVolume * (1 + percentageMortarWaste),
            cementKg = mathMortar.cementKg,
            limeKg = mathMortar.limeKg,
            sandM3 = mathMortar.sandM3,
            waterLiters = mathMortar.waterLiters,
            percentageMortarWaste = percentageMortarWaste,
            mixingRatio = mortarDosing.mixingRatio,
            cementBagKg = cementBagWeightKg,
            limeBagKg = limeBagWeightKg
        )
    }
}