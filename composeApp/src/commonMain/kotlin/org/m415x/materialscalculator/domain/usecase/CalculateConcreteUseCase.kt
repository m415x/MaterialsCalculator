package org.m415x.materialscalculator.domain.usecase

import org.m415x.materialscalculator.domain.common.WasteRegistry
import org.m415x.materialscalculator.domain.common.calculateWetMaterials
import org.m415x.materialscalculator.domain.repository.MaterialRepository
import org.m415x.materialscalculator.domain.model.*

/**
 * Calcula los materiales para un volumen de hormigón.
 *
 * @param repository Repositorio de materiales.
 */
class CalculateConcreteUseCase(private val repository: MaterialRepository) {

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
        tipo: TipoHormigon,
        pesoBolsaCementoKg: Int = 25,
        porcentajeDesperdicio: Double
    ): ResultadoHormigon {

        // 1. Geometría (Esta es la única responsabilidad única de este UseCase)
        val volumenGeometrico = anchoMetros * largoMetros * espesorMetros

        // 2. Datos
        val receta = repository.getDosificacionHormigon(tipo)
            ?: throw IllegalArgumentException("Tipo no soportado")

        // 3. El motor hace el cálculo
        val mats = calculateWetMaterials(
            volumenM3 = volumenGeometrico,
            receta = receta,
            desperdicio = porcentajeDesperdicio,
            pesoBolsaCemento = pesoBolsaCementoKg
        )

        // 4. Mapeo al resultado final
        return ResultadoHormigon(
            volumenTotalM3 = volumenGeometrico * (1 + porcentajeDesperdicio),
            cementoKg = mats.cementoKg,
            arenaM3 = mats.arenaM3,
            piedraM3 = mats.piedraM3,
            aguaLitros = mats.aguaLitros,
            bolsaCementoKg = pesoBolsaCementoKg,
            porcentajeDesperdicioHormigon = porcentajeDesperdicio,
            dosificacionMezcla = receta.dosificacionMezcla
        )
    }
}