package org.m415x.materialscalculator.domain.model

/**
 * Define los tipos de hormigón que el usuario puede elegir.
 *
 * @property resistencia Resistencia del hormigón en kg/cm².
 * @property usos Usos del hormigón.
 * @property isAptoEstructura Indica si el hormigón es apto para estructuras.
 */
enum class TipoHormigon(val resistencia: String, val usos: String, val isAptoEstructura: Boolean) {
    H8(
        resistencia = "Resistencia 80 kg/cm²",
        usos = "Limpieza y rellenos (pobre)",
        isAptoEstructura = false
    ),
    H13(
        resistencia = "Resistencia 130 kg/cm²",
        usos = "Cimientos y contrapisos",
        isAptoEstructura = false
    ),
    H17(
        resistencia = "Resistencia 170 kg/cm²",
        usos = "Losas y columnas ligeras",
        isAptoEstructura = true
    ),
    H21(
        resistencia = "Resistencia 210 kg/cm²",
        usos = "Vigas, losas y estructuras estándar",
        isAptoEstructura = true
    ),
    H25(
        resistencia = "Resistencia 250 kg/cm²",
        usos = "Estructuras de alta carga",
        isAptoEstructura = true
    ),
    H30(
        resistencia = "Resistencia 300 kg/cm²",
        usos = "Pavimentos y grandes estructuras",
        isAptoEstructura = true
    )
}

/**
 * Empaqueta los resultados de forma ordenada.
 *
 * @property volumenTotalM3 Volumen total en metros cúbicos.
 * @property cementoKg Cantidad de cemento en kilogramos.
 * @property arenaM3 Cantidad de arena en metros cúbicos.
 * @property piedraM3 Cantidad de piedra en metros cúbicos.
 * @property aguaLitros Cantidad de agua en litros.
 * @property bolsaCementoKg Cantidad de bolsas de cemento.
 * @property porcentajeDesperdicioHormigon Porcentaje de desperdicio.
 * @property dosificacionMezcla Dosificación de la mezcla.
 */
data class ResultadoHormigon(
    val volumenTotalM3: Double,
    val cementoKg: Double, // Cemento en Kg
    val arenaM3: Double, // Arena en m³
    val piedraM3: Double, // Piedra (árido grueso) en m³
    val aguaLitros: Double,
    val bolsaCementoKg: Int,
    val porcentajeDesperdicioHormigon: Double,
    val dosificacionMezcla: String
)