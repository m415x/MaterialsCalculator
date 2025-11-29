package org.m415x.materialscalculator

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable

@Composable
fun AndroidApp() {

    // Este BackHandler SOLO se compila en Android
    BackHandler(enabled = screensStack.size > 1) {
        if (screensStack.size > 1) {
            screensStack.removeAt(screensStack.lastIndex)
        }
    }

    App() // Llama a la UI común
}
