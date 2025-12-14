package org.m415x.materialscalculator.ui.common

import org.m415x.materialscalculator.domain.model.DosificacionMortero
import org.m415x.materialscalculator.domain.model.PropiedadesLadrillo

// Modelo auxiliar para el Dropdown (Mantenlo privado o dentro del archivo)
data class LadrilloOption(
    val id: String,
    val label: String,
    val isPortante: Boolean,
    val descripcion: String,
    val props: PropiedadesLadrillo,
    val receta: DosificacionMortero // Receta asociada/sugerida
)

// Modelo auxiliar para la lista de mezclas
data class MezclaOption(
    val id: String,
    val nombre: String,
    val descripcion: String, // Ej: "1:3 (Cem:Arena)"
    val data: DosificacionMortero
)