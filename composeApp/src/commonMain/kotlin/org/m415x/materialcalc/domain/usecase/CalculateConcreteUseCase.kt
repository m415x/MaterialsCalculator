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
import org.m415x.materialcalc.domain.common.calculateWetMaterials
import org.m415x.materialcalc.domain.model.*
import kotlin.math.ceil

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
     * @param priceSettings Configuración de precios para calcular costos.
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
        percentageConcreteWaste: Double,
        priceSettings: PriceSettings? = null
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

            // 3. Cálculo de costos (si hay configuración de precios)
            var materialCost = 0.0
            var laborCost = 0.0
            val costBreakdown = mutableListOf<Pair<String, Double>>()

            if (priceSettings != null) {
                // Cemento (Se compra por bolsa)
                priceSettings.materialPrices.find { it.id == MaterialIds.CEMENT }?.let {
                    val qty = mathConcrete.cementBags
                    val cost = it.price * qty
                    val unitPrice = HumanReadable.number(it.price.toLong())
                    materialCost += cost
                    costBreakdown.add("${it.name} ($qty x $$unitPrice)" to cost)
                }

                // Arena (Se compra por 1/2 m3, redondeamos hacia arriba a 0.5)
                priceSettings.materialPrices.find { it.id == MaterialIds.SAND }?.let {
                    val sandRounded = ceil(mathConcrete.sandM3 * 2) / 2.0
                    val cost = it.price * sandRounded
                    val unitPrice = HumanReadable.number(it.price.toLong())
                    materialCost += cost
                    costBreakdown.add("${it.name} ($sandRounded x $$unitPrice)" to cost)
                }

                // Piedra (Se compra por 1/2 m3, redondeamos hacia arriba a 0.5)
                priceSettings.materialPrices.find { it.id == MaterialIds.STONE }?.let {
                    val gravelRounded = ceil(mathConcrete.gravelM3 * 2) / 2.0
                    val cost = it.price * gravelRounded
                    val unitPrice = HumanReadable.number(it.price.toLong())
                    materialCost += cost
                    costBreakdown.add("${it.name} ($gravelRounded x $$unitPrice)" to cost)
                }

                // Mano de Obra (Hormigón)
                priceSettings.laborPrices.find { it.id == LaborIds.CONCRETE_M3 }?.let {
                    // Redondeamos el volumen a 2 decimales para mostrarlo limpio
                    val cost = it.price * geometricVolume
                    laborCost += cost
                    /*
                        val unitPrice = it.price.toInt()
                        costBreakdown.add("Mano de obra ${it.name}" to cost)
                    */
                }
            }

            // 4. Mapeo al resultado final
            Result.success(
                ConcreteResult(
                    totalVolumeM3 = geometricVolume,
                    cementKg = mathConcrete.cementKg,
                    sandM3 = mathConcrete.sandM3,
                    gravelM3 = mathConcrete.gravelM3,
                    waterLiters = mathConcrete.waterLiters,
                    cementBagKg = cementBagWeightKg,
                    percentageConcreteWaste = percentageConcreteWaste,
                    mixingRatio = concreteDosing.descriptionProportion,
                    materialCost = materialCost,
                    laborCost = laborCost,
                    costBreakdown = costBreakdown
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}