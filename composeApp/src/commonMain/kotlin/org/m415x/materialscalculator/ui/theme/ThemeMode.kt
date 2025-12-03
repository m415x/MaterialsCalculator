package org.m415x.materialscalculator.ui.theme

enum class ThemeMode {
    System, // Seguir configuración del dispositivo
    Light,  // Forzar modo claro
    Dark    // Forzar modo oscuro
}

enum class ContrastMode {
    Standard,     // Contraste normal
    HighContrast  // Contraste elevado (mejor para exteriores/glare)
}