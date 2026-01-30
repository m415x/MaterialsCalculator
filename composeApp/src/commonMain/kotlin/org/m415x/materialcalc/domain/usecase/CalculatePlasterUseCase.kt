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

import nl.jacobras.humanreadable.HumanReadable
import org.m415x.materialcalc.data.repository.StaticMaterialRepository
import org.m415x.materialcalc.domain.common.calculateWetMaterials
import org.m415x.materialcalc.domain.model.*
import org.m415x.materialcalc.domain.utils.calculateNetSurface
import kotlin.math.ceil

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
     * @param fineType Tipo de revoque fino seleccionado.
     * @param cementBagWeightKg Peso de la bolsa de cemento en kg.
     * @param limeBagWeightKg Peso de la bolsa de cal en kg.
     * @param premixBagWeightKg Peso de la bolsa de fino premezcla en kg.
     * @param percentagePlasterWaste Porcentaje de desperdicio en revoque.
     * @param priceSettings Configuración de precios para calcular costos.
     * @return Resultado del cálculo encapsulado en Result.
     */
    operator fun invoke(
        lengthMeters: Double,
        heightMeters: Double,
        thickThickness: Double,
        thinThickness: Double,
        isBothSides: Boolean,
        openingsList: List<Aperture>,
        mortarDosing: MortarDosing,
        fineType: FinePlasterType = FinePlasterType.LIME, // Nuevo parámetro
        cementBagWeightKg: Int,
        limeBagWeightKg: Int,
        premixBagWeightKg: Int,
        percentagePlasterWaste: Double,
        priceSettings: PriceSettings? = null
    ): Result<PlasterResult> {

        return try {
            // 1. GEOMETRÍA (ÁREA NETA)
            val netSurfaceOneSide = calculateNetSurface(
                length = lengthMeters,
                height = heightMeters,
                openingsList = openingsList
            )

            // Superficie Total (Aplicamos si son ambas caras)
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
            val finePercentageWaste = percentagePlasterWaste

            // Variables para el fino
            var finePremixKg = 0.0
            var fineLimeKg = 0.0
            var fineSandM3 = 0.0
            var fineCementBags = 0
            var fineLimeBags = 0
            var finePremixBags = 0

            // Calculamos solo lo necesario según el tipo seleccionado
            if (fineType == FinePlasterType.PREMIX) {
                // Opción 1: Premezcla (Rendimiento ~2.5 kg/m2)
                val premixPerformance = totalCalculationArea * 2.5
                finePremixKg = premixPerformance * (1 + finePercentageWaste)
                finePremixBags = ceil(finePremixKg / premixBagWeightKg).toInt()
            } else {
                // Opción 2: Tradicional
                val finePlasterRecipe = repository.getFinePlasterRecipe()
                val mathFine = calculateWetMaterials(
                    volumeM3 = geometricThinVolume,
                    recipe = finePlasterRecipe,
                    waste = finePercentageWaste,
                    limeBagWeight = limeBagWeightKg,
                    cementBagWeight = cementBagWeightKg
                )
                fineLimeKg = mathFine.limeKg
                fineSandM3 = mathFine.sandM3
                fineCementBags = mathFine.cementBags
                fineLimeBags = mathFine.limeBags
            }

            // CÁLCULO DE COSTOS
            var materialCost = 0.0
            var laborCost = 0.0
            val costBreakdown = mutableListOf<Pair<String, Double>>()

            if (priceSettings != null) {
                // Cemento (Bolsas) - Sumamos grueso + fino (si aplica)
                priceSettings.materialPrices.find { it.id == MaterialIds.CEMENT }?.let {
                    // Cemento del grueso
                    val totalBags = mathThick.cementBags + fineCementBags
                    if (totalBags > 0) {
                        val totalCost = it.price * totalBags
                        val unitPrice = HumanReadable.number(it.price.toLong())
                        materialCost += totalCost
                        costBreakdown.add("${it.name} ($totalBags x $$unitPrice)" to totalCost)
                    }
                }

                // Cal Hidratada (Bolsas) - Solo grueso
                priceSettings.materialPrices.find { it.id == MaterialIds.HYDRATED_LIME }?.let {
                    val totalBags = mathThick.limeBags
                    if (totalBags > 0) {
                        val totalCost = it.price * totalBags
                        val unitPrice = HumanReadable.number(it.price.toLong())
                        materialCost += totalCost
                        costBreakdown.add("${it.name} ($totalBags x $$unitPrice)" to totalCost)
                    }
                }

                // Cal Aérea (Bolsas) - Solo fino
                if (fineLimeBags > 0) {
                    priceSettings.materialPrices.find { it.id == MaterialIds.AERIAL_LIME }?.let {
                        val totalCost = it.price * fineLimeBags
                        val unitPrice = HumanReadable.number(it.price.toLong())
                        materialCost += totalCost
                        costBreakdown.add("${it.name} ($fineLimeBags x $$unitPrice)" to totalCost)
                    }
                }

                // Arena (1/2 m3) - Sumamos grueso + fino
                priceSettings.materialPrices.find { it.id == MaterialIds.SAND }?.let {
                    val totalSand = mathThick.sandM3 + fineSandM3
                    if (totalSand > 0) {
                        val sandRounded = ceil(totalSand * 2) / 2.0
                        val cost = it.price * sandRounded
                        val unitPrice = HumanReadable.number(it.price.toLong())
                        materialCost += cost
                        costBreakdown.add("${it.name} ($sandRounded x $$unitPrice)" to cost)
                    }
                }

                // Fino Premezcla (Bolsas)
                if (fineType == FinePlasterType.PREMIX) {
                    priceSettings.materialPrices.find { it.id == MaterialIds.PREMIX }?.let {
                        if (finePremixBags > 0) {
                            val cost = it.price * finePremixBags
                            val unitPrice = HumanReadable.number(it.price.toLong())
                            materialCost += cost
                            costBreakdown.add("${it.name} ($finePremixBags x $$unitPrice)" to cost)
                        }
                    }
                }

                // Mano de Obra (Revoque M2)
                priceSettings.laborPrices.find { it.id == LaborIds.PLASTER_M2 }?.let {
                    val cost = it.price * totalCalculationArea
                    laborCost += cost
                    /*
                        val unitPrice = HumanReadable.number(it.price.toLong())
                        costBreakdown.add("Mano de obra ${it.name}" to cost)
                    */
                }
            }

            Result.success(
                PlasterResult(
                    totalAreaM2 = totalCalculationArea,
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
                    finePremixKg = finePremixKg,
                    fineLimeKg = fineLimeKg,
                    fineSandM3 = fineSandM3,
                    finePercentageWaste = finePercentageWaste,
                    fineDosage = repository.getFinePlasterRecipe().mixingRatio,
                    materialCost = materialCost,
                    laborCost = laborCost,
                    costBreakdown = costBreakdown,
                    selectedFineType = fineType
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
