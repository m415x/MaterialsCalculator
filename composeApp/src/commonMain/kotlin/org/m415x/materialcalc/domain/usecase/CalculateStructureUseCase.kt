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
import org.m415x.materialcalc.domain.registry.SimaMeshRegistry
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
     * @param priceSettings Configuración de precios para calcular costos.
     * @return Resultado del cálculo o una excepción encapsulada.
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
        endHookLengthMeters: Double = 0.0,
        priceSettings: PriceSettings? = null
    ): Result<StructureResult> {

        return try {
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
            val mainIronDiameterMm = customMainIron?.diameterMm ?: mainIronDiameter.milimeters

            // La longitud total de cada barra incluye el largo de la estructura más los ganchos
            val singleBarLength = lengthMeters + startHookLengthMeters + endHookLengthMeters
            val mainIronTotalLength = (mainIronQuantity * singleBarLength) * (1 + percentageMainIronWaste)
            val mainIronTotalWeight = mainIronTotalLength * mainIronWeight
            val mainIronBarsBuy = ceil(mainIronTotalLength / commercialLengthIronMeters).toInt()

            // --- B. Estribos ---
            // Usamos el peso del custom si existe, sino el del enum
            val stirrupIronWeight =
                customStirrupIron?.linearWeight ?: repository.getIronWeightPerMeter(stirrupIronDiameter)
            val stirrupIronDiameterMm = customStirrupIron?.diameterMm ?: stirrupIronDiameter.milimeters
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

            // CÁLCULO DE COSTOS
            var materialCost = 0.0
            var laborCost = 0.0

            if (priceSettings != null) {
                // Cemento (Bolsas)
                priceSettings.materialPrices.find { it.id == MaterialIds.CEMENT }?.let {
                    materialCost += it.price * mathConcrete.cementBags
                }

                // Arena (1/2 m3)
                priceSettings.materialPrices.find { it.id == MaterialIds.SAND }?.let {
                    val sandRounded = ceil(mathConcrete.sandM3 * 2) / 2.0
                    materialCost += it.price * sandRounded
                }

                // Piedra (1/2 m3)
                priceSettings.materialPrices.find { it.id == MaterialIds.STONE }?.let {
                    val gravelRounded = ceil(mathConcrete.gravelM3 * 2) / 2.0
                    materialCost += it.price * gravelRounded
                }

                // Hierros (Barras)
                // Buscamos el precio del hierro principal por ID si es estándar, o por nombre si es custom
                val mainIronId = if (customMainIron != null) customMainIron.id else mainIronDiameter.name
                priceSettings.materialPrices.find { it.id == mainIronId }?.let {
                    materialCost += it.price * mainIronBarsBuy
                }

                // Buscamos el precio del hierro estribo
                val stirrupIronId = if (customStirrupIron != null) customStirrupIron.id else stirrupIronDiameter.name
                priceSettings.materialPrices.find { it.id == stirrupIronId }?.let {
                    materialCost += it.price * stirrupIronBarsBuy
                }

                // Mano de Obra (Estructura ML)
                priceSettings.laborPrices.find { it.id == LaborIds.STRUCTURE_ML }?.let {
                    laborCost += it.price * lengthMeters
                }
            }

            // 3. RESULTADO FINAL
            Result.success(
                StructureResult(
                    volumeConcreteM3 = geometricStructureVolume * (1 + percentageCementWaste),
                    cementKg = mathConcrete.cementKg,
                    sandM3 = mathConcrete.sandM3,
                    gravelM3 = mathConcrete.gravelM3,
                    waterLiters = mathConcrete.waterLiters,
                    cementBagKg = cementBagWeightKg,
                    percentageConcreteWaste = percentageCementWaste,
                    mainDiameterMm = mainIronDiameterMm,
                    stirrupDiameterMm = stirrupIronDiameterMm,
                    mainIronKg = mainIronTotalWeight,
                    stirrupIronKg = stirrupIronTotalWeight,
                    mainIronMeters = mainIronTotalLength,
                    stirrupIronMeters = stirrupIronTotalLength,
                    mainIronAmount = mainIronBarsBuy,
                    stirrupIronAmount = stirrupIronBarsBuy,
                    percentageMainIronWaste = percentageMainIronWaste,
                    percentageStirrupIronWaste = percentageStirrupIronWaste,
                    materialCost = materialCost,
                    laborCost = laborCost
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun calculateSlab(
        widthX: Double,      // m
        lengthY: Double,     // m
        thickness: Double,   // m (espesor)
        sepXm: Double,      // m
        sepYm: Double,      // m
        phiX: IronDiameter,  // mm
        phiY: IronDiameter,  // mm
        wastePct: Double,    // % (desde Settings)
        hookLengthMeters: Double, // Longitud de gancho (se asume igual para ambos extremos y ambas direcciones)
        concreteType: ConcreteType,
        cementBagWeightKg: Int,
        limeBagWeightKg: Int,
        percentageConcreteWaste: Double,
        priceSettings: PriceSettings? = null
    ): Result<SlabResult> {
        return try {
            // 1. Cantidad de barras (CIRSOC 201 sugiere cubrir todo el paño)
            // Se suma 1 para asegurar que se cubra el borde final
            val countX = ceil(lengthY / sepXm).toInt() + 1
            val countY = ceil(widthX / sepYm).toInt() + 1

            // 2. Longitud individual (Recubrimiento típico 2cm por lado)
            val covering = 0.02 * 2
            // El gancho se aplica en ambos extremos
            val totalHookL = hookLengthMeters * 2

            val individualWidthX = (widthX - covering) + totalHookL
            val individualLengthY = (lengthY - covering) + totalHookL

            // 3. Totales
            val netMetersX = countX * individualWidthX
            val netMetersY = countY * individualLengthY
            val totalMeters = (netMetersX + netMetersY) * (1 + wastePct)

            // Peso específico del acero
            val weightX = repository.getIronWeightPerMeter(phiX) * netMetersX
            val weightY = repository.getIronWeightPerMeter(phiY) * netMetersY
            val totalWeight = (weightX + weightY) * (1 + wastePct)

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

            // CÁLCULO DE COSTOS
            var materialCost = 0.0
            var laborCost = 0.0

            if (priceSettings != null) {
                // Cemento (Bolsas)
                priceSettings.materialPrices.find { it.id == MaterialIds.CEMENT }?.let {
                    materialCost += it.price * mathConcrete.cementBags
                }

                // Arena (1/2 m3)
                priceSettings.materialPrices.find { it.id == MaterialIds.SAND }?.let {
                    val sandRounded = ceil(mathConcrete.sandM3 * 2) / 2.0
                    materialCost += it.price * sandRounded
                }

                // Piedra (1/2 m3)
                priceSettings.materialPrices.find { it.id == MaterialIds.STONE }?.let {
                    val gravelRounded = ceil(mathConcrete.gravelM3 * 2) / 2.0
                    materialCost += it.price * gravelRounded
                }

                // Hierros (Barras) - Aquí sumamos todo el hierro y calculamos barras totales
                // Pero como phiX y phiY pueden ser distintos, deberíamos calcular barras por separado si quisiéramos ser exactos en precio por diámetro
                // Simplificación: Asumimos que el precio por barra depende del diámetro.

                // Barras X
                val totalMetersX = netMetersX * (1 + wastePct)
                val barsX = ceil(totalMetersX / 12.0).toInt()
                priceSettings.materialPrices.find { it.id == phiX.name }?.let {
                    materialCost += it.price * barsX
                }

                // Barras Y
                val totalMetersY = netMetersY * (1 + wastePct)
                val barsY = ceil(totalMetersY / 12.0).toInt()
                priceSettings.materialPrices.find { it.id == phiY.name }?.let {
                    materialCost += it.price * barsY
                }

                // Mano de Obra (Losa M3)
                priceSettings.laborPrices.find { it.id == LaborIds.STRUCTURE_SLAB }?.let {
                    laborCost += it.price * volumeM3 // Se cobra por m3 de hormigón llenado
                }
            }

            Result.success(
                SlabResult(
                    totalWeightKg = totalWeight,
                    totalMeters = totalMeters,
                    countX = countX,
                    countY = countY,
                    widthX = individualWidthX,
                    lengthY = individualLengthY,
                    diameterX = phiX.milimeters,
                    diameterY = phiY.milimeters,
                    weightX = weightX * (1 + wastePct),
                    weightY = weightY * (1 + wastePct),
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
                    percentageConcreteWaste = percentageConcreteWaste,
                    commercialBarLength = 12,
                    materialCost = materialCost,
                    laborCost = laborCost
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun calculateSlabWithMesh(
        widthX: Double,
        lengthY: Double,
        thickness: Double,
        meshId: String,
        concreteType: ConcreteType,
        cementBagWeightKg: Int,
        limeBagWeightKg: Int,
        percentageConcreteWaste: Double,
        percentageMeshWaste: Double,
        priceSettings: PriceSettings? = null,
        customMeshes: List<CustomIron> = emptyList() // Recibimos las mallas custom
    ): Result<SlabResult> {
        return try {
            // 1. Calcular paneles de malla
            // Buscamos primero en custom, luego en estándar
            val customMesh = customMeshes.find { it.id == meshId }
            
            val panelWidth: Double
            val panelLength: Double
            
            if (customMesh != null) {
                panelWidth = customMesh.panelWidth
                panelLength = customMesh.panelLength
            } else {
                val standardMesh = SimaMeshRegistry.getMeshById(meshId)
                panelWidth = standardMesh.panelWidthM
                panelLength = standardMesh.panelLengthM
            }

            val panelArea = panelWidth * panelLength
            val slabArea = widthX * lengthY
            // Se suele agregar un desperdicio por solapes (aprox 10-15%)
            val panelsNeeded = ceil((slabArea * (1 + percentageMeshWaste)) / panelArea).toInt()

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

            // CÁLCULO DE COSTOS
            var materialCost = 0.0
            var laborCost = 0.0

            if (priceSettings != null) {
                // Cemento
                priceSettings.materialPrices.find { it.id == MaterialIds.CEMENT }?.let {
                    materialCost += it.price * mathConcrete.cementBags
                }
                // Arena
                priceSettings.materialPrices.find { it.id == MaterialIds.SAND }?.let {
                    val sandRounded = ceil(mathConcrete.sandM3 * 2) / 2.0
                    materialCost += it.price * sandRounded
                }
                // Piedra
                priceSettings.materialPrices.find { it.id == MaterialIds.STONE }?.let {
                    val gravelRounded = ceil(mathConcrete.gravelM3 * 2) / 2.0
                    materialCost += it.price * gravelRounded
                }

                // Malla
                priceSettings.materialPrices.find { it.id == meshId }?.let { materialCost += it.price * panelsNeeded }

                // Mano de Obra (Losa M3)
                priceSettings.laborPrices.find { it.id == LaborIds.STRUCTURE_SLAB }?.let {
                    laborCost += it.price * volumeM3
                }
            }

            Result.success(
                SlabResult(
                    totalWeightKg = 0.0, // No calculamos peso exacto de malla por ahora
                    totalMeters = 0.0,
                    countX = 0,
                    countY = 0,
                    widthX = widthX,
                    lengthY = lengthY,
                    diameterX = 0.0,
                    diameterY = 0.0,
                    weightX = 0.0,
                    weightY = 0.0,
                    commercialBarLength = 0,
                    wasteAmountKg = 0.0,
                    suggestedMesh = meshId.uppercase(),
                    meshPanelsNeeded = panelsNeeded,
                    percentageIronWaste = percentageMeshWaste, // Usamos el desperdicio de malla aquí para mostrarlo
                    volumeConcreteM3 = volumeM3 * (1 + percentageConcreteWaste),
                    cementKg = mathConcrete.cementKg,
                    sandM3 = mathConcrete.sandM3,
                    gravelM3 = mathConcrete.gravelM3,
                    waterLiters = mathConcrete.waterLiters,
                    cementBagKg = cementBagWeightKg,
                    percentageConcreteWaste = percentageConcreteWaste,
                    materialCost = materialCost,
                    laborCost = laborCost
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}