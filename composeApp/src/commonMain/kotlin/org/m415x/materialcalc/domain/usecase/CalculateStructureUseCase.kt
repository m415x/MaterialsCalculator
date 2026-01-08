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

import org.m415x.materialcalc.domain.common.WasteRegistry
import org.m415x.materialcalc.domain.common.calculateWetMaterials
import org.m415x.materialcalc.domain.model.ConcreteType
import org.m415x.materialcalc.domain.model.IronDiameter
import org.m415x.materialcalc.domain.model.ResultadoEstructura
import org.m415x.materialcalc.domain.model.SlabResult
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
     * @param largoMetros Largo de la estructura en metros.
     * @param ladoAMetros Lado A de la estructura en metros.
     * @param ladoBMetros Lado B de la estructura en metros.
     * @param isCircular Indica si la estructura es circular.
     * @param concreteType Tipo de hormigón.
     * @param diametroPrincipal Diámetro principal de hierro.
     * @param cantidadVarillas Cantidad de varillas.
     * @param diametroEstribo Diámetro de estribo de hierro.
     * @param separacionEstriboMetros Separación de estribo en metros.
     * @param pesoBolsaCementoKg Peso de la bolsa de cemento en kg.
     * @return Resultado del cálculo.
     */
    operator fun invoke(
        // Dimensiones generales
        largoMetros: Double,
        ladoAMetros: Double,
        ladoBMetros: Double,
        isCircular: Boolean = false,

        // Configuración Hormigón
        concreteType: ConcreteType,

        // Configuración Armadura Principal (Los hierros largos)
        diametroPrincipal: IronDiameter,
        cantidadVarillas: Int,  // Ej. 4 hierros

        // Configuración Estribos (Los anillos)
        diametroEstribo: IronDiameter,
        separacionEstriboMetros: Double,

        // Configuraciones opcionales (con defaults)
        pesoBolsaCementoKg: Int = 25,
        desperdicioHormigon: Double,
        desperdicioHierroPrincipal: Double,
        desperdicioEstribos: Double
    ): ResultadoEstructura {

        // ============================================================
        // 1. CÁLCULO DE HORMIGÓN (Usando el Motor Unificado)
        // ============================================================

        // A. Volumen Geométrico
        val volumenGeometrico = if (isCircular) {
            val radio = ladoAMetros / 2
            PI * radio.pow(2) * largoMetros
        } else {
            ladoAMetros * ladoBMetros * largoMetros
        }

        // B. Datos y Desperdicios
        val receta = repository.getConcreteDosing(concreteType)
            ?: throw IllegalArgumentException("Hormigón no encontrado")

        // C. Cálculo automático de materiales húmedos
        val matsConcreto = calculateWetMaterials(
            volumeM3 = volumenGeometrico,
            recipe = receta,
            waste = desperdicioHormigon,
            cementBagWeight = pesoBolsaCementoKg,
            limeBagWeight = 25
        )

        // ============================================================
        // 2. CÁLCULO DE ARMADURA (HIERROS)
        // ============================================================

        // Obtenemos desperdicios específicos del registro
        val desperdicioPrincipal = WasteRegistry.getForIron(isEstribo = false)
        val desperdicioEstribos = WasteRegistry.getForIron(isEstribo = true)

        // --- A. Hierro Principal ---
        val pesoMetroPrincipal = repository.getIronWeightPerMeter(diametroPrincipal)

        val longitudTotalPrincipal = (cantidadVarillas * largoMetros) * (1 + desperdicioHierroPrincipal)
        val pesoTotalPrincipal = longitudTotalPrincipal * pesoMetroPrincipal

        val longitudComercialHierroMetros = 12 // Hardcoded for now
        val barrasPrincipalComprar = ceil(longitudTotalPrincipal / longitudComercialHierroMetros).toInt()

        // --- B. Estribos ---
        val pesoMetroEstribo = repository.getIronWeightPerMeter(diametroEstribo)
        val cantidadEstribos = ceil(largoMetros / separacionEstriboMetros).toInt()

        // Geometría del estribo (Longitud de una vuelta)
        val longitudUnEstribo = if (isCircular) {
            val diametroReal = (ladoAMetros - 0.05).coerceAtLeast(0.0) // Restamos recubrimiento
            (PI * diametroReal) + 0.15 // +15cm ganchos
        } else {
            val aReal = (ladoAMetros - 0.05).coerceAtLeast(0.0)
            val bReal = (ladoBMetros - 0.05).coerceAtLeast(0.0)
            (2 * aReal + 2 * bReal) + 0.15 // +15cm ganchos
        }

        val longitudTotalEstribos = (cantidadEstribos * longitudUnEstribo) * (1 + desperdicioEstribos)
        val pesoTotalEstribos = longitudTotalEstribos * pesoMetroEstribo

        val barrasEstribosComprar = ceil(longitudTotalEstribos / longitudComercialHierroMetros).toInt()

        // ============================================================
        // 3. RESULTADO FINAL
        // ============================================================
        return ResultadoEstructura(
            // Hormigón (Viene del motor)
            volumenHormigonM3 = volumenGeometrico * (1 + desperdicioHormigon),
            cementoKg = matsConcreto.cementoKg,
            arenaM3 = matsConcreto.arenaM3,
            piedraM3 = matsConcreto.piedraM3,
            aguaLitros = matsConcreto.aguaLitros,
            bolsaCementoKg = pesoBolsaCementoKg,
            porcentajeDesperdicioHormigon = desperdicioHormigon,

            // Armadura (Calculada aquí)
            diametroPrincipal = diametroPrincipal,
            diametroEstribo = diametroEstribo,
            hierroPrincipalKg = pesoTotalPrincipal,
            hierroEstribosKg = pesoTotalEstribos,
            hierroPrincipalMetros = longitudTotalPrincipal,
            hierroEstribosMetros = longitudTotalEstribos,
            cantidadHierroPrincipal = barrasPrincipalComprar,
            cantidadHierroEstribos = barrasEstribosComprar,
            porcentajeDesperdicioHierroPrincipal = desperdicioHierroPrincipal,
            porcentajeDesperdicioHierroEstribos = desperdicioEstribos
        )
    }

    fun calculateSlab(
        widthX: Double,      // m
        lengthY: Double,     // m
        sepXcm: Double,      // cm
        sepYcm: Double,      // cm
        phiX: Double,        // mm
        phiY: Double,        // mm
        wastePct: Double,    // % (desde Settings)
        includeHooks: Boolean
    ): SlabResult {
        val sepXm = sepXcm / 100.0
        val sepYm = sepYcm / 100.0

        // 1. Cantidad de barras (CIRSOC 201 sugiere cubrir todo el paño)
        val countX = ceil(lengthY / sepXm).toInt() + 1
        val countY = ceil(widthX / sepYm).toInt() + 1

        // 2. Longitud individual (Recubrimiento típico 2cm por lado)
        val recubrimiento = 0.02 * 2
        val hookL = if (includeHooks) (10 * (phiX / 1000.0)) * 2 else 0.0 // Pata estándar 10*phi

        val individualLengthX = (widthX - recubrimiento) + hookL
        val individualLengthY = (lengthY - recubrimiento) + hookL

        // 3. Totales
        val netMeters = (countX * individualLengthX) + (countY * individualLengthY)
        val totalMeters = netMeters * (1 + (wastePct / 100))

        // Peso específico del acero: (phi^2 / 162.2) kg/m
        val weightX = (phiX * phiX / 162.2) * (countX * individualLengthX)
        val weightY = (phiY * phiY / 162.2) * (countY * individualLengthY)
        val totalWeight = (weightX + weightY) * (1 + (wastePct / 100))

        return SlabResult(
            totalWeightKg = totalWeight,
            totalMeters = totalMeters,
            countX = countX,
            countY = countY,
            lengthX = individualLengthX,
            lengthY = individualLengthY,
            commercialBars12m = ceil(totalMeters / 12.0).toInt(),
            wasteAmountKg = totalWeight - (weightX + weightY)
        )
    }
}