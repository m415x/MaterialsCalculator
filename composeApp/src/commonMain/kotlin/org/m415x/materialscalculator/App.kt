package org.m415x.materialscalculator

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
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

import org.m415x.materialscalculator.data.repository.SettingsRepository
import org.m415x.materialscalculator.ui.common.AppBottomBar
import org.m415x.materialscalculator.ui.common.AppTopBar
import org.m415x.materialscalculator.ui.common.clearFocusOnTap
import org.m415x.materialscalculator.ui.common.getBrightnessManager
import org.m415x.materialscalculator.ui.common.KmpBackHandler
import org.m415x.materialscalculator.ui.navigation.BottomTab
import org.m415x.materialscalculator.ui.navigation.Screen
import org.m415x.materialscalculator.ui.screen.calculator.CalculatorTabContent
import org.m415x.materialscalculator.ui.screen.saved.SavedScreen
import org.m415x.materialscalculator.ui.screen.settings.SettingsScreen
import org.m415x.materialscalculator.ui.screen.settings.SettingsSection
import org.m415x.materialscalculator.ui.theme.*
import org.m415x.materialscalculator.ui.theme.AppTheme

/**
 * Composable principal de la aplicación.
 * 
 * @param settingsRepository Repositorio de configuración.
 */
@Composable
fun App(
    // Recibe el repositorio (Inyección de Dependencias manual)
    settingsRepository: SettingsRepository
) {
    // Estado para saber en qué sub-sección de ajustes estamos
    var settingsSection by remember { mutableStateOf(SettingsSection.MENU) }

    // Leemos la preferencia
    val userTheme by settingsRepository.themeMode.collectAsState(initial = ThemeMode.System)
    val userContrast by settingsRepository.contrastMode.collectAsState(initial = ContrastMode.Standard)
    val isOutdoorMode by settingsRepository.outdoorMode.collectAsState(initial = false)

    // Si Modo Exterior está activo, forzamos la configuración. Si no, usamos la del usuario.
    val effectiveTheme = if (isOutdoorMode) ThemeMode.Light else userTheme
    val effectiveContrast = if (isOutdoorMode) ContrastMode.HighContrast else userContrast

    // Brillo de la pantalla
    val brightnessManager = remember { getBrightnessManager() }

    // EFECTO REACTIVO:
    // Cada vez que 'isOutdoorMode' cambie, ejecutamos esto.
    LaunchedEffect(isOutdoorMode) {
        if (isOutdoorMode) {
            brightnessManager.setBrightness(1.0f) // 100% Brillo
        } else {
            brightnessManager.setBrightness(null) // Restaurar brillo del sistema
        }
    }

    // Scope para lanzar corrutinas de guardado
    val scope = rememberCoroutineScope()

    AppTheme(effectiveTheme, effectiveContrast) {
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
        val topBarTitle = when (currentTab) {
            BottomTab.CALCULATOR -> (calculatorStack.lastOrNull() ?: Screen.Home).title
            BottomTab.SAVED -> Screen.Guardados.title
            BottomTab.SETTINGS -> when (settingsSection) {
                SettingsSection.MENU -> "Configuración"
                SettingsSection.APPEARANCE -> "Apariencia"
                SettingsSection.GLOBAL_PARAMS -> "Parámetros Globales"
                SettingsSection.MATERIALS_DB -> "Materiales"
                SettingsSection.PRICES -> "Precios"
            }
        }

        // --- LÓGICA DE BOTÓN ATRÁS ---
        // Mostrar flecha si:
        // 1. En Calculadora hay pantallas apiladas.
        // 2. O en Ajustes estamos en un submenú.
        val showBackArrow = (currentTab == BottomTab.CALCULATOR && calculatorStack.size > 1) ||
                (currentTab == BottomTab.SETTINGS && settingsSection != SettingsSection.MENU)

        // --- LÓGICA DE BACK ---
        val navigateBack: () -> Unit = {
            if (currentTab == BottomTab.CALCULATOR && calculatorStack.size > 1) {
                // Caso 1: Volver en Calculadora
                calculatorStack.removeAt(calculatorStack.lastIndex)
            } else if (currentTab == BottomTab.SETTINGS && settingsSection != SettingsSection.MENU) {
                // Caso 2: Volver en Ajustes (Submenú -> Menú)
                settingsSection = SettingsSection.MENU
            } else if (currentTab != BottomTab.CALCULATOR) {
                // Caso 3: Volver al Tab Home desde otros tabs
                scope.launch { pagerState.animateScrollToPage(BottomTab.CALCULATOR.ordinal) }
            }
        }

        // Activamos el BackHandler del sistema (Botón físico de Android) si:
        // 1. Estamos en la calculadora y hay historial de pantallas.
        // 2. O si estamos en otra pestaña (para volver a la calculadora antes de salir).
        KmpBackHandler(
            enabled = showBackArrow || currentTab != BottomTab.CALCULATOR
        ) {
            navigateBack()
        }

        Scaffold(
            topBar = {
                AppTopBar(
                    title = topBarTitle, // Usamos el título dinámico
                    showBackButton = showBackArrow, // Usamos la lógica combinada
                    onBack = navigateBack,
                    isOutdoorMode = isOutdoorMode,
                    onToggleOutdoorMode = {
                        // Guardamos el nuevo estado en Settings
                        scope.launch {
                            settingsRepository.saveOutdoorMode(!isOutdoorMode)
                        }
                    }
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

                        // 2. Lógica de "Reselección" (Si tocas la pestaña en la que ya estás)
                        if (newTab == currentTab) {
                            // A. Para CALCULADORA: Volver al Home (Root)
                            if (newTab == BottomTab.CALCULATOR) {
                                if (calculatorStack.size > 1) {
                                    calculatorStack.clear()
                                    calculatorStack.add(Screen.Home)
                                }
                            }

                            // B. Para AJUSTES: Volver al Menú Principal (Root)
                            if (newTab == BottomTab.SETTINGS) {
                                settingsSection = SettingsSection.MENU
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
                    .imePadding() // Aplicamos el padding del teclado SOLO al contenido.
                    .clearFocusOnTap(), // Cierre del teclado
                color = MaterialTheme.colorScheme.background
            ) {
                HorizontalPager(
                    state = pagerState,
                    // Opcional: userScrollEnabled = false (si quisieras bloquear el swipe)
                ) { pageIndex ->

                    // Renderizamos el contenido según la página (0, 1 o 2)
                    when (BottomTab.entries[pageIndex]) {

                        BottomTab.CALCULATOR -> {
                            // Pestaña 1
                            CalculatorTabContent(
                                currentScreen = calculatorStack.lastOrNull() ?: Screen.Home,
                                settingsRepository = settingsRepository,
                                onNavigate = { newScreen -> calculatorStack.add(newScreen) }
                            )
                        }

                        BottomTab.SAVED -> {
                            // Pestaña 2
                            SavedScreen()
                        }

                        BottomTab.SETTINGS -> {
                            // Pestaña 3
                            SettingsScreen(
                                repository = settingsRepository, // Pasamos el repo completo
                                currentTheme = userTheme,
                                currentContrast = userContrast,
                                currentOutdoorMode = isOutdoorMode,
                                currentSection = settingsSection, // Pasamos el estado de App
                                onThemeChange = { scope.launch { settingsRepository.saveThemeMode(it) } },
                                onContrastChange = { scope.launch { settingsRepository.saveContrastMode(it) } },
                                onOutdoorModeChange = { scope.launch { settingsRepository.saveOutdoorMode(it) } },
                                onSectionChange = { settingsSection = it } // Actualizamos el estado de App
                            )
                        }
                    }
                }
            }
        }
    }
}