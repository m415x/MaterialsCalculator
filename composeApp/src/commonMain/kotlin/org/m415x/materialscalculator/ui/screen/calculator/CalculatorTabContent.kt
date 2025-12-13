package org.m415x.materialscalculator.ui.screen.calculator

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import org.m415x.materialscalculator.data.repository.SettingsRepository
import org.m415x.materialscalculator.ui.navigation.Screen
import org.m415x.materialscalculator.ui.screen.concrete.ConcreteScreen
import org.m415x.materialscalculator.ui.screen.plaster.PlasterScreen
import org.m415x.materialscalculator.ui.screen.structure.StructureScreen
import org.m415x.materialscalculator.ui.screen.wall.WallScreen

/**
 * Componente principal de la pantalla de cálculo.
 *
 * @param currentScreen La pantalla actual.
 * @param settingsRepository El repositorio de configuración.
 * @param onNavigate La función de navegación.
 */
@Composable
fun CalculatorTabContent(
    currentScreen: Screen,
    settingsRepository: SettingsRepository,
    onNavigate: (Screen) -> Unit
) {
    AnimatedContent(
        targetState = currentScreen,
        label = "CalculatorNavAnimation",
        transitionSpec = {
            // LÓGICA DE DIRECCIÓN:
            // Si el destino es HOME, estamos volviendo (Back).
            // Si el destino NO es Home, estamos entrando a un detalle (Forward).
            if (targetState == Screen.Home) {
                // BACK: Entra por izquierda, sale por derecha
                slideInHorizontally(
                    initialOffsetX = { -it },
                    animationSpec = tween(300)
                ) togetherWith slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(300)
                )
            } else {
                // FORWARD: Entra por derecha, sale por izquierda
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(300)
                ) togetherWith slideOutHorizontally(
                    targetOffsetX = { -it },
                    animationSpec = tween(300)
                )
            }
        }
    ) { targetScreen ->
        // Renderizamos la pantalla correspondiente
        when (targetScreen) {
            is Screen.Home -> HomeScreen(
                onConcreteClick = { onNavigate(Screen.Hormigon) },
                onWallClick = { onNavigate(Screen.Muro) },
                onStructureClick = { onNavigate(Screen.Estructura) },
                onPlasterClick = { onNavigate(Screen.Revoque) }
            )
            is Screen.Hormigon -> ConcreteScreen(settingsRepository)
            is Screen.Muro -> WallScreen(settingsRepository)
            is Screen.Estructura -> StructureScreen(settingsRepository)
            is Screen.Revoque -> PlasterScreen(settingsRepository)
            else -> {}
        }
    }
}