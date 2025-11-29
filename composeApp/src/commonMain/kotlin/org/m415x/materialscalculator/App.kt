package org.m415x.materialscalculator

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import org.m415x.materialscalculator.ui.common.AppBottomBar
import org.m415x.materialscalculator.ui.common.AppTopBar
import org.m415x.materialscalculator.ui.common.KmpBackHandler
import org.m415x.materialscalculator.ui.screens.concrete.ConcreteScreen
import org.m415x.materialscalculator.ui.screens.home.HomeScreen
import org.m415x.materialscalculator.ui.screens.navigation.BottomTab
import org.m415x.materialscalculator.ui.screens.navigation.Screen
import org.m415x.materialscalculator.ui.screens.saved.SavedScreen
import org.m415x.materialscalculator.ui.screens.settings.SettingsScreen
import org.m415x.materialscalculator.ui.screens.structure.StructureScreen
import org.m415x.materialscalculator.ui.theme.AppTheme
import org.m415x.materialscalculator.ui.screens.wall.WallScreen

@Composable
fun App() {
    AppTheme {
        // 1. Estado de la Pestaña Activa (Bottom Bar)
        var currentTab by remember { mutableStateOf(BottomTab.CALCULATOR) }

        // 2. Estado de la Pila de Navegación (Solo para la Calculadora)
        val calculatorStack = remember { mutableStateListOf<Screen>(Screen.Home) }

        // Lógica para saber qué pantalla mostrar
        val activeScreen = when (currentTab) {
            BottomTab.CALCULATOR -> calculatorStack.lastOrNull() ?: Screen.Home
            BottomTab.SAVED -> Screen.Guardados
            BottomTab.SETTINGS -> Screen.Configuracion
        }

        // Lógica de navegación atrás
        val navigateBack: () -> Unit = {
            if (currentTab == BottomTab.CALCULATOR && calculatorStack.size > 1) {
                calculatorStack.removeAt(calculatorStack.lastIndex)
            } else {
                // Opcional: Si estás en Ajustes y das atrás, ¿vuelves a la calculadora?
                // Por ahora no, para mantenerlo simple.
            }
        }

        // Interceptor del botón físico
        KmpBackHandler(enabled = currentTab == BottomTab.CALCULATOR && calculatorStack.size > 1) {
            navigateBack()
        }

        Scaffold(
            topBar = {
                AppTopBar(
                    title = activeScreen.title,
                    showBackButton = activeScreen.showBackButton, // Esto viene de tu clase Screen
                    onBack = navigateBack
                )
            },
            bottomBar = {
                AppBottomBar(
                    currentTab = currentTab,
                    onTabSelected = { newTab ->
                        currentTab = newTab
                        // Opcional: Si tocas "Calcular" y ya estabas ahí, podrías resetear al Home
                        if (newTab == BottomTab.CALCULATOR && currentTab == BottomTab.CALCULATOR) {
                            if (calculatorStack.size > 1) {
                                calculatorStack.clear()
                                calculatorStack.add(Screen.Home)
                            }
                        }
                    }
                )
            }
        ) { paddingValues ->
            // Contenedor principal con el padding del Scaffold
            Surface(
                modifier = Modifier.padding(paddingValues),
                color = MaterialTheme.colorScheme.background
            ) {
                when (currentTab) {
                    BottomTab.CALCULATOR -> {
                        // Renderizamos la pila de la calculadora
                        when (val screen = activeScreen) {
                            is Screen.Home -> HomeScreen(
                                onConcreteClick = { calculatorStack.add(Screen.Hormigon) },
                                onWallClick = { calculatorStack.add(Screen.Muro) },
                                onStructureClick = { calculatorStack.add(Screen.Estructura) }
                            )
                            is Screen.Hormigon -> ConcreteScreen()
                            is Screen.Muro -> WallScreen()
                            is Screen.Estructura -> StructureScreen()
                            else -> {} // Caso imposible
                        }
                    }
                    BottomTab.SAVED -> SavedScreen()
                    BottomTab.SETTINGS -> SettingsScreen()
                }
            }
        }
    }
}