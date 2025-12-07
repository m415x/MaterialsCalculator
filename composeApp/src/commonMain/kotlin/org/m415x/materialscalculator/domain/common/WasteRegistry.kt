package org.m415x.materialscalculator.domain.common

import org.m415x.materialscalculator.domain.model.TipoHormigon

/**
 * Registro único de porcentajes de desperdicio.
 * Retorna valores decimales (ej: 0.10 para 10%)
 *
 * @property WasteRegistry Registro único de porcentajes de desperdicio.
 */
object WasteRegistry {

    // --- HORMIGONES ---
    /**
     * Retorna el porcentaje de desperdicio para el hormigón.
     *
     * @param tipo Tipo de hormigón.
     * @return Porcentaje de desperdicio.
     */
    fun getForConcrete(tipo: TipoHormigon): Double = when (tipo) {
        TipoHormigon.H8 -> 0.10  // 10% (Suelo irregular)
        TipoHormigon.H13 -> 0.08
        TipoHormigon.H17 -> 0.07
        else -> 0.05             // 5% (Estructurales H21, H25, H30 suelen tener buen encofrado)
    }

    // --- HIERROS ---
    /**
     * Retorna el porcentaje de desperdicio para el hierro.
     *
     * @param isEstribo Indica si es estribo.
     * @return Porcentaje de desperdicio.
     */
    fun getForIron(isEstribo: Boolean): Double {
        return if (isEstribo) {
            0.05 // 5% para estribos (se aprovechan recortes pequeños)
        } else {
            0.10 // 10% para hierros largos (empalmes y puntas)
        }
    }

    // --- ALBAÑILERÍA (Morteros y Revoques) ---
    /**
     * Retorna el porcentaje de desperdicio para el mortero.
     *
     * @return Porcentaje de desperdicio.
     */
    fun getForMorteroAsiento(): Double = 0.15 // Se cae mucho al pegar ladrillos

    /**
     * Retorna el porcentaje de desperdicio para el ladrillo.
     *
     * @return Porcentaje de desperdicio.
     */
    fun getForLadrillos(): Double = 0.05      // 5% (Roturas al cortar/trasladar)

    /**
     * Retorna el porcentaje de desperdicio para el revoque grueso.
     *
     * @return Porcentaje de desperdicio.
     */
    fun getForRevoqueGrueso(): Double = 0.10

    /**
     * Retorna el porcentaje de desperdicio para el revoque fino.
     *
     * @return Porcentaje de desperdicio.
     */
    fun getForRevoqueFino(): Double = 0.15    // Merma al fratachar/fieltrar
}