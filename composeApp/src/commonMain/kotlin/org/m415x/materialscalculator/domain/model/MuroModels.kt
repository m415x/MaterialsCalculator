package org.m415x.materialscalculator.domain.model

/**
 * Define los tipos de ladrillos que el usuario puede elegir.
 */
enum class TipoLadrillo {
    COMUN,          // 5x12x25 cm (aprox)
    LADRILLON,      // 18x12x25 cm
    CERAMICO_8,     // 8x18x33 cm
    CERAMICO_12,    // 12x18x33 cm
    CERAMICO_18,    // 18x18x33 cm
    BLOQUE_10,      // 9x19x39 cm
    BLOQUE_15,      // 13x19x39 cm
    BLOQUE_20       // 19x19x39 cm
}

/**
 * Propiedades físicas del ladrillo para el cálculo.
 * Todas las medidas en METROS.
 */
data class PropiedadesLadrillo(
    val anchoMuro: Double,
    val altoUnidad: Double,
    val largoUnidad: Double,
    val espesorJunta: Double
)

/**
 *
 */
data class Abertura(
    val anchoMetros: Double,
    val altoMetros: Double
)

/**
 *
 */
data class DosificacionMortero(
    val cementoKg: Double, // Kg de cemento por m3 de mortero
    val calKg: Double,     // Kg de cal por m3 de mortero (0.0 si es para bloques)
    val arenaM3: Double    // m3 de arena por m3 de mortero
)

/**
 *
 */
data class ResultadoMuro(
    val areaNetaM2: Double,
    val cantidadLadrillos: Int,
    val morteroM3: Double,
    val cementoBolsas: Int,
    val calBolsas: Int, // Asumiremos bolsas de 25kg o 30kg para cal
    val arenaTotalM3: Double
)