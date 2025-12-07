package org.m415x.materialscalculator.domain.model

/**
 * Empaqueta los resultados de forma ordenada.
 *
 * @property areaTotalM2 Superficie total (x1 o x2 caras)
 * @property bolsaCementoKg Cantidad de bolsas de cemento
 * @property bolsaCalKg Cantidad de bolsas de cal
 * @property bolsaFinoPremezclaKg Cantidad de bolsas de premezcla fina
 * @property volumenGruesoM3 Volumen grueso en metros cúbicos
 * @property gruesoCementoKg Cantidad de cemento grueso en kilogramos
 * @property gruesoCalKg Cantidad de cal gruesa en kilogramos
 * @property gruesoArenaM3 Cantidad de arena gruesa en metros cúbicos
 * @property porcentajeDesperdicioGrueso Porcentaje de desperdicio grueso
 * @property dosificacionGrueso Proporcion grueso
 * @property finoPremezclaKg Cantidad de premezcla fina en kilogramos
 * @property finoCalKg Cantidad de cal fina en kilogramos
 * @property finoArenaM3 Cantidad de arena fina en metros cúbicos
 * @property porcentajeDesperdicioFino Porcentaje de desperdicio fino
 * @property dosificacionFino Proporcion fina
 */
data class ResultadoRevoque(
    val areaTotalM2: Double, // Superficie total (x1 o x2 caras)
    val bolsaCementoKg: Int,
    val bolsaCalKg: Int,
    val bolsaFinoPremezclaKg: Int,
    // --- REVOQUE GRUESO (Jaharro) ---
    val volumenGruesoM3: Double,
    val gruesoCementoKg: Double,
    val gruesoCalKg: Double,
    val gruesoArenaM3: Double,
    val porcentajeDesperdicioGrueso: Double,
    val dosificacionGrueso: String,
    // --- REVOQUE FINO (Enlucido) ---
    // Opción 1: Premezclado (Bolsa lista)
    val finoPremezclaKg: Double,
    // Opción 2: Tradicional (A la cal)
    val finoCalKg: Double,
    val finoArenaM3: Double, // Arena voladora/fina
    val porcentajeDesperdicioFino: Double,
    val dosificacionFino: String
)