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
import org.m415x.materialcalc.domain.model.*
import org.m415x.materialcalc.domain.utils.calculateNetSurface
import kotlin.math.ceil

/**
 * Calcula los materiales para un volumen de hormigón.
 *
 * @param repository Repositorio de materiales.
 */
class CalculateWallUseCase {

    /**
     * Calcula los materiales para un volumen de hormigón.
     *
     * @param largoMuroMetros Largo del muro en metros.
     * @param altoMuroMetros Alto del muro en metros.
     * @param props Tipo de ladrillo.
     * @param aberturas Lista de aberturas en el muro.
     * @param bolsaCementoKg Peso de la bolsa de cemento en kg.
     * @param bolsaCalKg Peso de la bolsa de cal en kg.
     * @return Resultado del cálculo.
     */
    operator fun invoke(
        largoMuroMetros: Double,
        altoMuroMetros: Double,
        props: PropiedadesLadrillo,
        dosis: DosificacionMortero,
        aberturas: List<Abertura>,
        bolsaCementoKg: Int,
        bolsaCalKg: Int,
        desperdicioLadrillos: Double,
        desperdicioMortero: Double
    ): ResultadoMuro {

        // ============================================================
        // 1. GEOMETRÍA (ÁREA NETA)
        // ============================================================
        // Ahora esto incluye las validaciones automáticamente
        val areaNeta = calculateNetSurface(
            largo = largoMuroMetros,
            alto = altoMuroMetros,
            aberturas = aberturas
        )

        // ============================================================
        // 2. CÁLCULO DE LADRILLOS (Unidades Físicas)
        // ============================================================
        // Fórmula: 1 / ((Largo + Junta) * (Alto + Junta))
        val supLadrilloConJunta = (props.largoUnidad + props.espesorJunta) * (props.altoUnidad + props.espesorJunta)
        val ladrillosPorM2 = 1.0 / supLadrilloConJunta

        // Cantidad Teórica
        val totalLadrillosTeorico = areaNeta * ladrillosPorM2

        // Cantidad Real (con desperdicio)
        val cantidadRealLadrillos = ceil(totalLadrillosTeorico * (1 + desperdicioLadrillos)).toInt()

        // ============================================================
        // 3. CÁLCULO DE MORTERO (Mezcla Húmeda)
        // ============================================================

        // A. Volumen Geométrico del Muro (Área * Espesor)
        val volumenParedM3 = areaNeta * props.anchoMuro

        // B. Volumen ocupado por Ladrillos (Sin desperdicio, ocupación física real)
        val volumenLadrillosSolidos = totalLadrillosTeorico * (props.largoUnidad * props.altoUnidad * props.anchoMuro)

        // C. Volumen Geométrico de la Mezcla (Diferencia)
        val volumenMorteroGeo = (volumenParedM3 - volumenLadrillosSolidos).coerceAtLeast(0.0)

        // D. ¡MOTOR DE CÁLCULO! (Aplica desperdicio 15% y calcula materiales)
        val matsMortero = calculateWetMaterials(
            volumenM3 = volumenMorteroGeo,
            receta = dosis,
            desperdicio = desperdicioMortero,
            pesoBolsaCemento = bolsaCementoKg,
            pesoBolsaCal = bolsaCalKg
        )

        // ============================================================
        // 4. RESULTADO FINAL
        // ============================================================
        return ResultadoMuro(
            areaNetaM2 = areaNeta,

            // Ladrillos
            cantidadLadrillos = cantidadRealLadrillos,
            porcentajeDesperdicioLadrillos = desperdicioLadrillos,

            // Mortero (Viene del motor)
            morteroM3 = volumenMorteroGeo * (1 + desperdicioMortero),
            cementoKg = matsMortero.cementoKg,
            calKg = matsMortero.calKg,
            arenaTotalM3 = matsMortero.arenaM3,
            aguaLitros = matsMortero.aguaLitros,
            porcentajeDesperdicioMortero = desperdicioMortero,

            // Configuración
            proporcionMezcla = dosis.dosificacionMezcla,
            bolsaCementoKg = bolsaCementoKg,
            bolsaCalKg = bolsaCalKg
        )
    }
}