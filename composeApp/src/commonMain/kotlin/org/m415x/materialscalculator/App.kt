package org.m415x.materialscalculator

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch

import org.m415x.materialscalculator.ui.theme.*
import org.m415x.materialscalculator.ui.common.AppBottomBar
import org.m415x.materialscalculator.ui.common.AppTopBar
import org.m415x.materialscalculator.ui.common.KmpBackHandler
import org.m415x.materialscalculator.ui.screen.concrete.ConcreteScreen
import org.m415x.materialscalculator.ui.screen.home.HomeScreen
import org.m415x.materialscalculator.ui.navigation.BottomTab
import org.m415x.materialscalculator.ui.navigation.Screen
import org.m415x.materialscalculator.ui.screen.saved.SavedScreen
import org.m415x.materialscalculator.ui.screen.settings.SettingsScreen
import org.m415x.materialscalculator.ui.screen.structure.StructureScreen
import org.m415x.materialscalculator.ui.theme.AppTheme
import org.m415x.materialscalculator.ui.screen.wall.WallScreen
import org.m415x.materialscalculator.data.repository.SettingsRepository

@Composable
fun App(
    // Recibe el repositorio (Inyección de Dependencias manual)
    settingsRepository: SettingsRepository
) {
    // Leer el estado desde el repositorio (collectAsState)
    // Usamos el valor inicial System/Standard mientras carga el dato real
    val themeMode by settingsRepository.themeMode.collectAsState(initial = ThemeMode.System)
    val contrastMode by settingsRepository.contrastMode.collectAsState(initial = ContrastMode.Standard)

    // Scope para lanzar corrutinas de guardado
    val scope = rememberCoroutineScope()

    AppTheme(themeMode, contrastMode) {
        // 1. Estado del Pager (Controla el deslizamiento)
        // Le decimos que tenemos tantos "pasos" como tabs haya en el enum (3)
        val pagerState = rememberPagerState(pageCount = { BottomTab.entries.size })

        // Necesitamos un CoroutineScope para mover el pager cuando hacemos click en los botones
        val scope = rememberCoroutineScope()

        // 2. Calculamos el Tab Actual basándonos en la página del Pager
        val currentTab = BottomTab.entries[pagerState.currentPage]

        // 3. Estado de la Pila de Navegación (Solo para la Calculadora - Tab 0)
        val calculatorStack = remember { mutableStateListOf<Screen>(Screen.Home) }

        // Lógica para saber qué pantalla mostrar
        val activeScreen = when (currentTab) {
            BottomTab.CALCULATOR -> calculatorStack.lastOrNull() ?: Screen.Home
            BottomTab.SAVED -> Screen.Guardados
            BottomTab.SETTINGS -> Screen.Configuracion
        }

        // --- LÓGICA DE BACK ---
        val navigateBack: () -> Unit = {
            if (currentTab == BottomTab.CALCULATOR && calculatorStack.size > 1) {
                // Si estamos en la calculadora y hay sub-pantallas, volvemos atrás
                calculatorStack.removeAt(calculatorStack.lastIndex)
            } else if (currentTab != BottomTab.CALCULATOR) {
                // OPCIONAL: Si estamos en "Guardados" o "Ajustes" y damos atrás,
                // volvemos a la pestaña principal ("Calculadora") deslizando.
                scope.launch { pagerState.animateScrollToPage(BottomTab.CALCULATOR.ordinal) }
            }
        }

        // Activamos el BackHandler si:
        // 1. Estamos en la calculadora y hay historial de pantallas.
        // 2. O si estamos en otra pestaña (para volver a la calculadora antes de salir).
        KmpBackHandler(
            enabled = (currentTab == BottomTab.CALCULATOR && calculatorStack.size > 1) ||
                    (currentTab != BottomTab.CALCULATOR)
        ) {
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
            contentWindowInsets = WindowInsets.systemBars, // Esto hace que el Scaffold (y la BottomBar) NO se muevan cuando sale el teclado.
            bottomBar = {
                AppBottomBar(
                    currentTab = currentTab,
                    onTabSelected = { newTab ->
                        // Al hacer click, lanzamos la animación del Pager
                        scope.launch {
                            pagerState.animateScrollToPage(newTab.ordinal)
                        }

                        // Reseteo opcional: Si tocas "Calcular" y ya estabas ahí, volver al Home
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
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues) // Aplica padding de barras de sistema
                    .consumeWindowInsets(paddingValues) // Buena práctica en M3
                    .imePadding(), // Aplicamos el padding del teclado SOLO al contenido.
                color = MaterialTheme.colorScheme.background
            ) {
                HorizontalPager(
                    state = pagerState,
                    // Opcional: userScrollEnabled = false (si quisieras bloquear el swipe)
                ) { pageIndex ->

                    // Renderizamos el contenido según la página (0, 1 o 2)
                    when (BottomTab.entries[pageIndex]) {

                        BottomTab.CALCULATOR -> {
                            // Pestaña 1: La Calculadora con su propia navegación interna
                            when (val screen = calculatorStack.lastOrNull() ?: Screen.Home) {
                                is Screen.Home -> HomeScreen(
                                    onConcreteClick = { calculatorStack.add(Screen.Hormigon) },
                                    onWallClick = { calculatorStack.add(Screen.Muro) },
                                    onStructureClick = { calculatorStack.add(Screen.Estructura) }
                                )
                                is Screen.Hormigon -> ConcreteScreen()
                                is Screen.Muro -> WallScreen()
                                is Screen.Estructura -> StructureScreen()
                                else -> {}
                            }
                        }

                        BottomTab.SAVED -> {
                            // Pestaña 2
                            SavedScreen()
                        }

                        BottomTab.SETTINGS -> {
                            // Pestaña 3
                            SettingsScreen(
                                currentTheme = themeMode,
                                currentContrast = contrastMode,
                                onThemeChange = { newMode ->
                                    // GUARDAR EN BACKGROUND
                                    scope.launch { settingsRepository.saveThemeMode(newMode) }
                                },
                                onContrastChange = { newMode ->
                                    scope.launch { settingsRepository.saveContrastMode(newMode) }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}