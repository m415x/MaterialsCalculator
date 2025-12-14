package org.m415x.materialscalculator.domain.usecase

import org.m415x.materialscalculator.data.repository.StaticMaterialRepository
import org.m415x.materialscalculator.domain.common.WasteRegistry
import org.m415x.materialscalculator.domain.common.calculateWetMaterials
import org.m415x.materialscalculator.domain.model.ResultadoRevoque
import kotlin.Double

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
        bolsaCementoKg: Int = 25,
        bolsaCalKg: Int = 25,
        bolsaFinoPremezclaKg: Int = 25,
        porcentajeDesperdicio: Double,
    ): ResultadoRevoque {

        // 1. Geometría Base
        val superficieBase = largoParedMetros * altoParedMetros
        val superficieTotal = if (isAmbasCaras) superficieBase * 2 else superficieBase

        // ----------------------------------------------------
        // CÁLCULO DE REVOQUE GRUESO (JAHARRO)
        // ----------------------------------------------------
        // A. Volumen Geométrico (Sin desperdicio aún)
        val volumenGruesoGeo = superficieTotal * espesorGruesoMetros

        // B. Datos del Repo y Registro
        val recetaGrueso = repository.getRecetaGrueso()

        // C. El motor calcula todo (cemento, cal, arena)
        val matsGrueso = calculateWetMaterials(
            volumenM3 = volumenGruesoGeo,
            receta = recetaGrueso,
            desperdicio = porcentajeDesperdicio,
            pesoBolsaCemento = bolsaCementoKg,
            pesoBolsaCal = bolsaCalKg
        )

        // ----------------------------------------------------
        // CÁLCULO DE REVOQUE FINO (ENLUCIDO)
        // ----------------------------------------------------
        // A. Volumen Geométrico
        val volumenFinoGeo = superficieTotal * espesorFinoMetros
        val desperdicioFino = WasteRegistry.getForRevoqueFino() // Centralizado (0.15)

        // B. Opción 1: Premezcla
        // Rendimiento base: ~2.5 kg/m2. Le sumamos el desperdicio del fino para ser consistentes.
        val consumoBasePremezcla = superficieTotal * 2.5
        val finoPremezclaTotal = consumoBasePremezcla * (1 + desperdicioFino)

        // C. Opción 2: Tradicional (Motor de Cálculo)
        val recetaFino = repository.getRecetaFino()

        val matsFino = calculateWetMaterials(
            volumenM3 = volumenFinoGeo,
            receta = recetaFino,
            desperdicio = desperdicioFino,
            pesoBolsaCal = bolsaCalKg,
            pesoBolsaCemento = 25
        )

        // ----------------------------------------------------
        // RESULTADO FINAL
        // ----------------------------------------------------
        return ResultadoRevoque(
            areaTotalM2 = superficieTotal,

            // Configuraciones de bolsas
            bolsaCementoKg = bolsaCementoKg,
            bolsaCalKg = bolsaCalKg,
            bolsaFinoPremezclaKg = bolsaFinoPremezclaKg,

            // Resultados Grueso (Vienen del objeto matsGrueso)
            volumenGruesoM3 = volumenGruesoGeo * (1 + porcentajeDesperdicio),
            gruesoCementoKg = matsGrueso.cementoKg,
            gruesoCalKg = matsGrueso.calKg,
            gruesoArenaM3 = matsGrueso.arenaM3,
            porcentajeDesperdicioGrueso = porcentajeDesperdicio,
            dosificacionGrueso = recetaGrueso.dosificacionMezcla, // Usamos la propiedad de la interfaz

            // Resultados Fino
            finoPremezclaKg = finoPremezclaTotal,

            // Resultados Fino Tradicional (Vienen del objeto matsFino)
            finoCalKg = matsFino.calKg,
            finoArenaM3 = matsFino.arenaM3,
            porcentajeDesperdicioFino = desperdicioFino,
            dosificacionFino = recetaFino.dosificacionMezcla
        )
    }
}