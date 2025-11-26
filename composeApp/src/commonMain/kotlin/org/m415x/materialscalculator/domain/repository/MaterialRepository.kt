package org.m415x.materialscalculator.domain.repository

import org.m415x.materialscalculator.domain.model.*

interface MaterialRepository {
    fun getDosificacionHormigon(tipo: TipoHormigon): DosificacionHormigon?
    fun getPropiedadesLadrillo(tipo: TipoLadrillo): PropiedadesLadrillo?
    fun getDosificacionMortero(tipo: TipoLadrillo): DosificacionMortero?
    fun getPesoHierroPorMetro(diametro: DiametroHierro): Double
}