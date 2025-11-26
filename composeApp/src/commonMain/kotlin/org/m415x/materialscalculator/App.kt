package org.m415x.materialscalculator

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

import org.m415x.materialscalculator.ui.HomeScreen
import org.m415x.materialscalculator.ui.Screen

@Composable
fun App() {
    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            // Estado para controlar en qué pantalla estamos
            // "by remember" recuerda el valor aunque la UI se redibuje
            var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }

            when (currentScreen) {
                is Screen.Home -> {
                    HomeScreen(
                        onNavigate = { screenDestino ->
                            currentScreen = screenDestino
                        }
                    )
                }
                is Screen.Hormigon -> {
                    // TODO: Aquí pondremos la pantalla de Hormigón real
                    PantallaEnConstruccion("Calculadora de Hormigón", onBack = { currentScreen = Screen.Home })
                }
                is Screen.Muros -> {
                    // TODO: Pantalla de Muros
                    PantallaEnConstruccion("Calculadora de Muros", onBack = { currentScreen = Screen.Home })
                }
                is Screen.Estructura -> {
                    // TODO: Pantalla de Estructura
                    PantallaEnConstruccion("Calculadora de Estructura", onBack = { currentScreen = Screen.Home })
                }
            }
        }
    }
}

// Un placeholder temporal para probar la navegación
@Composable
fun PantallaEnConstruccion(titulo: String, onBack: () -> Unit) {
    androidx.compose.foundation.layout.Column {
        androidx.compose.material3.Button(onClick = onBack) {
            androidx.compose.material3.Text("Volver")
        }
        androidx.compose.material3.Text(text = "Aquí va: $titulo")
    }
}