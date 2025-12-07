package org.m415x.materialscalculator.domain.model

/**
 * Define los tipos de ladrillos que el usuario puede elegir. Todas las medidas en CENTIMETROS.
 *
 * @property nombre Nombre del ladrillo
 * @property isPortante Indica si el ladrillo es portante
 * @property usos Usos del ladrillo
 */
enum class TipoLadrillo(val nombre: String, val isPortante: Boolean, val usos: String) {
    // Ladrillos macizos de arcilla
    COMUN("Común", true, "Muros, parrillas"),
    LADRILLON("Ladrillón", true, "Muros de carga"),

    // Huecos (Tabiquería / No Portantes) - Altura estándar 18cm
    HUECO_8("Cerámico Hueco 8", false, "Tabiquería interior"),
    HUECO_12("Cerámico Hueco 12", false, "Tabiquería interior/exterior"),
    HUECO_18("Cerámico Hueco 18", false, "Tabiquería, cerramientos"),

    // Portantes (Estructurales) - Altura estándar 19cm
    PORTANTE_12("Cerámico Portante 12", true, "Muros de carga"),
    PORTANTE_18("Cerámico Portante 18", true, "Muros de carga"),

    // Bloques de cemento/hormigón (Portantes y no portantes)
    BLOQUE_10("Bloque 10", false, "Tabiques, muros divisorios"),
    BLOQUE_13("Bloque 13", true, "Muros de carga"),
    BLOQUE_15("Bloque 15", true, "Muros de carga"),
    BLOQUE_20("Bloque 20", true, "Muros de carga")
}

/**
 * Propiedades físicas del ladrillo para el cálculo. Todas las medidas en METROS.
 *
 * @property anchoMuro Ancho del muro en metros
 * @property altoUnidad Alto de la unidad en metros
 * @property largoUnidad Largo de la unidad en metros
 * @property espesorJunta Espesor de la junta en metros
 */
data class PropiedadesLadrillo(
    val anchoMuro: Double,
    val altoUnidad: Double,
    val largoUnidad: Double,
    val espesorJunta: Double
)

/**
 * Abertura en el muro. Todas las medidas en METROS.
 *
 * @property anchoMetros Ancho de la abertura en metros
 * @property altoMetros Alto de la abertura en metros
 * @property cantidad Cantidad de aberturas
 * @property nombre Nombre descriptivo de la abertura
 */
data class Abertura(
    val anchoMetros: Double,
    val altoMetros: Double,
    val cantidad: Int = 1, // Por defecto 1
    val nombre: String = "Abertura" // Nombre descriptivo
)

/**
 * Resultado del cálculo del muro.
 *
 * @property areaNetaM2 Área neta del muro en metros cuadrados
 * @property cantidadLadrillos Cantidad de ladrillos necesarios
 * @property morteroM3 Volumen de mortero en metros cúbicos
 * @property cementoKg Kilogramos de cemento
 * @property calKg Kilogramos de cal
 * @property arenaTotalM3 Volumen total de arena en metros cúbicos
 * @property aguaLitros Litros de agua
 * @property bolsaCementoKg Cantidad de bolsas de cemento
 * @property bolsaCalKg Cantidad de bolsas de cal
 * @property proporcionMezcla Proporción de la mezcla
 * @property porcentajeDesperdicioLadrillos Porcentaje de desperdicio de ladrillos
 * @property porcentajeDesperdicioMortero Porcentaje de desperdicio de mortero
 */
data class ResultadoMuro(
    val areaNetaM2: Double,
    val cantidadLadrillos: Int,
    val morteroM3: Double,
    // Materia prima
    val cementoKg: Double,
    val calKg: Double,
    val arenaTotalM3: Double,
    val aguaLitros: Double,
    val bolsaCementoKg: Int,
    val bolsaCalKg: Int,
    // Proporción
    val proporcionMezcla: String,
    // Desperdicio
    val porcentajeDesperdicioLadrillos: Double,
    val porcentajeDesperdicioMortero: Double
)