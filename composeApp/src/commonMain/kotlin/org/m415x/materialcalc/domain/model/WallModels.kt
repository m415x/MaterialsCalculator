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
 * Familia de ladrillos según su comportamiento estructural y material.
 */
enum class BrickFamily(
    val familyName: StringResource,
    val description: StringResource
) {
    SOLID_BRICK(
        Res.string.brick_family_solid_name,
        Res.string.brick_family_solid_desc
    ), // Macizo Común
    LOAD_BEARING_HOLLOW_BRICK(
        Res.string.brick_family_load_bearing_hollow_name,
        Res.string.brick_family_load_bearing_hollow_desc
    ), // Hueco portante
    NON_LOAD_BEARING_HOLLOW_BRICK(
        Res.string.brick_family_non_load_bearing_hollow_name,
        Res.string.brick_family_non_load_bearing_hollow_desc
    ), // Hueco No portante
    CONCRETE_BLOCK(
        Res.string.brick_family_concrete_block_name,
        Res.string.brick_family_concrete_block_desc
    ), // Block de Hormigón
    RETAK(
        Res.string.brick_family_retak_name,
        Res.string.brick_family_retak_desc
    ) // Hormigón celular (HCCA)
}

/**
 * Define los tipos de ladrillos que el usuario puede elegir. Todas las medidas en CENTIMETROS.
 *
 * @property brickNameRes Recurso del nombre del ladrillo
 * @property isBearing Indica si el ladrillo es portante
 * @property descriptionRes Recurso de descripción y usos del ladrillo
 * @property family Familia del ladrillo
 */
enum class BrickType(
    val brickNameRes: StringResource,
    val isBearing: Boolean,
    val descriptionRes: StringResource,
    val family: BrickFamily
) {

    // Ladrillos macizos de arcilla
    COMUN(
        Res.string.brick_name_comun,
        true,
        Res.string.brick_desc_comun,
        BrickFamily.SOLID_BRICK
    ),
    LADRILLON(
        Res.string.brick_name_ladrillon,
        true,
        Res.string.brick_desc_ladrillon,
        BrickFamily.SOLID_BRICK
    ),

    // Huecos (Tabiquería / No Portantes) - Altura estándar 18cm
    HUECO_8(
        Res.string.brick_name_hueco_8,
        false,
        Res.string.brick_desc_hueco_8,
        BrickFamily.NON_LOAD_BEARING_HOLLOW_BRICK
    ),
    HUECO_12(
        Res.string.brick_name_hueco_12,
        false,
        Res.string.brick_desc_hueco_12,
        BrickFamily.NON_LOAD_BEARING_HOLLOW_BRICK
    ),
    HUECO_18(
        Res.string.brick_name_hueco_18,
        false,
        Res.string.brick_desc_hueco_18,
        BrickFamily.NON_LOAD_BEARING_HOLLOW_BRICK
    ),

    // Portantes (Estructurales) - Altura estándar 19cm
    PORTANTE_12(
        Res.string.brick_name_portante_12,
        true,
        Res.string.brick_desc_portante_12,
        BrickFamily.LOAD_BEARING_HOLLOW_BRICK
    ),
    PORTANTE_18(
        Res.string.brick_name_portante_18,
        true,
        Res.string.brick_desc_portante_18,
        BrickFamily.LOAD_BEARING_HOLLOW_BRICK
    ),

    // Bloques de cemento/hormigón (Portantes y no portantes)
    BLOQUE_10(
        Res.string.brick_name_bloque_10,
        false, Res.string.brick_desc_bloque_10,
        BrickFamily.CONCRETE_BLOCK
    ),
    BLOQUE_13(
        Res.string.brick_name_bloque_13,
        true,
        Res.string.brick_desc_bloque_13,
        BrickFamily.CONCRETE_BLOCK
    ),
    BLOQUE_15(
        Res.string.brick_name_bloque_15,
        true,
        Res.string.brick_desc_bloque_15,
        BrickFamily.CONCRETE_BLOCK
    ),
    BLOQUE_20(
        Res.string.brick_name_bloque_20,
        true,
        Res.string.brick_desc_bloque_20,
        BrickFamily.CONCRETE_BLOCK
    )
}

enum class WallLayout(
    val resName: StringResource,
    val description: StringResource
) {
    STRETCHER(
        Res.string.layout_stretcher_title,
        Res.string.layout_stretcher_desc
    ), // Soga: L x H (Ancho = W)
    HEADER(
        Res.string.layout_header_title,
        Res.string.layout_header_desc
    ), // Cabeza: W x H (Ancho = L)
    ROWLOCK(
        Res.string.layout_rowlock_title,
        Res.string.layout_rowlock_desc
    ) // Canto: L x W (Ancho = H)
}

/**
 * Propiedades físicas del ladrillo para el cálculo. Todas las medidas en METROS.
 *
 * @property id Identificador único del ladrillo (para buscar precios).
 * @property width Ancho de la unidad en metros
 * @property height Alto de la unidad en metros
 * @property length Largo de la unidad en metros
 * @property gasketThickness Espesor de la junta en metros
 * @property family Familia del ladrillo
 */
data class BrickProps(
    val id: String, // Agregado para identificar el ladrillo en precios
    val width: Double,
    val height: Double,
    val length: Double,
    val gasketThickness: Double, // Espesor de la junta
    val family: BrickFamily // Nueva propiedad
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
 * @property netAreaM2 Área neta del muro en m².
 * @property quantityBricks Cantidad de ladrillos necesarios
 * @property percentageBrickWaste Porcentaje de desperdicio de ladrillos
 * @property mortarM3 Volumen de mortero en m³.
 * @property cementKg Cantidad de cemento en kg.
 * @property limeKg Cantidad de cal en kg.
 * @property sandM3 Cantidad de arena en m³.
 * @property waterLiters Cantidad de agua en litros.
 * @property percentageMortarWaste Porcentaje de desperdicio de mortero.
 * @property mixingRatio Razón de mezcla.
 * @property cementBagKg Peso de la bolsa de cemento.
 * @property limeBagKg Peso de la bolsa de cal.
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
