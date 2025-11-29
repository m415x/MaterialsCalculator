package org.m415x.materialscalculator.ui.common

import androidx.compose.runtime.Composable
import androidx.activity.compose.BackHandler // Esto viene de la librería de Android

@Composable
actual fun KmpBackHandler(enabled: Boolean, onBack: () -> Unit) {
    // Aquí conectamos nuestro puente con la función real de Android
    BackHandler(enabled = enabled, onBack = onBack)
}