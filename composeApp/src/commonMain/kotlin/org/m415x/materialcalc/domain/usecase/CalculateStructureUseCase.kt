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
import org.m415x.materialcalc.domain.model.*
import org.m415x.materialcalc.domain.repository.MaterialRepository
import kotlin.math.PI
import kotlin.math.ceil
import kotlin.math.pow

/**
 * Calcula los materiales para una estructura.
 *
 * @param repository Repositorio de materiales.
 */
class CalculateStructureUseCase(private val repository: MaterialRepository) {

    /**
     * Calcula los materiales para una estructura.
     *
     * @param lengthMeters Largo de la estructura en metros.
     * @param sideAMeters Lado A de la estructura en metros.
     * @param sideBMeters Lado B de la estructura en metros.
     * @param isCircular Indica si la estructura es circular.
     * @param concreteType Tipo de hormigón.
     * @param mainIronDiameter Diámetro principal de hierro.
     * @param mainIronQuantity Cantidad de varillas.
     * @param stirrupIronDiameter Diámetro de estribo de hierro.
     * @param stirrupSpacingMeters Separación de estribo en metros.
     * @param cementBagWeightKg Peso de la bolsa de cemento en kg.
     * @param limeBagWeightKg Peso de la bolsa de cal en kg.
     * @param percentageCementWaste Porcentaje de desperdicio en hormigón.
     * @param percentageMainIronWaste Porcentaje de desperdicio en hierro principal.
     * @param percentageStirrupIronWaste Porcentaje de desperdicio enswiper de hierro.
     * @param startHookLengthMeters Longitud adicional por gancho inicial en metros.
     * @param endHookLengthMeters Longitud adicional por gancho final en metros.
     * @return Resultado del cálculo.
     */
    operator fun invoke(
        // Dimensiones generales
        lengthMeters: Double,
        sideAMeters: Double,
        sideBMeters: Double,
        isCircular: Boolean = false,
        // Configuración Hormigón
        concreteType: ConcreteType,
        // Configuración Armadura Principal (Los hierros largos)
        mainIronDiameter: IronDiameter,
        mainIronQuantity: Int,
        // Configuración Estribos (Los anillos)
        stirrupIronDiameter: IronDiameter,
        stirrupSpacingMeters: Double,
        // Configuraciones opcionales
        cementBagWeightKg: Int,
        limeBagWeightKg: Int,
        percentageCementWaste: Double,
        percentageMainIronWaste: Double,
        percentageStirrupIronWaste: Double,
        // Hierros custom (opcionales, para sobreescribir el peso si se usan)
        customMainIron: CustomIron? = null,
        customStirrupIron: CustomIron? = null,
        // Ganchos
        startHookLengthMeters: Double = 0.0,
        endHookLengthMeters: Double = 0.0
    ): StructureResult {

        // 1. CÁLCULO DE HORMIGÓN (Usando el Motor Unificado)
        // A. Volumen Geométrico
        val geometricStructureVolume = if (isCircular) {
            val radius = sideAMeters / 2
            PI * radius.pow(2) * lengthMeters
        } else {
            sideAMeters * sideBMeters * lengthMeters
        }

        // B. Datos y Desperdicios
        val concreteDosing = repository.getConcreteDosing(concreteType)
            ?: throw IllegalArgumentException("Hormigón no encontrado")

        // C. Cálculo automático de materiales húmedos
        val mathConcrete = calculateWetMaterials(
            volumeM3 = geometricStructureVolume,
            recipe = concreteDosing,
            waste = percentageCementWaste,
            cementBagWeight = cementBagWeightKg,
            limeBagWeight = limeBagWeightKg
        )

        // 2. CÁLCULO DE ARMADURA (HIERROS)
        val commercialLengthIronMeters = 12 // Hardcoded for now

        // --- A. Hierro Principal ---
        // Usamos el peso del custom si existe, sino el del enum
        val mainIronWeight = customMainIron?.linearWeight ?: repository.getIronWeightPerMeter(mainIronDiameter)

        // La longitud total de cada barra incluye el largo de la estructura más los ganchos
        val singleBarLength = lengthMeters + startHookLengthMeters + endHookLengthMeters
        val mainIronTotalLength = (mainIronQuantity * singleBarLength) * (1 + percentageMainIronWaste)
        val mainIronTotalWeight = mainIronTotalLength * mainIronWeight
        val mainIronBarsBuy = ceil(mainIronTotalLength / commercialLengthIronMeters).toInt()

        // --- B. Estribos ---
        // Usamos el peso del custom si existe, sino el del enum
        val stirrupIronWeight = customStirrupIron?.linearWeight ?: repository.getIronWeightPerMeter(stirrupIronDiameter)
        val stirrupQuantity = ceil(lengthMeters / stirrupSpacingMeters).toInt()

        // Geometría del estribo (Longitud de una vuelta)
        val stirrupLengthMeters = if (isCircular) {
            val actualDiameter = (sideAMeters - 0.05).coerceAtLeast(0.0) // Restamos recubrimiento
            (PI * actualDiameter) + 0.15 // +15cm ganchos
        } else {
            val realASide = (sideAMeters - 0.05).coerceAtLeast(0.0)
            val realBSide = (sideBMeters - 0.05).coerceAtLeast(0.0)
            (2 * realASide + 2 * realBSide) + 0.15 // +15cm ganchos
        }

        val stirrupIronTotalLength = (stirrupQuantity * stirrupLengthMeters) * (1 + percentageStirrupIronWaste)
        val stirrupIronTotalWeight = stirrupIronTotalLength * stirrupIronWeight
        val stirrupIronBarsBuy = ceil(stirrupIronTotalLength / commercialLengthIronMeters).toInt()

        // 3. RESULTADO FINAL
        return StructureResult(
            volumeConcreteM3 = geometricStructureVolume * (1 + percentageCementWaste),
            cementKg = mathConcrete.cementKg,
            sandM3 = mathConcrete.sandM3,
            gravelM3 = mathConcrete.gravelM3,
            waterLiters = mathConcrete.waterLiters,
            cementBagKg = cementBagWeightKg,
            percentageConcreteWaste = percentageCementWaste,
            mainDiameter = mainIronDiameter, // TODO: Considerar si StructureResult debe soportar CustomIron en el futuro para mostrar el nombre correcto
            stirrupDiameter = stirrupIronDiameter,
            mainIronKg = mainIronTotalWeight,
            stirrupIronKg = stirrupIronTotalWeight,
            mainIronMeters = mainIronTotalLength,
            stirrupIronMeters = stirrupIronTotalLength,
            mainIronAmount = mainIronBarsBuy,
            stirrupIronAmount = stirrupIronBarsBuy,
            percentageMainIronWaste = percentageMainIronWaste,
            percentageStirrupIronWaste = percentageStirrupIronWaste
        )
    }

    fun calculateSlab(
        widthX: Double,      // m
        lengthY: Double,     // m
        thickness: Double,   // m (espesor)
        sepXcm: Double,      // cm
        sepYcm: Double,      // cm
        phiX: IronDiameter,  // mm
        phiY: IronDiameter,  // mm
        wastePct: Double,    // % (desde Settings)
        hookLengthMeters: Double, // Longitud de gancho (se asume igual para ambos extremos y ambas direcciones)
        concreteType: ConcreteType,
        cementBagWeightKg: Int,
        limeBagWeightKg: Int,
        percentageConcreteWaste: Double
    ): SlabResult {
        val sepXm = sepXcm / 100.0
        val sepYm = sepYcm / 100.0

        // 1. Cantidad de barras (CIRSOC 201 sugiere cubrir todo el paño)
        // Se suma 1 para asegurar que se cubra el borde final
        val countX = ceil(lengthY / sepXm).toInt() + 1
        val countY = ceil(widthX / sepYm).toInt() + 1

        // 2. Longitud individual (Recubrimiento típico 2cm por lado)
        val covering = 0.02 * 2
        // El gancho se aplica en ambos extremos
        val totalHookL = hookLengthMeters * 2

        val individualLengthX = (widthX - covering) + totalHookL
        val individualLengthY = (lengthY - covering) + totalHookL

        // 3. Totales
        val netMetersX = countX * individualLengthX
        val netMetersY = countY * individualLengthY
        val totalMeters = (netMetersX + netMetersY) * (1 + wastePct)

        // Peso específico del acero
        val weightX = repository.getIronWeightPerMeter(phiX) * netMetersX
        val weightY = repository.getIronWeightPerMeter(phiY) * netMetersY
        val totalWeight = (weightX + weightY) * (1 + wastePct)

        val commercialBars12m = ceil(totalMeters / 12.0).toInt()

        // 4. Hormigón
        val volumeM3 = widthX * lengthY * thickness
        val concreteDosing = repository.getConcreteDosing(concreteType)
            ?: throw IllegalArgumentException("Hormigón no encontrado")

        val mathConcrete = calculateWetMaterials(
            volumeM3 = volumeM3,
            recipe = concreteDosing,
            waste = percentageConcreteWaste,
            cementBagWeight = cementBagWeightKg,
            limeBagWeight = limeBagWeightKg
        )

        return SlabResult(
            totalWeightKg = totalWeight,
            totalMeters = totalMeters,
            countX = countX,
            countY = countY,
            lengthX = individualLengthX,
            lengthY = individualLengthY,
            diameterX = phiX.milimeters,
            diameterY = phiY.milimeters,
            weightX = weightX * (1 + wastePct),
            weightY = weightY * (1 + wastePct),
            commercialBars12m = commercialBars12m,
            wasteAmountKg = totalWeight - (weightX + weightY),
            suggestedMesh = null,
            meshPanelsNeeded = null,
            percentageIronWaste = wastePct,
            volumeConcreteM3 = volumeM3 * (1 + percentageConcreteWaste),
            cementKg = mathConcrete.cementKg,
            sandM3 = mathConcrete.sandM3,
            gravelM3 = mathConcrete.gravelM3,
            waterLiters = mathConcrete.waterLiters,
            cementBagKg = cementBagWeightKg,
            percentageConcreteWaste = percentageConcreteWaste
        )
    }

    fun calculateSlabWithMesh(
        widthX: Double,
        lengthY: Double,
        thickness: Double,
        meshId: String,
        concreteType: ConcreteType,
        cementBagWeightKg: Int,
        limeBagWeightKg: Int,
        percentageConcreteWaste: Double
    ): SlabResult {
        // 1. Calcular paneles de malla
        // Panel estándar de 2.4m x 6m = 14.4 m2
        val panelArea = 2.4 * 6.0
        val slabArea = widthX * lengthY
        // Se suele agregar un desperdicio por solapes (aprox 10-15%)
        val panelsNeeded = ceil((slabArea * 1.15) / panelArea).toInt()

        // 2. Hormigón
        val volumeM3 = widthX * lengthY * thickness
        val concreteDosing = repository.getConcreteDosing(concreteType)
            ?: throw IllegalArgumentException("Hormigón no encontrado")

        val mathConcrete = calculateWetMaterials(
            volumeM3 = volumeM3,
            recipe = concreteDosing,
            waste = percentageConcreteWaste,
            cementBagWeight = cementBagWeightKg,
            limeBagWeight = limeBagWeightKg
        )

        return SlabResult(
            totalWeightKg = 0.0, // No calculamos peso exacto de malla por ahora
            totalMeters = 0.0,
            countX = 0,
            countY = 0,
            lengthX = 0.0,
            lengthY = 0.0,
            diameterX = 0.0,
            diameterY = 0.0,
            weightX = 0.0,
            weightY = 0.0,
            commercialBars12m = 0,
            wasteAmountKg = 0.0,
            suggestedMesh = meshId.uppercase(),
            meshPanelsNeeded = panelsNeeded,
            percentageIronWaste = 0.0,
            volumeConcreteM3 = volumeM3 * (1 + percentageConcreteWaste),
            cementKg = mathConcrete.cementKg,
            sandM3 = mathConcrete.sandM3,
            gravelM3 = mathConcrete.gravelM3,
            waterLiters = mathConcrete.waterLiters,
            cementBagKg = cementBagWeightKg,
            percentageConcreteWaste = percentageConcreteWaste
        )
    }
}