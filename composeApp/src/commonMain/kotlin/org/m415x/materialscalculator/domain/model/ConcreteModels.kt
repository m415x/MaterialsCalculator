package org.m415x.materialscalculator.domain.model

import org.m415x.materialscalculator.domain.usecase.CalculateWallUseCase

/**
 * Define los tipos de hormigón que el usuario puede elegir.
 */
enum class TipoHormigon(
    val endurance: String,
    val uses: String,
    val esAptoEstructura: Boolean
) {
    H8(
        endurance = "Resistencia 80 kg/cm²",
        uses = "Limpieza y rellenos (pobre)",
        esAptoEstructura = false
    ),
    H13(
        endurance = "Resistencia 130 kg/cm²",
        uses = "Cimientos y contrapisos",
        esAptoEstructura = false
    ),
    H17(
        endurance = "Resistencia 170 kg/cm²",
        uses = "Losas y columnas ligeras",
        esAptoEstructura = true
    ),
    H21(
        endurance = "Resistencia 210 kg/cm²",
        uses = "Vigas, losas y estructuras estándar",
        esAptoEstructura = true
    ),
    H25(
        endurance = "Resistencia 250 kg/cm²",
        uses = "Estructuras de alta carga",
        esAptoEstructura = true
    ),
    H30(
        endurance = "Resistencia 300 kg/cm²",
        uses = "Pavimentos y grandes estructuras",
        esAptoEstructura = true
    )
}

/**
 * Contiene las constantes de materiales para 1 m³ de hormigón.
 * (Valores promedio de tablas estándar)
 */
data class DosificacionHormigon(
    val cementoKg: Double,
    val arenaM3: Double,
    val piedraM3: Double,
    val relacionAguaCemento: Double
)

/**
 * Un 'data class' para empaquetar los resultados de forma ordenada.
 */
data class ResultadoHormigon(
    val volumenTotalM3: Double,
    val cementoBolsas: Int,     // Bolsas (redondeado hacia arriba)
    val cementoKg: Double,      // Cemento en Kg
    val arenaM3: Double,        // Arena en m³
    val piedraM3: Double,       // Piedra (árido grueso) en m³
    val aguaLitros: Double
)