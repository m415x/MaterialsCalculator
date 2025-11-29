package org.m415x.materialscalculator.ui.common

import androidx.compose.runtime.Composable

@Composable
actual fun KmpBackHandler(enabled: Boolean, onBack: () -> Unit) {
    // En web el botón atrás es del navegador.
    // Manejarlo requiere integración con el historial del navegador.
    // Por ahora lo dejamos vacío.
}