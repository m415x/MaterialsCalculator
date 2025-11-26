package org.m415x.materialscalculator.domain.model

/**
 * Define los tipos de hormigón que el usuario puede elegir.
 */
enum class TipoHormigon {
    H8,     // Resistencia 80 kg/cm²
    H13,    // Resistencia 130 kg/cm²
    H17,    // Resistencia 170 kg/cm²
    H21,    // Resistencia 210 kg/cm²
    H25,    // Resistencia 250 kg/cm²
    H30     // Resistencia 300 kg/cm²
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