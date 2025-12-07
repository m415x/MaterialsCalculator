package org.m415x.materialscalculator.domain.common

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