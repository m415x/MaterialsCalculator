package org.m415x.materialscalculator.domain.model

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
    val aguaLitros: Double,
    // Materiales Armadura
    val diametroPrincipal: DiametroHierro,
    val diametroEstribo: DiametroHierro,
    val hierroPrincipalKg: Double, // Peso total hierros largos
    val hierroEstribosKg: Double,  // Peso total estribos
    val hierroPrincipalMetros: Double, // Metros totales hierros largos
    val hierroEstribosMetros: Double,  // Metros totales estribos
    val cantidadHierroPrincipal: Int,   // Cantidad de barras de 12m a comprar (aprox)
    val cantidadHierroEstribos: Int    // Cantidad de barras de 12m a comprar (aprox)
)