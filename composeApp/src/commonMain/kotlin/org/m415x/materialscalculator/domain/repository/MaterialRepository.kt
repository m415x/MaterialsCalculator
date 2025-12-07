package org.m415x.materialscalculator.domain.repository

import org.m415x.materialscalculator.domain.model.*

/**
 * Interfaz que define los métodos para obtener los datos de los materiales.
 */
interface MaterialRepository {
    /**
     * Obtiene la dosificación del hormigón para el tipo especificado.
     *
     * @param tipo Tipo de hormigón.
     * @return Dosificación del hormigón.
     */
    fun getDosificacionHormigon(tipo: TipoHormigon): DosificacionHormigon?

    /**
     * Obtiene las propiedades del ladrillo para el tipo especificado.
     *
     * @param tipo Tipo de ladrillo.
     * @return Propiedades del ladrillo.
     */
    fun getPropiedadesLadrillo(tipo: TipoLadrillo): PropiedadesLadrillo?

    /**
     * Obtiene la dosificación del mortero para el tipo de ladrillo especificado.
     *
     * @param tipo Tipo de ladrillo.
     * @return Dosificación del mortero.
     */
    fun getDosificacionMortero(tipo: TipoLadrillo): DosificacionMortero?

    /**
     * Obtiene el peso del hierro por metro para el diámetro especificado.
     *
     * @param diametro Diámetro del hierro.
     * @return Peso del hierro por metro.
     */
    fun getPesoHierroPorMetro(diametro: DiametroHierro): Double
}