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
     * @param unitQuantity Cantidad de unidades.
     * @param concreteDosing Receta de hormigón.
     * @param cementBagWeightKg Peso de la bolsa de cemento en kg.
     * @param limeBagWeightKg Peso de la bolsa de cal en kg.
     * @param percentageConcreteWaste Porcentaje de desperdicio de hormigón.
     * @return Resultado del cálculo encapsulado en Result.
     */
    operator fun invoke(
        widthMeters: Double,
        lengthMeters: Double,
        thicknessMeters: Double,
        unitQuantity: Int = 1,
        concreteDosing: ConcreteDosing,
        cementBagWeightKg: Int,
        limeBagWeightKg: Int,
        percentageConcreteWaste: Double
    ): Result<ConcreteResult> {

        return try {
            // 1. Geometría (Esta es la única responsabilidad única de este UseCase)
            val geometricVolume = widthMeters * lengthMeters * thicknessMeters * unitQuantity

            // 2. El motor hace el cálculo
            val mathConcrete = calculateWetMaterials(
                volumeM3 = geometricVolume,
                recipe = concreteDosing,
                waste = percentageConcreteWaste,
                cementBagWeight = cementBagWeightKg,
                limeBagWeight = limeBagWeightKg
            )

            // 3. Mapeo al resultado final
            Result.success(
                ConcreteResult(
                    totalVolumeM3 = geometricVolume,
                    cementKg = mathConcrete.cementKg,
                    sandM3 = mathConcrete.sandM3,
                    gravelM3 = mathConcrete.gravelM3,
                    waterLiters = mathConcrete.waterLiters,
                    cementBagKg = cementBagWeightKg,
                    percentageConcreteWaste = percentageConcreteWaste,
                    mixingRatio = concreteDosing.descriptionProportion
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}