package org.m415x.materialscalculator.ui.theme

/**
 * Enum que define el modo de tema.
 */
enum class ThemeMode {
    System, // Seguir configuración del dispositivo
    Light,  // Forzar modo claro
    Dark    // Forzar modo oscuro
}

/**
 * Enum que define el modo de contraste.
 */
enum class ContrastMode {
    Standard,     // Contraste normal
    HighContrast  // Contraste elevado (mejor para exteriores/glare)
}