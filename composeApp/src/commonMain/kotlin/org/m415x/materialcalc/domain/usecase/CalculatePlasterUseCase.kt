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

import org.m415x.materialcalc.data.repository.StaticMaterialRepository
import org.m415x.materialcalc.domain.common.calculateWetMaterials
import org.m415x.materialcalc.domain.model.Aperture
import org.m415x.materialcalc.domain.model.MortarDosing
import org.m415x.materialcalc.domain.model.PlasterResult
import org.m415x.materialcalc.domain.utils.calculateNetSurface

/**
 * Calcula los materiales para un muro.
 *
 * @param repository Repositorio de materiales.
 */
class CalculatePlasterUseCase(private val repository: StaticMaterialRepository) {

    /**
     * Calcula los materiales para un muro.
     *
     * @param lengthMeters Largo de la pared en metros.
     * @param heightMeters Alto de la pared en metros.
     * @param thickThickness Espesor del revoque grueso en metros.
     * @param thinThickness Espesor del revoque fino en metros.
     * @param isBothSides Indica si se calcula para ambas caras.
     * @param openingsList Lista de aberturas en la pared.
     * @param mortarDosing Receta de mortero.
     * @param cementBagWeightKg Peso de la bolsa de cemento en kg.
     * @param limeBagWeightKg Peso de la bolsa de cal en kg.
     * @param premixBagWeightKg Peso de la bolsa de fino premezcla en kg.
     * @param percentagePlasterWaste Porcentaje de desperdicio en revoque.
     * @return Resultado del cálculo.
     */
    operator fun invoke(
        lengthMeters: Double,
        heightMeters: Double,
        thickThickness: Double,
        thinThickness: Double,
        isBothSides: Boolean,
        openingsList: List<Aperture>,
        mortarDosing: MortarDosing,
        cementBagWeightKg: Int,
        limeBagWeightKg: Int,
        premixBagWeightKg: Int,
        percentagePlasterWaste: Double,
    ): PlasterResult {

        // 1. GEOMETRÍA (ÁREA NETA)
        val netSurfaceOneSide = calculateNetSurface(
            length = lengthMeters,
            height = heightMeters,
            openingsList = openingsList
        )

        // Superficie Total (Aplicamos si son ambas caras)
        // Si hay ventana, se descuenta de ambos lados, así que la lógica se mantiene:
        // (Pared - Ventana) * 2 lados
        val totalCalculationArea = if (isBothSides) netSurfaceOneSide * 2 else netSurfaceOneSide

        // CÁLCULO DE REVOQUE GRUESO (JAHARRO)
        val geometricThickVolume = totalCalculationArea * thickThickness

        val mathThick = calculateWetMaterials(
            volumeM3 = geometricThickVolume,
            recipe = mortarDosing,
            waste = percentagePlasterWaste,
            cementBagWeight = cementBagWeightKg,
            limeBagWeight = limeBagWeightKg
        )

        // CÁLCULO DE REVOQUE FINO (ENLUCIDO)
        val geometricThinVolume = totalCalculationArea * thinThickness
        // Usamos el mismo porcentaje de desperdicio que para el grueso
        val finePercentageWaste = percentagePlasterWaste

        // Opción 1: Premezcla (Rendimiento ~2.5 kg/m2)
        val premixPerformance = totalCalculationArea * 2.5
        val totalFinePemix = premixPerformance * (1 + finePercentageWaste)

        // Opción 2: Tradicional
        val finePlasterRecipe = repository.getFinePlasterRecipe()
        val mathFine = calculateWetMaterials(
            volumeM3 = geometricThinVolume,
            recipe = finePlasterRecipe,
            waste = finePercentageWaste,
            limeBagWeight = limeBagWeightKg,
            cementBagWeight = cementBagWeightKg
        )

        return PlasterResult(
            totalAreaM2 = totalCalculationArea, // Área real a cubrir
            cementBagKg = cementBagWeightKg,
            limeBagKg = limeBagWeightKg,
            premixBagKg = premixBagWeightKg,
            thickVolumeM3 = geometricThickVolume * (1 + percentagePlasterWaste),
            thickCementKg = mathThick.cementKg,
            thickLimeKg = mathThick.limeKg,
            thickSandKg = mathThick.sandM3,
            thickWaterLiters = mathThick.waterLiters,
            thickPercentageWaste = percentagePlasterWaste,
            thickDosage = mortarDosing.mixingRatio,
            finePremixKg = totalFinePemix,
            fineLimeKg = mathFine.limeKg,
            fineSandM3 = mathFine.sandM3,
            finePercentageWaste = finePercentageWaste,
            fineDosage = finePlasterRecipe.mixingRatio
        )
    }
}