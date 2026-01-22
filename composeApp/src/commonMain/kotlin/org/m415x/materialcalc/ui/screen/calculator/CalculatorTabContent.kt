/*
 * materialCalc
 * Copyright (C) 2026 M415X
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
import org.m415x.materialcalc.domain.model.AppSettingsState
import org.m415x.materialcalc.ui.navigation.Screen
import org.m415x.materialcalc.ui.screen.concrete.ConcreteScreen
import org.m415x.materialcalc.ui.screen.plaster.PlasterScreen
import org.m415x.materialcalc.ui.screen.structure.StructureScreen
import org.m415x.materialcalc.ui.screen.wall.WallScreen

/**
 * Componente principal de la pantalla de cálculo.
 *
 * @param currentScreen La pantalla actual.
 * @param appSettings El estado global de la configuración.
 * @param onNavigate La función de navegación.
 */
@Composable
fun CalculatorTabContent(
    currentScreen: Screen,
    appSettings: AppSettingsState,
    onNavigate: (Screen) -> Unit
) {
    AnimatedContent(
        targetState = currentScreen,
        label = "CalculatorNavAnimation",
        transitionSpec = { calculatorTransitionSpec(targetState) }
    ) { targetScreen ->
        // Renderizamos la pantalla correspondiente
        when (targetScreen) {
            is Screen.Home -> CalculatorMenuScreen(
                onConcreteClick = { onNavigate(Screen.Concrete) },
                onWallClick = { onNavigate(Screen.Wall) },
                onStructureClick = { onNavigate(Screen.Structure) },
                onPlasterClick = { onNavigate(Screen.Plaster) }
            )

            is Screen.Concrete -> ConcreteScreen(appSettings)
            is Screen.Wall -> WallScreen(appSettings)
            is Screen.Structure -> StructureScreen(appSettings)
            is Screen.Plaster -> PlasterScreen(appSettings)
            else -> {}
        }
    }
}

/**
 * Define la especificación de transición para la navegación de la calculadora.
 * Extraída para mejorar la legibilidad y evitar recomposiciones innecesarias de la definición.
 */
private fun calculatorTransitionSpec(targetState: Screen): ContentTransform {
    // LÓGICA DE DIRECCIÓN:
    // Si el destino es HOME, estamos volviendo (Back).
    // Si el destino NO es Home, estamos entrando a un detalle (Forward).
    return if (targetState == Screen.Home) {
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