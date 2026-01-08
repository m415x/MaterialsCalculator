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
import org.m415x.materialcalc.domain.model.Aperture
import org.m415x.materialcalc.domain.model.BrickProps
import org.m415x.materialcalc.domain.model.MortarDosing
import org.m415x.materialcalc.domain.model.WallResult
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
        props: BrickProps,
        dosis: MortarDosing,
        aberturas: List<Aperture>,
        bolsaCementoKg: Int,
        bolsaCalKg: Int,
        desperdicioLadrillos: Double,
        desperdicioMortero: Double
    ): WallResult {

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
        val supLadrilloConJunta = (props.length + props.gasketThickness) * (props.height + props.gasketThickness)
        val ladrillosPorM2 = 1.0 / supLadrilloConJunta

        // Cantidad Teórica
        val totalLadrillosTeorico = areaNeta * ladrillosPorM2

        // Cantidad Real (con desperdicio)
        val cantidadRealLadrillos = ceil(totalLadrillosTeorico * (1 + desperdicioLadrillos)).toInt()

        // ============================================================
        // 3. CÁLCULO DE MORTERO (Mezcla Húmeda)
        // ============================================================

        // A. Volumen Geométrico del Muro (Área * Espesor)
        val volumenParedM3 = areaNeta * props.width

        // B. Volumen ocupado por Ladrillos (Sin desperdicio, ocupación física real)
        val volumenLadrillosSolidos = totalLadrillosTeorico * (props.length * props.height * props.width)

        // C. Volumen Geométrico de la Mezcla (Diferencia)
        val volumenMorteroGeo = (volumenParedM3 - volumenLadrillosSolidos).coerceAtLeast(0.0)

        // D. ¡MOTOR DE CÁLCULO! (Aplica desperdicio 15% y calcula materiales)
        val matsMortero = calculateWetMaterials(
            volumeM3 = volumenMorteroGeo,
            recipe = dosis,
            waste = desperdicioMortero,
            cementBagWeight = bolsaCementoKg,
            limeBagWeight = bolsaCalKg
        )

        // ============================================================
        // 4. RESULTADO FINAL
        // ============================================================
        return WallResult(
            netAreaM2 = areaNeta,

            // Ladrillos
            quantityBricks = cantidadRealLadrillos,
            percentageBrickWaste = desperdicioLadrillos,

            // Mortero (Viene del motor)
            mortarM3 = volumenMorteroGeo * (1 + desperdicioMortero),
            cementKg = matsMortero.cementoKg,
            limeKg = matsMortero.calKg,
            sandM3 = matsMortero.arenaM3,
            waterLiters = matsMortero.aguaLitros,
            percentageMortarWaste = desperdicioMortero,

            // Configuración
            mixingRatio = dosis.mixingRatio,
            cementBagKg = bolsaCementoKg,
            limeBagKg = bolsaCalKg
        )
    }
}