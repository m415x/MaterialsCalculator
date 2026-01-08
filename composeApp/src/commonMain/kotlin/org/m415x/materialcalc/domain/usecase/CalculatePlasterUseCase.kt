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

import org.m415x.materialcalc.data.repository.StaticMaterialRepository
import org.m415x.materialcalc.domain.common.WasteRegistry
import org.m415x.materialcalc.domain.common.calculateWetMaterials
import org.m415x.materialcalc.domain.model.Aperture
import org.m415x.materialcalc.domain.model.MortarDosing
import org.m415x.materialcalc.domain.model.ResultadoRevoque
import org.m415x.materialcalc.domain.utils.calculateNetSurface

/**
 * Calcula los materiales para un volumen de hormigón.
 *
 * @param repository Repositorio de materiales.
 */
class CalculatePlasterUseCase(private val repository: StaticMaterialRepository) {

    /**
     * Calcula los materiales para un volumen de hormigón.
     *
     * @param largoParedMetros Largo de la pared en metros.
     * @param altoParedMetros Alto de la pared en metros.
     * @param espesorGruesoMetros Espesor del revoque grueso en metros.
     * @param espesorFinoMetros Espesor del revoque fino en metros.
     * @param isAmbasCaras Indica si se calcula para ambas caras.
     * @param bolsaCementoKg Peso de la bolsa de cemento en kg.
     * @param bolsaCalKg Peso de la bolsa de cal en kg.
     * @param bolsaFinoPremezclaKg Peso de la bolsa de fino premezcla en kg.
     * @return Resultado del cálculo.
     */
    operator fun invoke(
        largoParedMetros: Double,
        altoParedMetros: Double,
        espesorGruesoMetros: Double,
        espesorFinoMetros: Double,
        isAmbasCaras: Boolean,
        aberturas: List<Aperture>,
        recetaGrueso: MortarDosing,
        bolsaCementoKg: Int = 25,
        bolsaCalKg: Int = 25,
        bolsaFinoPremezclaKg: Int = 25,
        porcentajeDesperdicio: Double,
    ): ResultadoRevoque {

        // ============================================================
        // 1. GEOMETRÍA (ÁREA NETA)
        // ============================================================
        // Ahora esto incluye las validaciones automáticamente
        val superficieNetaUnaCara = calculateNetSurface(
            largo = largoParedMetros,
            alto = altoParedMetros,
            aberturas = aberturas
        )

        // 4. Superficie Total (Aplicamos si son ambas caras)
        // Si hay ventana, se descuenta de ambos lados, así que la lógica se mantiene:
        // (Pared - Ventana) * 2 lados
        val superficieTotalCalculo = if (isAmbasCaras) superficieNetaUnaCara * 2 else superficieNetaUnaCara

        // ----------------------------------------------------
        // CÁLCULO DE REVOQUE GRUESO (JAHARRO)
        // ----------------------------------------------------
        val volumenGruesoGeo = superficieTotalCalculo * espesorGruesoMetros

        val matsGrueso = calculateWetMaterials(
            volumeM3 = volumenGruesoGeo,
            recipe = recetaGrueso,
            waste = porcentajeDesperdicio,
            cementBagWeight = bolsaCementoKg,
            limeBagWeight = bolsaCalKg
        )

        // ----------------------------------------------------
        // CÁLCULO DE REVOQUE FINO (ENLUCIDO)
        // ----------------------------------------------------
        val volumenFinoGeo = superficieTotalCalculo * espesorFinoMetros
        val desperdicioFino = WasteRegistry.getForRevoqueFino()

        // Opción 1: Premezcla (Rendimiento ~2.5 kg/m2)
        val consumoBasePremezcla = superficieTotalCalculo * 2.5
        val finoPremezclaTotal = consumoBasePremezcla * (1 + desperdicioFino)

        // Opción 2: Tradicional
        val recetaFino = repository.getFinePlasterRecipe()
        val matsFino = calculateWetMaterials(
            volumeM3 = volumenFinoGeo,
            recipe = recetaFino,
            waste = desperdicioFino,
            limeBagWeight = bolsaCalKg,
            cementBagWeight = 25
        )

        return ResultadoRevoque(
            areaTotalM2 = superficieTotalCalculo, // Área real a cubrir

            bolsaCementoKg = bolsaCementoKg,
            bolsaCalKg = bolsaCalKg,
            bolsaFinoPremezclaKg = bolsaFinoPremezclaKg,

            volumenGruesoM3 = volumenGruesoGeo * (1 + porcentajeDesperdicio),
            gruesoCementoKg = matsGrueso.cementoKg,
            gruesoCalKg = matsGrueso.calKg,
            gruesoArenaM3 = matsGrueso.arenaM3,
            porcentajeDesperdicioGrueso = porcentajeDesperdicio,
            dosificacionGrueso = recetaGrueso.mixingRatio,

            finoPremezclaKg = finoPremezclaTotal,
            finoCalKg = matsFino.calKg,
            finoArenaM3 = matsFino.arenaM3,
            porcentajeDesperdicioFino = desperdicioFino,
            dosificacionFino = recetaFino.mixingRatio
        )
    }
}