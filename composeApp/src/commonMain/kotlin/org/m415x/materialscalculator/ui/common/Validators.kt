package org.m415x.materialscalculator.ui.common

/**
 * Valida que una serie de valores numéricos (Enteros o Decimales) sean:
 * 1. No nulos (que la conversión de String a Double haya funcionado).
 * 2. Mayores a Cero (no aceptamos negativos ni dimensiones de 0).
 *
 * @param values Valores a validar.
 * @return True si todos los valores son válidos, false en caso contrario.
 */
fun areValidDimensions(vararg values: Number?): Boolean {
    // .all devuelve true solo si la condición se cumple para TODOS los elementos
    return values.all {
        // Convertimos a Double para unificar la comparación
        it != null && it.toDouble() > 0.0
    }
}