/*
 * materialCalc
 * Copyright (C) 2025 M415X
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.m415x.materialcalc.ui.screen.calculator

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import org.m415x.materialcalc.data.repository.SettingsRepository
import org.m415x.materialcalc.ui.navigation.Screen
import org.m415x.materialcalc.ui.screen.concrete.ConcreteScreen
import org.m415x.materialcalc.ui.screen.plaster.PlasterScreen
import org.m415x.materialcalc.ui.screen.structure.StructureScreen
import org.m415x.materialcalc.ui.screen.wall.WallScreen

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