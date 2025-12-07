package org.m415x.materialscalculator.ui.common

import androidx.compose.runtime.Composable

/**
 * Maneja el evento de presionar el botón de volver (back) en la app.
 * "expect" significa: "Espero que cada plataforma implemente esta función"

 * @param enabled Indica si el manejo de back está habilitado.
 * @param onBack Acción a realizar al presionar el botón de volver.
 */
@Composable
expect fun KmpBackHandler(enabled: Boolean = true, onBack: () -> Unit)