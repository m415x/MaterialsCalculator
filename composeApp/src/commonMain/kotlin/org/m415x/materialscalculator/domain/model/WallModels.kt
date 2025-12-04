package org.m415x.materialscalculator.domain.model

/**
 * Define los tipos de ladrillos que el usuario puede elegir.
 */
enum class TipoLadrillo(val nombre: String) {
    COMUN("Común"),             // 5x12x25 cm (aprox)
    LADRILLON("Ladrillón"),     // 18x12x25 cm
    CERAMICO_8("Cerámico 8"),   // 8x18x33 cm
    CERAMICO_12("Cerámico 12"), // 12x18x33 cm
    CERAMICO_18("Cerámico 18"), // 18x18x33 cm
    BLOQUE_10("Bloque 10"),     // 9x19x39 cm
    BLOQUE_15("Bloque 15"),     // 13x19x39 cm
    BLOQUE_20("Bloque 20")      // 19x19x39 cm
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
    val altoMetros: Double,
    val cantidad: Int = 1,          // Por defecto 1
    val nombre: String = "Abertura" // Nombre descriptivo
)

/**
 *
 */
data class DosificacionMortero(
    val cementoKg: Double, // Kg de cemento por m3 de mortero
    val calKg: Double,     // Kg de cal por m3 de mortero (0.0 si es para bloques)
    val arenaM3: Double,    // m3 de arena por m3 de mortero
    val relacionAguaCemento: Double
)

/**
 *
 */
data class ResultadoMuro(
    val areaNetaM2: Double,
    val cantidadLadrillos: Int,
    val morteroM3: Double,
    val cementoBolsas: Int,
    val calBolsas: Int,
    val arenaTotalM3: Double,
    val aguaLitros: Double
)