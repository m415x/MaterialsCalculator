package org.m415x.materialscalculator.ui

// Define las pantallas disponibles en la app
sealed class Screen {
    object Home : Screen()
    object Hormigon : Screen()
    object Muros : Screen()
    object Estructura : Screen()
}