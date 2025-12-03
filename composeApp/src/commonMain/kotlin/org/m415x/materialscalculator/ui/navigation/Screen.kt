package org.m415x.materialscalculator.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

// 1. Pantallas de la Navegación (Stack)
// Añadimos propiedades para configurar la TopBar automáticamente
sealed class Screen(val title: String, val showBackButton: Boolean) {
    data object Home : Screen("Calculadora de Materiales", false)
    data object Hormigon : Screen("Hormigón", true)
    data object Muro : Screen("Muros", true)
    data object Estructura : Screen("Armaduras", true)

    // Pantallas de las otras secciones (Son raíces, no tienen back button)
    data object Guardados : Screen("Cálculos Guardados", false)
    data object Configuracion : Screen("Configuración", false)
}

// 2. Secciones del Menú Inferior (Bottom Bar)
enum class BottomTab(
    val title: String,
    val icon: ImageVector,
    val screen: Screen // A qué pantalla nos lleva este tab
) {
    CALCULATOR("Calcular", Icons.Default.Calculate, Screen.Home),
    SAVED("Guardados", Icons.Default.Save, Screen.Guardados),
    SETTINGS("Ajustes", Icons.Default.Settings, Screen.Configuracion)
}