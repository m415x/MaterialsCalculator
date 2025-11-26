package org.m415x.materialscalculator

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import org.m415x.materialscalculator.ui.concrete.ConcreteScreen

import org.m415x.materialscalculator.ui.home.HomeScreen
import org.m415x.materialscalculator.ui.navigation.Screen
import org.m415x.materialscalculator.ui.structure.StructureScreen
import org.m415x.materialscalculator.ui.wall.WallScreen

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
                    ConcreteScreen(
                        onBack = { currentScreen = Screen.Home }
                    )
                }
                is Screen.Muro -> {
                    WallScreen(
                        onBack = { currentScreen = Screen.Home }
                    )
                }
                is Screen.Estructura -> {
                    StructureScreen(
                        onBack = { currentScreen = Screen.Home }
                    )
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