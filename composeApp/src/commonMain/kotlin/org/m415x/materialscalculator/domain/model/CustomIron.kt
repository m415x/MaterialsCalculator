package org.m415x.materialscalculator.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class CustomIron(
    val id: String,
    val nombre: String,      // Ej: "Hierro del 8 (Especial)"
    val diametroMm: Int,     // 8, 10, 12...
    val pesoPorMetro: Double // Kg/m
)