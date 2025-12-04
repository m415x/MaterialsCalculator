package org.m415x.materialscalculator.domain.usecase

import org.m415x.materialscalculator.data.repository.StaticMaterialRepository
import org.m415x.materialscalculator.domain.common.cmToMeters
import org.m415x.materialscalculator.domain.model.ResultadoRevoque
import kotlin.math.ceil

class CalculatePlasterUseCase(private val repository: StaticMaterialRepository) {

    operator fun invoke(
        largoParedMetros: Double,
        altoParedMetros: Double,
        espesorGruesoCm: Double, // Input en cm (default 2.0 en UI)
        aplicarEnAmbasCaras: Boolean
    ): ResultadoRevoque {

        // 1. Calcular Superficie
        val superficieBase = largoParedMetros * altoParedMetros
        val superficieTotal = if (aplicarEnAmbasCaras) superficieBase * 2 else superficieBase

        // --- CÁLCULO GRUESO ---
        // Convertimos espesor a metros (ej 2.0 cm -> 0.02 m)
        val espesorGruesoM = espesorGruesoCm.cmToMeters

        // Volumen + 10% Desperdicio (El revoque siempre se cae un poco)
        val volumenGrueso = (superficieTotal * espesorGruesoM) * 1.10

        val recetaG = repository.getRecetaGrueso()

        val gCemento = ceil((volumenGrueso * recetaG.cementoKg) / 50.0).toInt() // Bolsa 50kg
        val gCal = ceil((volumenGrueso * recetaG.calKg) / 25.0).toInt()         // Bolsa 25kg
        val gArena = volumenGrueso * recetaG.arenaM3


        // --- CÁLCULO FINO ---
        // Espesor fijo estándar: 3mm = 0.3 cm
        val espesorFinoM = 0.3.cmToMeters
        val volumenFino = (superficieTotal * espesorFinoM) * 1.15 // Más desperdicio en fino

        // Opción A: Premezcla (Rendimiento aprox: 10 a 12 m2 por bolsa de 25kg con 2-3mm espesor)
        // Usamos un conservador 10m2 por bolsa.
        val bolsasPremezcla = ceil(superficieTotal / 10.0).toInt()

        // Opción B: Tradicional (Materiales sueltos)
        val recetaF = repository.getRecetaFino()
        val fCal = ceil((volumenFino * recetaF.calKg) / 25.0).toInt()
        val fArena = volumenFino * recetaF.arenaM3

        return ResultadoRevoque(
            areaTotalM2 = superficieTotal,
            volumenGruesoM3 = volumenGrueso,
            gruesoCementoBolsas = gCemento,
            gruesoCalBolsas = gCal,
            gruesoArenaM3 = gArena,
            finoPremezclaBolsas = bolsasPremezcla,
            finoCalBolsas = fCal,
            finoArenaM3 = fArena
        )
    }
}