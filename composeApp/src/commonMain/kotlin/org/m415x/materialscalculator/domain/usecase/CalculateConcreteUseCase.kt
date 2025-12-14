package org.m415x.materialscalculator.domain.usecase

import org.m415x.materialscalculator.domain.common.calculateWetMaterials
import org.m415x.materialscalculator.domain.model.DosificacionHormigon
import org.m415x.materialscalculator.domain.model.ResultadoHormigon

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
        receta: DosificacionHormigon,
        pesoBolsaCementoKg: Int,
        pesoBolsaCalKg: Int,
        porcentajeDesperdicio: Double
    ): ResultadoHormigon {

        // 1. Geometría (Esta es la única responsabilidad única de este UseCase)
        val volumenGeometrico = anchoMetros * largoMetros * espesorMetros

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
            dosificacionMezcla = receta.proporcionMezcla
        )
    }
}