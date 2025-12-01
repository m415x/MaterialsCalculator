package org.m415x.materialscalculator.domain.common

// Convierte Centímetros a Metros
val Double.cmToMeters: Double
    get() = this / 100.0

val Int.cmToMeters: Double
    get() = this.toDouble() / 100.0

// (Opcional) Convierte Metros a Centímetros
val Double.metersToCm: Double
    get() = this * 100.0