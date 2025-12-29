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

package org.m415x.materialcalc.domain.common

/**
 * Convierte Centímetros a Metros.
 *
 * @property cmToMeters Convierte Double a Metros.
 */
val Double.cmToMeters: Double
    get() = this / 100.0

/**
 * Convierte Centímetros a Metros.
 *
 * @property cmToMeters Convierte Int a Metros.
 */
val Int.cmToMeters: Double
    get() = this.toDouble() / 100.0

/**
 * Convierte Metros a Centímetros.
 *
 * @property metersToCm Convierte Double a Centímetros.
 */
val Double.metersToCm: Double
    get() = this * 100.0