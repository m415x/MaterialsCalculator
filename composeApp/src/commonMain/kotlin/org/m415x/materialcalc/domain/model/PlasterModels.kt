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
 * Empaqueta los resultados de forma ordenada.
 *
 * @property areaTotalM2 Superficie total (x1 o x2 caras)
 * @property bolsaCementoKg Cantidad de bolsas de cemento
 * @property bolsaCalKg Cantidad de bolsas de cal
 * @property bolsaFinoPremezclaKg Cantidad de bolsas de premezcla fina
 * @property volumenGruesoM3 Volumen grueso en metros cúbicos
 * @property gruesoCementoKg Cantidad de cemento grueso en kilogramos
 * @property gruesoCalKg Cantidad de cal gruesa en kilogramos
 * @property gruesoArenaM3 Cantidad de arena gruesa en metros cúbicos
 * @property porcentajeDesperdicioGrueso Porcentaje de desperdicio grueso
 * @property dosificacionGrueso Proporcion grueso
 * @property finoPremezclaKg Cantidad de premezcla fina en kilogramos
 * @property finoCalKg Cantidad de cal fina en kilogramos
 * @property finoArenaM3 Cantidad de arena fina en metros cúbicos
 * @property porcentajeDesperdicioFino Porcentaje de desperdicio fino
 * @property dosificacionFino Proporcion fina
 */
data class ResultadoRevoque(
    val areaTotalM2: Double, // Superficie total (x1 o x2 caras)
    val bolsaCementoKg: Int,
    val bolsaCalKg: Int,
    val bolsaFinoPremezclaKg: Int,
    // --- REVOQUE GRUESO (Jaharro) ---
    val volumenGruesoM3: Double,
    val gruesoCementoKg: Double,
    val gruesoCalKg: Double,
    val gruesoArenaM3: Double,
    val porcentajeDesperdicioGrueso: Double,
    val dosificacionGrueso: String,
    // --- REVOQUE FINO (Enlucido) ---
    // Opción 1: Premezclado (Bolsa lista)
    val finoPremezclaKg: Double,
    // Opción 2: Tradicional (A la cal)
    val finoCalKg: Double,
    val finoArenaM3: Double, // Arena voladora/fina
    val porcentajeDesperdicioFino: Double,
    val dosificacionFino: String
)