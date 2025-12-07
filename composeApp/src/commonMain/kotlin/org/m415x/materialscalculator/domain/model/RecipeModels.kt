package org.m415x.materialscalculator.domain.model

/**
 * Interfaz común para cualquier mezcla húmeda
 *
 * @property cementoKg Cantidad de cemento en kilogramos
 * @property calKg Cantidad de cal en kilogramos
 * @property arenaM3 Cantidad de arena en metros cúbicos
 * @property piedraM3 Cantidad de piedra en metros cúbicos
 * @property relacionAgua Relación de agua con cemento
 */
interface MaterialRecipe {
    val cementoKg: Double
    val calKg: Double
    val arenaM3: Double
    val piedraM3: Double
    val relacionAgua: Double
}

/**
 * Contiene las constantes de materiales para 1 m³ de hormigón. (Valores promedio de tablas
 * estándar)
 *
 * @property dosificacionMezcla Proporcion de la mezcla
 * @property cementoKg Cantidad de cemento en kilogramos
 * @property arenaM3 Cantidad de arena en metros cúbicos
 * @property piedraM3 Cantidad de piedra en metros cúbicos
 * @property relacionAgua Relación de agua con cemento
 * @property calKg Cantidad de cal en kilogramos
 */
data class DosificacionHormigon(
    val dosificacionMezcla: String,
    override val cementoKg: Double,
    override val arenaM3: Double,
    override val piedraM3: Double,
    override val relacionAgua: Double,
    override val calKg: Double = 0.0
) : MaterialRecipe

/**
 * Contiene las constantes de materiales para 1 m³ de mortero. (Valores promedio de tablas estándar)
 *
 * @property dosificacionMezcla Proporcion de la mezcla
 * @property cementoKg Cantidad de cemento en kilogramos
 * @property calKg Cantidad de cal en kilogramos
 * @property arenaM3 Cantidad de arena en metros cúbicos
 * @property relacionAgua Relación de agua con cemento
 * @property piedraM3 Cantidad de piedra en metros cúbicos
 */
data class DosificacionMortero(
    val dosificacionMezcla: String,
    override val cementoKg: Double,
    override val calKg: Double,
    override val arenaM3: Double,
    override val relacionAgua: Double,
    override val piedraM3: Double = 0.0
) : MaterialRecipe
