package org.m415x.materialscalculator.ui.common

interface BrightnessManager {
    /**
     * @param value: 0.0 (mínimo) a 1.0 (máximo). Si es null, vuelve al automático/sistema.
     */
    fun setBrightness(value: Float?)
}

expect fun getBrightnessManager(): BrightnessManager