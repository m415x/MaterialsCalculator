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
import org.m415x.materialcalc.domain.model.DosificacionHormigon
import org.m415x.materialcalc.domain.model.ResultadoHormigon

/**
 * Calcula los materiales para un volumen de hormigón.
 */
class CalculateConcreteUseCase {

    /**
     * Calcula los materiales para un volumen de hormigón.
     *
     * @param anchoMetros Ancho en metros.
     * @param largoMetros Largo en metros.
     * @param espesorMetros Espesor en metros.
     * @param tipo Tipo de hormigón.
     * @param pesoBolsaCementoKg Peso de la bolsa de cemento en kg.
     * @return Resultado del cálculo.
     */
    operator fun invoke(
        anchoMetros: Double,
        largoMetros: Double,
        espesorMetros: Double,
        quantityUnits: Int,
        receta: DosificacionHormigon,
        pesoBolsaCementoKg: Int,
        pesoBolsaCalKg: Int,
        porcentajeDesperdicio: Double
    ): ResultadoHormigon {

        // 1. Geometría (Esta es la única responsabilidad única de este UseCase)
        val volumenGeometrico = anchoMetros * largoMetros * espesorMetros * quantityUnits

        // 2. El motor hace el cálculo
        val mats = calculateWetMaterials(
            volumenM3 = volumenGeometrico,
            receta = receta,
            desperdicio = porcentajeDesperdicio,
            pesoBolsaCemento = pesoBolsaCementoKg,
            pesoBolsaCal = pesoBolsaCalKg
        )

        // 4. Mapeo al resultado final
        return ResultadoHormigon(
            volumenTotalM3 = volumenGeometrico,
            cementoKg = mats.cementoKg,
            arenaM3 = mats.arenaM3,
            piedraM3 = mats.piedraM3,
            aguaLitros = mats.aguaLitros,
            bolsaCementoKg = pesoBolsaCementoKg,
            porcentajeDesperdicioHormigon = porcentajeDesperdicio,
            proporcionMezcla = receta.descripcionProporcion
        )
    }
}