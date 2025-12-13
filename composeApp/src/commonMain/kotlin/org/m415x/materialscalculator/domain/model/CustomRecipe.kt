package org.m415x.materialscalculator.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class CustomRecipe(
    val id: String,
    val nombre: String,          // Ej: "H21 Reforzado"
    override val cementoKg: Double,       // por m3
    override val arenaM3: Double,         // por m3
    override val piedraM3: Double = 0.0,  // por m3 (0 si es mortero)
    override val calKg: Double = 0.0,     // por m3 (0 si es hormigón)
    override val relacionAgua: Double,    // Relación A/C
    val tipo: String             // "CONCRETE" o "MORTAR" (Para filtrar)
) : MaterialRecipe {
    // Propiedad 'proporcionMezcla' requerida por la interfaz
    // La generamos dinámicamente o la guardamos como campo
    val proporcionMezcla: String
    get() = "$nombre (Personalizado)"
}

