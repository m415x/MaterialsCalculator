package org.m415x.materialscalculator

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "MaterialsCalculator",
    ) {
        App()
    }
}