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

package org.m415x.materialcalc.domain.usecase

import org.m415x.materialcalc.domain.common.calculateWetMaterials
import org.m415x.materialcalc.domain.model.ConcreteDosing
import org.m415x.materialcalc.domain.model.ConcreteResult

/**
 * Calcula los materiales para un volumen de hormigón.
 */
class CalculateConcreteUseCase {

    /**
     * Calcula los materiales para un volumen de hormigón.
     *
     * @param widthMeters Ancho en metros.
     * @param lengthMeters Largo en metros.
     * @param thicknessMeters Espesor en metros.
     * @param quantityUnits Cantidad de unidades.
     * @param recipe Receta de hormigón.
     * @param cementBagWeightKg Peso de la bolsa de cemento en kg.
     * @param limeBagWeightKg Peso de la bolsa de cal en kg.
     * @param wastePercentage Porcentaje de desperdicio.
     * @return Resultado del cálculo.
     */
    operator fun invoke(
        widthMeters: Double,
        lengthMeters: Double,
        thicknessMeters: Double,
        quantityUnits: Int = 1,
        recipe: ConcreteDosing,
        cementBagWeightKg: Int,
        limeBagWeightKg: Int,
        wastePercentage: Double
    ): ConcreteResult {

        // 1. Geometría (Esta es la única responsabilidad única de este UseCase)
        val geometricVolume = widthMeters * lengthMeters * thicknessMeters * quantityUnits

        // 2. El motor hace el cálculo
        val mats = calculateWetMaterials(
            volumeM3 = geometricVolume,
            recipe = recipe,
            waste = wastePercentage,
            cementBagWeight = cementBagWeightKg,
            limeBagWeight = limeBagWeightKg
        )

        // 4. Mapeo al resultado final
        return ConcreteResult(
            totalVolumeM3 = geometricVolume,
            cementKg = mats.cementoKg,
            sandM3 = mats.arenaM3,
            gravelM3 = mats.piedraM3,
            waterLiters = mats.aguaLitros,
            cementBagKg = cementBagWeightKg,
            percentageConcreteWaste = wastePercentage,
            mixingRatio = recipe.descriptionProportion
        )
    }
}