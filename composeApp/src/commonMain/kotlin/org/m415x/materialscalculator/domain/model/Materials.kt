package org.m415x.materialscalculator.domain.model

// ==================================================
// --- HORMIGONES ---
// ==================================================
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

// ==================================================
// --- LADRILLOS ---
// ==================================================
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

// ==================================================
// --- VIGAS Y COLUMNAS ---
// ==================================================
enum class DiametroHierro(val mm: Int) {
    HIERRO_6(6),
    HIERRO_8(8),
    HIERRO_10(10),
    HIERRO_12(12),
    HIERRO_16(16)
}

// Data class para el resultado de vigas/columnas
data class ResultadoEstructura(
    val volumenHormigonM3: Double,
    // Materiales Hormigón
    val cementoBolsas: Int,
    val arenaM3: Double,
    val piedraM3: Double,
    // Materiales Armadura
    val hierroPrincipalKg: Double, // Peso total hierros largos
    val hierroEstribosKg: Double,  // Peso total estribos
    val cantidadBarrasAcero: Int   // Cantidad de barras de 12m a comprar (aprox)
)