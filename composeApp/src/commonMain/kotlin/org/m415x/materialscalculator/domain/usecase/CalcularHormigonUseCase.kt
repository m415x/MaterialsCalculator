package org.m415x.materialscalculator.domain.usecase

import org.m415x.materialscalculator.domain.repository.MaterialRepository
import org.m415x.materialscalculator.domain.model.*
import kotlin.math.ceil

/**
 * Calcula los materiales para un volumen de hormigón.
 */
class CalcularHormigonUseCase(private val repository: MaterialRepository) {

    // Función 'invoke' permite llamar a la clase como si fuera una función
    operator fun invoke(
        ancho: Double,
        alto: Double,
        espesor: Double,
        tipo: TipoHormigon,
        pesoBolsaCementoKg: Double = 25.0 // Asumimos bolsas de 25kg
    ): ResultadoHormigon {

        val dosificacion = repository.getDosificacionHormigon(tipo)
            ?: throw IllegalArgumentException("Tipo no soportado")

        val volumen = ancho * alto * espesor

        val cementoTotalKg = volumen * dosificacion.cementoKg
        val arenaTotalM3 = volumen * dosificacion.arenaM3
        val piedraTotalM3 = volumen * dosificacion.piedraM3
        val aguaTotalLitros = cementoTotalKg * dosificacion.relacionAguaCemento

        // 4. Calcular bolsas de cemento (redondeando hacia arriba)
        val bolsasDeCemento = ceil(cementoTotalKg / pesoBolsaCementoKg).toInt()

        // 5. Devolver el resultado empaquetado
        return ResultadoHormigon(
            volumenTotalM3 = volumen,
            cementoBolsas = bolsasDeCemento,
            cementoKg = cementoTotalKg,
            arenaM3 = arenaTotalM3,
            piedraM3 = piedraTotalM3,
            aguaLitros = aguaTotalLitros
        )
    }
}