package org.m415x.materialscalculator.domain.model

import kotlinx.serialization.Serializable

/**
 * Representa un ladrillo creado por el usuario.
 * @Serializable permite convertirlo a JSON automáticamente.
 */
@Serializable
data class CustomBrick(
    val id: String,                  // Identificador único (usaremos UUID o Timestamp)
    val nombre: String,              // Ej: "Bloque San Juan"
    val ancho: Double,               // Metros
    val alto: Double,                // Metros
    val largo: Double,               // Metros
    val junta: Double,               // Metros (Espesor de mezcla sugerido)
    val isPortante: Boolean = false, // Por defecto false
    val descripcion: String = ""     // Por defecto vacío
)

// Extensión útil para convertir este ladrillo a las propiedades que usa el cálculo
fun CustomBrick.toProperties() = PropiedadesLadrillo(
    anchoMuro = this.ancho,
    altoUnidad = this.alto,
    largoUnidad = this.largo,
    espesorJunta = this.junta
)