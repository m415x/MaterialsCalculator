package org.m415x.materialscalculator.domain.model

/**
 * Enumeración que representa los diámetros estándar de hierro para estructuras.
 *
 * @property mm Valor en milímetros del diámetro
 */
enum class DiametroHierro(val nombre: String, val mm: Int) {
    HIERRO_6("Hierro Ø 6 mm", 6),
    HIERRO_8("Hierro Ø 8 mm", 8),
    HIERRO_10("Hierro Ø 10 mm", 10),
    HIERRO_12("Hierro Ø 12 mm", 12),
    HIERRO_16("Hierro Ø 16 mm", 16)
}

/**
 * Data class para el resultado de vigas/columnas.
 *
 * @property volumenHormigonM3 Volumen de hormigón en metros cúbicos
 * @property cementoKg Cantidad de cemento en kilogramos
 * @property arenaM3 Cantidad de arena en metros cúbicos
 * @property piedraM3 Cantidad de piedra en metros cúbicos
 * @property aguaLitros Cantidad de agua en litros
 * @property bolsaCementoKg Cantidad de bolsas de cemento
 * @property dosificacionHormigon Dosificación de hormigón
 * @property porcentajeDesperdicioHormigon Porcentaje de desperdicio de hormigón
 * @property diametroPrincipal Diámetro principal de hierro
 * @property diametroEstribo Diámetro de estribo de hierro
 * @property hierroPrincipalKg Cantidad de hierro principal en kilogramos
 * @property hierroEstribosKg Cantidad de hierro estribos en kilogramos
 * @property hierroPrincipalMetros Longitud total de hierro principal en metros
 * @property hierroEstribosMetros Longitud total de hierro estribos en metros
 * @property cantidadHierroPrincipal Cantidad de hierro principal a comprar (aprox)
 * @property cantidadHierroEstribos Cantidad de hierro estribos a comprar (aprox)
 * @property longitudComercialHierroMetros Longitud comercial de hierro en metros
 * @property porcentajeDesperdicioHierroPrincipal Porcentaje de desperdicio de hierro principal
 * @property porcentajeDesperdicioHierroEstribos Porcentaje de desperdicio de hierro estribos
 */
data class ResultadoEstructura(
    val volumenHormigonM3: Double,
    // Materiales Hormigón
    val cementoKg: Double,
    val arenaM3: Double,
    val piedraM3: Double,
    val aguaLitros: Double,
    val bolsaCementoKg: Int,
    val dosificacionHormigon: String,
    val porcentajeDesperdicioHormigon: Double,
    // Materiales Armadura
    val diametroPrincipal: DiametroHierro,
    val diametroEstribo: DiametroHierro,
    val hierroPrincipalKg: Double,
    val hierroEstribosKg: Double,
    val hierroPrincipalMetros: Double,
    val hierroEstribosMetros: Double,
    val cantidadHierroPrincipal: Int,
    val cantidadHierroEstribos: Int,
    val longitudComercialHierroMetros: Int,
    val porcentajeDesperdicioHierroPrincipal: Double,
    val porcentajeDesperdicioHierroEstribos: Double
)
