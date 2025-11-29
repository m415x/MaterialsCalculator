package org.m415x.materialscalculator

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.material3.Surface
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme

import org.m415x.materialscalculator.ui.common.KmpBackHandler
import org.m415x.materialscalculator.ui.navigation.Screen
import org.m415x.materialscalculator.ui.home.HomeScreen
import org.m415x.materialscalculator.ui.concrete.ConcreteScreen
import org.m415x.materialscalculator.ui.wall.WallScreen
import org.m415x.materialscalculator.ui.structure.StructureScreen
import org.m415x.materialscalculator.ui.theme.AppTheme

// 1. Define la pila de navegación (Back Stack)
val screensStack = mutableStateListOf<Screen>(Screen.Home)

@Composable
fun App() {
    AppTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {

            // 2. Determina la pantalla actual (la última de la pila)
            val currentScreen = screensStack.last()

            // 3. Función para navegar hacia atrás
            val navigateBack: () -> Unit = {
                if (screensStack.size > 1) screensStack.removeAt(screensStack.lastIndex) // Si hay más de una, saca la última.
                // Si solo queda HomeScreen, no hacemos nada (la app se cerrará por el SO)
            }

            // 4. Intercepta el botón del sistema (Android)
            KmpBackHandler(enabled = screensStack.size > 1) {
                navigateBack()
            }

            // 5. Función para navegar a una nueva pantalla
            val navigateTo: (Screen) -> Unit = { screen ->
                screensStack.add(screen)
            }

            // 6. Renderiza la pantalla actual con la lógica de navegación
            when (currentScreen) {
                is Screen.Home -> {
                    HomeScreen(
                        onConcreteClick = { navigateTo(Screen.Hormigon) },
                        onWallClick = { navigateTo(Screen.Muro) },
                        onStructureClick = { navigateTo(Screen.Estructura) }
                    )
                }
                is Screen.Hormigon -> {
                    ConcreteScreen(
                        onBack = navigateBack // Usa la función de la pila
                    )
                }
                is Screen.Muro -> {
                    WallScreen(
                        onBack = navigateBack // Usa la función de la pila
                    )
                }
                is Screen.Estructura -> {
                    StructureScreen(
                        onBack = navigateBack // Usa la función de la pila
                    )
                }
            }
        }
    }
}