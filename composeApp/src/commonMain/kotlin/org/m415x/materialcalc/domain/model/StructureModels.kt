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

package org.m415x.materialcalc.domain.model

/**
 * Enumeración que representa los tipos de estructuras.
 *
 * @property label Etiqueta para mostrar en la UI.
 */
enum class StructureType(val label: String) {
    BEAM("Viga"),
    COLUMN("Columna"),
    SLAB("Losa")
}

/**
 * Enumeración que representa los diámetros de hierro.
 *
 * @property mm Diámetro en milímetros.
 * @property pesoLinealKgM Peso lineal en kg/m.
 */
enum class IronDiameter(val mm: Double, val pesoLinealKgM: Double) {
    HIERRO_4_2(4.2, 0.109),
    HIERRO_6(6.0, 0.222),
    HIERRO_8(8.0, 0.395),
    HIERRO_10(10.0, 0.617),
    HIERRO_12(12.0, 0.888),
    HIERRO_16(16.0, 1.578),
    HIERRO_20(20.0, 2.466),
    HIERRO_25(25.0, 3.853)
}

/**
 * Representa el resultado del cálculo de una estructura.
 *
 * @property volumenHormigonM3 Volumen de hormigón en m³.
 * @property porcentajeDesperdicioHormigon Porcentaje de desperdicio de hormigón.
 * @property cementoKg Cantidad de cemento en kg.
 * @property bolsaCementoKg Peso de la bolsa de cemento.
 * @property arenaM3 Cantidad de arena en m³.
 * @property piedraM3 Cantidad de piedra en m³.
 * @property aguaLitros Cantidad de agua en litros.
 * @property diametroPrincipal Diámetro del hierro principal.
 * @property hierroPrincipalMetros Metros de hierro principal.
 * @property hierroPrincipalKg Kilos de hierro principal.
 * @property cantidadHierroPrincipal Cantidad de barras de hierro principal.
 * @property porcentajeDesperdicioHierroPrincipal Porcentaje de desperdicio de hierro principal.
 * @property diametroEstribo Diámetro del hierro de estribo.
 * @property hierroEstribosMetros Metros de hierro de estribo.
 * @property hierroEstribosKg Kilos de hierro de estribo.
 * @property cantidadHierroEstribos Cantidad de barras de hierro de estribo.
 * @property porcentajeDesperdicioHierroEstribos Porcentaje de desperdicio de hierro de estribo.
 */
data class ResultadoEstructura(
    // Hormigón
    val volumenHormigonM3: Double,
    val porcentajeDesperdicioHormigon: Double,
    val cementoKg: Double,
    val bolsaCementoKg: Int,
    val arenaM3: Double,
    val piedraM3: Double,
    val aguaLitros: Double,
    // Hierro Principal
    val diametroPrincipal: IronDiameter,
    val hierroPrincipalMetros: Double,
    val hierroPrincipalKg: Double,
    val cantidadHierroPrincipal: Int,
    val porcentajeDesperdicioHierroPrincipal: Double,
    // Estribos
    val diametroEstribo: IronDiameter,
    val hierroEstribosMetros: Double,
    val hierroEstribosKg: Double,
    val cantidadHierroEstribos: Int,
    val porcentajeDesperdicioHierroEstribos: Double
)