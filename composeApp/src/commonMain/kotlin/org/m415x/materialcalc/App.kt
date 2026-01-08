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

package org.m415x.materialcalc

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import materialscalculator.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.m415x.materialcalc.data.repository.SettingsRepository
import org.m415x.materialcalc.domain.model.AppSettingsState
import org.m415x.materialcalc.ui.common.*
import org.m415x.materialcalc.ui.navigation.BottomTab
import org.m415x.materialcalc.ui.navigation.Screen
import org.m415x.materialcalc.ui.screen.calculator.CalculatorTabContent
import org.m415x.materialcalc.ui.screen.saved.SavedScreen
import org.m415x.materialcalc.ui.screen.settings.SettingsScreen
import org.m415x.materialcalc.ui.screen.settings.SettingsSection
import org.m415x.materialcalc.ui.theme.AppTheme
import org.m415x.materialcalc.ui.theme.ContrastMode
import org.m415x.materialcalc.ui.theme.ThemeMode

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
    // --- ESTADO GLOBAL DE LA APP ---
    val appSettings by produceState(initialValue = AppSettingsState()) {
        // Lanzamos cada recolección en una corrutina separada para no bloquear
        launch { settingsRepository.themeMode.collect { value = value.copy(themeMode = it) } }
        launch { settingsRepository.contrastMode.collect { value = value.copy(contrastMode = it) } }
        launch { settingsRepository.colorPalette.collect { value = value.copy(colorPalette = it) } }
        launch { settingsRepository.outdoorMode.collect { value = value.copy(isOutdoorMode = it) } }

        launch { settingsRepository.bagCementKg.collect { value = value.copy(bagCementKg = it) } }
        launch { settingsRepository.bagLimeKg.collect { value = value.copy(bagLimeKg = it) } }
        launch { settingsRepository.bagPremixKg.collect { value = value.copy(bagPremixKg = it) } }

        launch { settingsRepository.bucketCapacityLiters.collect { value = value.copy(bucketVolL = it) } }
        launch { settingsRepository.barrowCapacityLiters.collect { value = value.copy(barrowVolL = it) } }
        launch { settingsRepository.mixerCapacityLiters.collect { value = value.copy(mixerVolL = it) } }

        launch { settingsRepository.wasteConcretePct.collect { value = value.copy(wasteConcretePct = it) } }
        launch { settingsRepository.wasteMortarPct.collect { value = value.copy(wasteMortarPct = it) } }
        launch { settingsRepository.wasteBricksPct.collect { value = value.copy(wasteBrickPct = it) } }
        launch { settingsRepository.wasteIronMainPct.collect { value = value.copy(wasteIronMainPct = it) } }
        launch { settingsRepository.wasteIronStirrupPct.collect { value = value.copy(wasteIronStirrupPct = it) } }
        launch { settingsRepository.wastePlasterPct.collect { value = value.copy(wastePlasterPct = it) } }

        launch { settingsRepository.fineThicknessMm.collect { value = value.copy(fineThicknessMm = it) } }

        launch { settingsRepository.defaultBrickId.collect { value = value.copy(defaultBrickId = it) } }
        launch { settingsRepository.defaultConcreteGenId.collect { value = value.copy(defaultConcreteGenId = it) } }
        launch { settingsRepository.defaultConcreteStrId.collect { value = value.copy(defaultConcreteStrId = it) } }
        launch { settingsRepository.defaultPlasterRoughId.collect { value = value.copy(defaultPlasterId = it) } }

        launch { settingsRepository.customBricks.collect { value = value.copy(customBricks = it) } }
        launch { settingsRepository.customIrons.collect { value = value.copy(customIrons = it) } }
        launch { settingsRepository.customRecipes.collect { value = value.copy(customRecipes = it) } }

        launch { settingsRepository.hiddenBrickIds.collect { value = value.copy(hiddenBrickIds = it) } }
        launch { settingsRepository.hiddenIronIds.collect { value = value.copy(hiddenIronIds = it) } }
        launch { settingsRepository.hiddenRecipeIds.collect { value = value.copy(hiddenRecipeIds = it) } }
    }

    var settingsSection by remember { mutableStateOf(SettingsSection.MENU) }

    // Si Modo Exterior está activo, forzamos la configuración. Si no, usamos la del usuario.
    val effectiveTheme = if (appSettings.isOutdoorMode) ThemeMode.Light else appSettings.themeMode
    val effectiveContrast = if (appSettings.isOutdoorMode) ContrastMode.HighContrast else appSettings.contrastMode

    // Brillo de la pantalla
    val brightnessManager = remember { getBrightnessManager() }

    // EFECTO REACTIVO:
    // Cada vez que 'isOutdoorMode' cambie, ejecutamos esto.
    LaunchedEffect(appSettings.isOutdoorMode) {
        if (appSettings.isOutdoorMode) {
            brightnessManager.setBrightness(1.0f) // 100% Brillo
        } else {
            brightnessManager.setBrightness(null) // Restaurar brillo del sistema
        }
    }

    // Scope para lanzar corrutinas de guardado
    val scope = rememberCoroutineScope()

    AppTheme(effectiveTheme, effectiveContrast, appSettings.colorPalette) {
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
            BottomTab.CALCULATOR -> stringResource((calculatorStack.lastOrNull() ?: Screen.Home).title)
            BottomTab.SAVED -> stringResource(Screen.Saved.title)
            BottomTab.SETTINGS -> when (settingsSection) {
                // Mapeamos el Enum de Settings directamente a recursos
                SettingsSection.MENU -> stringResource(Res.string.settings_title_main)
                SettingsSection.APPEARANCE -> stringResource(Res.string.settings_item_appearance_title)
                SettingsSection.GLOBAL_PARAMS -> stringResource(Res.string.settings_item_params_title)
                SettingsSection.MATERIALS_DB -> stringResource(Res.string.settings_item_materials_title)
                SettingsSection.PRICES -> stringResource(Res.string.settings_item_prices_title)
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
                    isOutdoorMode = appSettings.isOutdoorMode,
                    onToggleOutdoorMode = {
                        // Guardamos el nuevo estado en Settings
                        scope.launch {
                            settingsRepository.saveOutdoorMode(!appSettings.isOutdoorMode)
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
                                appSettings = appSettings, // Pasamos el estado
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
                                repository = settingsRepository, // Pasamos el repo para guardar
                                appSettings = appSettings, // Pasamos el estado
                                currentSection = settingsSection, // Pasamos el estado de App
                                onSectionChange = { settingsSection = it } // Actualizamos el estado de App
                            )
                        }
                    }
                }
            }
        }
    }
}