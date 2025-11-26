package org.m415x.materialscalculator.ui.navigation

/*
 * Define las pantallas disponibles en la app
 */
sealed class Screen {
    object Home : Screen()
    object Hormigon : Screen()
    object Muro : Screen()
    object Estructura : Screen()
}