package org.m415x.materialscalculator.ui.common

import androidx.compose.runtime.Composable

@Composable
actual fun KmpBackHandler(enabled: Boolean, onBack: () -> Unit) {
    // En escritorio no hay botón atrás de hardware.
    // Se suele usar ESC o botones en pantalla. Lo dejamos vacío.
}