/*
 * materialCalc
 * Copyright (C) 2025 M415X
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.m415x.materialcalc.domain.repository

import org.m415x.materialcalc.domain.model.*

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