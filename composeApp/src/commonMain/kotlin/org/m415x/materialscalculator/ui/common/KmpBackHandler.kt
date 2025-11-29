package org.m415x.materialscalculator.ui.common

import androidx.compose.runtime.Composable

// "expect" significa: "Espero que cada plataforma implemente esta función"
@Composable
expect fun KmpBackHandler(enabled: Boolean = true, onBack: () -> Unit)