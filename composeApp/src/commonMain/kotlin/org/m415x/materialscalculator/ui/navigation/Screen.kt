package org.m415x.materialscalculator.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Enumeración que representa las pantallas de la aplicación.
 * Cada pantalla tiene un título y un indicador de si debe mostrar el botón de regreso.
 *
 * @property title Título de la pantalla.
 * @property showBackButton Indicador de si debe mostrar el botón de regreso.
 */
sealed class Screen(val title: String, val showBackButton: Boolean) {
    data object Home : Screen("Calculadora de Materiales", false)
    data object Hormigon : Screen("Hormigón", true)
    data object Muro : Screen("Muros", true)
    data object Estructura : Screen("Armaduras", true)
    data object Revoque : Screen("Revoque", true)

    // Pantallas de las otras secciones (Son raíces, no tienen back button)
    data object Guardados : Screen("Cálculos Guardados", false)
    data object Configuracion : Screen("Configuración", false)
}

/**
 * Enumeración que representa las secciones del menú inferior.
 * Cada sección tiene un título, un icono y una pantalla asociada.
 *
 * @property title Título de la sección.
 * @property icon Icono de la sección.
 * @property screen Pantalla asociada a la sección.
 */
enum class BottomTab(
    val title: String,
    val icon: ImageVector,
    val screen: Screen // A qué pantalla nos lleva este tab
) {
    CALCULATOR("Calcular", Icons.Default.Calculate, Screen.Home),
    SAVED("Guardados", Icons.Default.Save, Screen.Guardados),
    SETTINGS("Ajustes", Icons.Default.Settings, Screen.Configuracion)
}