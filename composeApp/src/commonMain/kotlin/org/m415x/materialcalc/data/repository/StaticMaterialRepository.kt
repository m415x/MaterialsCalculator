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

package org.m415x.materialcalc.data.repository

import materialscalculator.composeapp.generated.resources.*
import org.m415x.materialcalc.domain.model.*
import org.m415x.materialcalc.domain.repository.MaterialRepository

/**
 * Repositorio de datos estáticos.
 * Este repositorio proporciona acceso a datos estáticos de materiales de construcción, como dosificaciones de hormigón,
 * propiedades de ladrillos, pesos de hierro y recetas de mortero.
 */
class StaticMaterialRepository : MaterialRepository {

    /**
     * Base de datos interna de dosificaciones de hormigones.
     * Aquí es donde "guardamos" las tablas que buscamos.
     *
     * @property concreteDB Base de datos de dosificaciones de hormigones.
     */
    private val concreteDB = mapOf(

        // H8: Hormigón de limpieza (pobre). Poco cemento.
        // Usos: Contrapisos, carpetas, nivelación.
        ConcreteType.H8 to ConcreteDosing(
            name = TextSource.Resource(Res.string.recipe_name_h8),
            descriptionProportion = TextSource.Resource(Res.string.recipe_prop_h8),
            cementKg = 182.0,
            sandM3 = 0.52,
            gravelM3 = 1.04,
            waterLiters = 109.0,
            waterCementRatio = 0.6
        ),

        // H13: Hormigón simple (no estructural o estructuras muy livianas).
        ConcreteType.H13 to ConcreteDosing(
            name = TextSource.Resource(Res.string.recipe_name_h13),
            descriptionProportion = TextSource.Resource(Res.string.recipe_prop_h13),
            cementKg = 258.0,
            sandM3 = 0.55,
            gravelM3 = 0.92,
            waterLiters = 129.0,
            waterCementRatio = 0.5
        ),

        // H17: Hormigón estándar para viviendas pequeñas (bases, encadenados).
        ConcreteType.H17 to ConcreteDosing(
            name = TextSource.Resource(Res.string.recipe_name_h17),
            descriptionProportion = TextSource.Resource(Res.string.recipe_prop_h17),
            cementKg = 305.0,
            sandM3 = 0.54,
            gravelM3 = 0.87,
            waterLiters = 152.0,
            waterCementRatio = 0.5
        ),

        // H21: Hormigón ESTRUCTURAL estándar (Losas, vigas, columnas).
        // Es el más utilizado en construcción tradicional.
        ConcreteType.H21 to ConcreteDosing(
            name = TextSource.Resource(Res.string.recipe_name_h21),
            descriptionProportion = TextSource.Resource(Res.string.recipe_prop_h21),
            cementKg = 377.0, // Un H21 de obra suele ser rico (>350kg)
            sandM3 = 0.54,
            gravelM3 = 0.81,
            waterLiters = 170.0,
            waterCementRatio = 0.45
        ),

        // H25: Hormigón de alta resistencia.
        // Usos: Columnas muy cargadas, estructuras importantes.
        ConcreteType.H25 to ConcreteDosing(
            name = TextSource.Resource(Res.string.recipe_name_h25),
            descriptionProportion = TextSource.Resource(Res.string.recipe_prop_h25),
            cementKg = 450.0,
            sandM3 = 0.48,
            gravelM3 = 0.81,
            waterLiters = 189.0,
            waterCementRatio = 0.42
        ),

        // H30: Hormigón de muy alta resistencia.
        // NOTA: Difícil de lograr a mano sin aditivos fluidificantes por la poca agua.
        ConcreteType.H30 to ConcreteDosing(
            name = TextSource.Resource(Res.string.recipe_name_h30),
            descriptionProportion = TextSource.Resource(Res.string.recipe_prop_h30),
            cementKg = 554.0,
            sandM3 = 0.40,
            gravelM3 = 0.79,
            waterLiters = 221.0,
            waterCementRatio = 0.40
        )
    )

    /**
     * Base de datos interna de dosificaciones de revoques.
     */
    private val plasterDB = mapOf(
        PlasterType.STD_JAHARRO to MortarDosing(
            name = TextSource.Resource(Res.string.recipe_name_jaharro),
            mixingRatio = TextSource.Resource(Res.string.recipe_prop_jaharro),
            cementKg = 115.0,       // Aprox 4 bolsas por m3 (es una mezcla "bastarda", lleva menos cemento que un concreto)
            limeKg = 200.0,         // Mucha cal para plasticidad
            sandM3 = 1.1,           // Arena común
            waterLiters = 250.0
        )
    )

    /**
     * Mapeo de dimensiones y juntas sugeridas.
     * Se asume colocación estándar (no panderete/canto, salvo especificación).
     *
     * @property brickDB Mapeo de dimensiones y juntas sugeridas.
     */
    private val brickDB = mapOf(
        // // --- Ladrillos Macizos ---
        // Junta más gruesa a 2.0 cm
        BrickType.COMUN to BrickProps(
            id = BrickType.COMUN.name,
            width = 0.12,
            height = 0.05,
            length = 0.25,
            gasketThickness = 0.02,
            family = BrickFamily.SOLID_BRICK
        ),
        BrickType.LADRILLON to BrickProps(
            id = BrickType.LADRILLON.name,
            width = 0.18,
            height = 0.07,
            length = 0.29,
            gasketThickness = 0.02,
            family = BrickFamily.SOLID_BRICK
        ),

        // --- Cerámicos HUECOS (No Portantes) ---
        // Altura 18 cm. Junta suele ser 1.5 cm aprox.
        BrickType.HUECO_8 to BrickProps(
            id = BrickType.HUECO_8.name,
            width = 0.08,
            height = 0.18,
            length = 0.33,
            gasketThickness = 0.015,
            family = BrickFamily.NON_LOAD_BEARING_HOLLOW_BRICK
        ),
        BrickType.HUECO_12 to BrickProps(
            id = BrickType.HUECO_12.name,
            width = 0.12,
            height = 0.18,
            length = 0.33,
            gasketThickness = 0.015,
            family = BrickFamily.NON_LOAD_BEARING_HOLLOW_BRICK
        ),
        BrickType.HUECO_18 to BrickProps(
            id = BrickType.HUECO_18.name,
            width = 0.18,
            height = 0.18,
            length = 0.33,
            gasketThickness = 0.015,
            family = BrickFamily.NON_LOAD_BEARING_HOLLOW_BRICK
        ),

        // --- Cerámicos PORTANTES ---
        // Altura 19 cm. Se usan con menos mezcla (1 cm) para modulación perfecta de 20cm.
        BrickType.PORTANTE_12 to BrickProps(
            id = BrickType.PORTANTE_12.name,
            width = 0.12,
            height = 0.19,
            length = 0.33,
            gasketThickness = 0.015,
            family = BrickFamily.LOAD_BEARING_HOLLOW_BRICK
        ),
        BrickType.PORTANTE_18 to BrickProps(
            id = BrickType.PORTANTE_18.name,
            width = 0.18,
            height = 0.19,
            length = 0.33,
            gasketThickness = 0.015,
            family = BrickFamily.LOAD_BEARING_HOLLOW_BRICK
        ),

        // --- Bloques de Hormigón ---
        // Medidas estándar Argentina: Largo 39, Alto 19.
        // Bloques de Hormigón: Junta fina (1.0 cm)
        BrickType.BLOQUE_10 to BrickProps(
            id = BrickType.BLOQUE_10.name,
            width = 0.1,
            height = 0.19,
            length = 0.39,
            gasketThickness = 0.01,
            family = BrickFamily.CONCRETE_BLOCK
        ),
        BrickType.BLOQUE_13 to BrickProps(
            id = BrickType.BLOQUE_13.name,
            width = 0.13,
            height = 0.19,
            length = 0.39,
            gasketThickness = 0.01,
            family = BrickFamily.CONCRETE_BLOCK
        ),
        BrickType.BLOQUE_15 to BrickProps(
            id = BrickType.BLOQUE_15.name,
            width = 0.15,
            height = 0.19,
            length = 0.39,
            gasketThickness = 0.01,
            family = BrickFamily.CONCRETE_BLOCK
        ),
        BrickType.BLOQUE_20 to BrickProps(
            id = BrickType.BLOQUE_20.name,
            width = 0.2,
            height = 0.19,
            length = 0.39,
            gasketThickness = 0.01,
            family = BrickFamily.CONCRETE_BLOCK
        )
    )

    /**
     * Dosificación para 1 m3 de Revoque Reforzado
     *
     * @property reinforcedLimeMixture Dosificación para 1 m3 de Revoque Reforzado
     */
    private val reinforcedLimeMixture = MortarDosing(
        name = TextSource.Resource(Res.string.recipe_name_reinforced),
        mixingRatio = TextSource.Resource(Res.string.recipe_prop_reinforced),
        cementKg = 170.0,
        limeKg = 150.0,
        sandM3 = 1.05,
        waterLiters = 240.0
    )

    /**
     * Dosificación para 1 m3 de Mezcla Cemento-Arena (sin cal)
     *
     * @property cementSandMixture Dosificación para 1 m3 de Mezcla Cemento-Arena (sin cal)
     */
    private val cementSandMixture = MortarDosing(
        name = TextSource.Resource(Res.string.recipe_name_cement),
        mixingRatio = TextSource.Resource(Res.string.recipe_prop_cement),
        cementKg = 450.0,
        limeKg = 0.0,
        sandM3 = 1.1,
        waterLiters = 220.0,
        waterCementRatio = 0.5
    )

    /**
     * Dosificación para 1 m3 de Revoque Fino Tradicional (1/8 Cemento : 1 Aérea : 2 Arena Fina).
     *
     * @property finePlasterRecipe Dosificación para 1 m3 de Revoque Fino Tradicional (1/8 Cemento : 1 Aérea : 2 Arena Fina).
     */
    private val finePlasterRecipe = MortarDosing(
        name = TextSource.Resource(Res.string.recipe_name_fine),
        mixingRatio = TextSource.Resource(Res.string.recipe_prop_fine),
        cementKg = 90.0,        // Muy poco, solo para ligar
        limeKg = 250.0,         // Pura cal aérea
        sandM3 = 1.0,           // Arena fina (voladora)
        waterLiters = 240.0
    )

    /**
     * Dosificación para 1 m3 de hormigón.
     *
     * @property getConcreteDosing Dosificación para 1 m3 de hormigón.
     */
    override fun getConcreteDosing(type: ConcreteType) = concreteDB[type]

    /**
     * Dosificación para 1 m3 de revoque.
     */
    override fun getPlasterDosing(type: PlasterType) = plasterDB[type]

    /**
     * Propiedades de un ladrillo.
     *
     * @property getBrickProps Propiedades de un ladrillo.
     */
    override fun getBrickProps(type: BrickType) = brickDB[type]

    /**
     * Dosificación para 1 m3 de mortero.
     *
     * @property getMortarDosing Dosificación para 1 m3 de mortero.
     */
    override fun getMortarDosing(type: BrickType): MortarDosing {
        return when (type) {
            BrickType.BLOQUE_10,
            BrickType.BLOQUE_13,
            BrickType.BLOQUE_15,
            BrickType.BLOQUE_20 -> cementSandMixture

            else -> reinforcedLimeMixture
        }
    }

    /**
     * Peso por metro de varilla según diámetro.
     *
     * @property getIronWeightPerMeter Peso por metro de varilla según diámetro.
     */
    override fun getIronWeightPerMeter(diameter: IronDiameter): Double {
        return diameter.linearWeightKgM
    }

    /**
     * Mezcla recomendada para revoque reforzado.
     *
     * @property getThickPlasterRecipe Mezcla recomendada para revoque reforzado.
     */
    override fun getThickPlasterRecipe() = plasterDB[PlasterType.STD_JAHARRO]!!

    /**
     * Mezcla recomendada para revoque fino.
     *
     * @property getFinePlasterRecipe Mezcla recomendada para revoque fino.
     */
    override fun getFinePlasterRecipe() = finePlasterRecipe

    /**
     * Función auxiliar para obtener TODOS los ladrillos (Sistema + Usuario)
     * para mostrarlos en la lista de selección.
     */
    fun getAllBricks(customBricks: List<CustomBrick>): List<BrickOption> {
        // 1. Convertimos los del sistema (Enum) a una estructura común
        val systemOptions = BrickType.entries.map { type ->
            val props = getBrickProps(type)!!
            BrickOption(
                id = type.name,
                name = TextSource.Resource(type.brickNameRes),
                props = props,
                isCustom = false
            )
        }

        // 2. Convertimos los del usuario
        val customOptions = customBricks.map { custom ->
            BrickOption(
                id = custom.id,
                name = TextSource.Raw(custom.name),
                props = custom.toProperties(),
                isCustom = true
            )
        }

        return systemOptions + customOptions
    }
}

// Clase auxiliar para la UI (Representa una opción en el dropdown/lista)
data class BrickOption(
    val id: String,
    val name: TextSource,
    val props: BrickProps,
    val isCustom: Boolean // Para saber si mostrar el botón de "Borrar"
)
