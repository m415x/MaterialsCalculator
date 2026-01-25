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

import materialscalculator.composeapp.generated.resources.*
import org.jetbrains.compose.resources.StringResource

/**
 * Define los tipos de ladrillos que el usuario puede elegir. Todas las medidas en CENTIMETROS.
 *
 * @property brickNameRes Recurso del nombre del ladrillo
 * @property isBearing Indica si el ladrillo es portante
 * @property descriptionRes Recurso de descripción y usos del ladrillo
 */
enum class BrickType(
    val brickNameRes: StringResource,
    val isBearing: Boolean,
    val descriptionRes: StringResource
) {

    // Ladrillos macizos de arcilla
    COMUN(Res.string.brick_name_comun, true, Res.string.brick_desc_comun),
    LADRILLON(Res.string.brick_name_ladrillon, true, Res.string.brick_desc_ladrillon),

    // Huecos (Tabiquería / No Portantes) - Altura estándar 18cm
    HUECO_8(Res.string.brick_name_hueco_8, false, Res.string.brick_desc_hueco_8),
    HUECO_12(Res.string.brick_name_hueco_12, false, Res.string.brick_desc_hueco_12),
    HUECO_18(Res.string.brick_name_hueco_18, false, Res.string.brick_desc_hueco_18),

    // Portantes (Estructurales) - Altura estándar 19cm
    PORTANTE_12(Res.string.brick_name_portante_12, true, Res.string.brick_desc_portante_12),
    PORTANTE_18(Res.string.brick_name_portante_18, true, Res.string.brick_desc_portante_18),

    // Bloques de cemento/hormigón (Portantes y no portantes)
    BLOQUE_10(Res.string.brick_name_bloque_10, false, Res.string.brick_desc_bloque_10),
    BLOQUE_13(Res.string.brick_name_bloque_13, true, Res.string.brick_desc_bloque_13),
    BLOQUE_15(Res.string.brick_name_bloque_15, true, Res.string.brick_desc_bloque_15),
    BLOQUE_20(Res.string.brick_name_bloque_20, true, Res.string.brick_desc_bloque_20)
}

/**
 * Propiedades físicas del ladrillo para el cálculo. Todas las medidas en METROS.
 *
 * @property id Identificador único del ladrillo (para buscar precios).
 * @property width Ancho de la unidad en metros
 * @property height Alto de la unidad en metros
 * @property length Largo de la unidad en metros
 * @property gasketThickness Espesor de la junta en metros
 */
data class BrickProps(
    val id: String, // Agregado para identificar el ladrillo en precios
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
 * @property materialCost Costo estimado de materiales.
 * @property laborCost Costo estimado de mano de obra.
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
    val mixingRatio: TextSource,
    val cementBagKg: Int,
    val limeBagKg: Int,
    // Costos
    val materialCost: Double = 0.0,
    val laborCost: Double = 0.0
)
