package org.m415x.materialscalculator.ui.common

import org.m415x.materialscalculator.domain.model.TipoLadrillo
import kotlin.math.round

/*
 * Función extendida para redondear Doubles fácilmente en toda la app
 */
fun Double.roundToDecimals(decimals: Int): String {
    var multiplier = 1.0
    repeat(decimals) { multiplier *= 10 }
    val rounded = round(this * multiplier) / multiplier
    return rounded.toString()
}

/*
 * Función auxiliar para formatear nombre (ej. CERAMICO_12 -> Cerámico 12)
 */
fun formatLadrilloName(tipo: TipoLadrillo): String {
    return tipo.name
        .replace("_", " ")
        .lowercase()
        .replaceFirstChar { it.uppercase() }
}