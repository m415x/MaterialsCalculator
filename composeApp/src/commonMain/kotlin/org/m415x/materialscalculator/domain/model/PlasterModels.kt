package org.m415x.materialscalculator.domain.model

data class ResultadoRevoque(
    val areaTotalM2: Double, // Superficie total (x1 o x2 caras)

    // --- REVOQUE GRUESO (Jaharro) ---
    val volumenGruesoM3: Double,
    val gruesoCementoBolsas: Int,
    val gruesoCalBolsas: Int,
    val gruesoArenaM3: Double,

    // --- REVOQUE FINO (Enlucido) ---
    // Opción 1: Premezclado (Bolsa lista)
    val finoPremezclaBolsas: Int,

    // Opción 2: Tradicional (A la cal)
    val finoCalBolsas: Int,
    val finoArenaM3: Double // Arena voladora/fina
)