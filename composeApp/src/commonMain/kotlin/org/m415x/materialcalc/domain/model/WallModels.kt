/*
 * materialCalc
 * Copyright (C) 2026 M415X
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
 * Define los tipos de ladrillos que el usuario puede elegir. Todas las medidas en CENTIMETROS.
 *
 * @property brickName Nombre del ladrillo
 * @property isBearing Indica si el ladrillo es portante
 * @property description Descripción y usos del ladrillo
 */
enum class BrickType(
    val brickName: String,
    val isBearing: Boolean,
    val description: String
) {

    // Ladrillos macizos de arcilla
    COMUN("Ladrillo Común", true, "Muros, parrillas"),
    LADRILLON("Ladrillón", true, "Muros de carga"),

    // Huecos (Tabiquería / No Portantes) - Altura estándar 18cm
    HUECO_8("Cerámico Hueco 8", false, "Tabiquería interior"),
    HUECO_12("Cerámico Hueco 12", false, "Tabiquería interior/exterior"),
    HUECO_18("Cerámico Hueco 18", false, "Tabiquería, cerramientos"),

    // Portantes (Estructurales) - Altura estándar 19cm
    PORTANTE_12("Cerámico Portante 12", true, "Muros de carga"),
    PORTANTE_18("Cerámico Portante 18", true, "Muros de carga"),

    // Bloques de cemento/hormigón (Portantes y no portantes)
    BLOQUE_10("Bloque 10", false, "Tabiques, muros divisorios"),
    BLOQUE_13("Bloque 13", true, "Muros de carga"),
    BLOQUE_15("Bloque 15", true, "Muros de carga"),
    BLOQUE_20("Bloque 20", true, "Muros de carga")
}

/**
 * Propiedades físicas del ladrillo para el cálculo. Todas las medidas en METROS.
 *
 * @property width Ancho de la unidad en metros
 * @property height Alto de la unidad en metros
 * @property length Largo de la unidad en metros
 * @property gasketThickness Espesor de la junta en metros
 */
data class BrickProps(
    val width: Double,
    val height: Double,
    val length: Double,
    val gasketThickness: Double // Espesor de la junta
)

/**
 * Abertura en el muro. Todas las medidas en METROS.
 *
 * @property widthMeters Ancho de la abertura en metros
 * @property heightMeters Alto de la abertura en metros
 * @property quantity Cantidad de aberturas
 * @property name Nombre descriptivo de la abertura
 */
data class Aperture(
    val widthMeters: Double,
    val heightMeters: Double,
    val quantity: Int = 1,
    val name: String
)

/**
 * Resultado del cálculo del muro.
 *
 * @property netAreaM2 Área neta del muro en metros cuadrados
 * @property quantityBricks Cantidad de ladrillos necesarios
 * @property percentageBrickWaste Porcentaje de desperdicio de ladrillos
 * @property mortarM3 Volumen de mortero en metros cúbicos
 * @property cementKg Kilogramos de cemento
 * @property limeKg Kilogramos de cal
 * @property sandM3 Volumen total de arena en metros cúbicos
 * @property waterLiters Litros de agua
 * @property percentageMortarWaste Porcentaje de desperdicio de mortero
 * @property mixingRatio Proporción de la mezcla
 * @property cementBagKg Cantidad de bolsas de cemento
 * @property limeBagKg Cantidad de bolsas de cal
 */
data class WallResult(
    val netAreaM2: Double,
    // Ladrillos
    val quantityBricks: Int,
    val percentageBrickWaste: Double,
    // Mortero
    val mortarM3: Double,
    val cementKg: Double,
    val limeKg: Double,
    val sandM3: Double,
    val waterLiters: Double,
    val percentageMortarWaste: Double,
    // Configuración
    val mixingRatio: String,
    val cementBagKg: Int,
    val limeBagKg: Int
)