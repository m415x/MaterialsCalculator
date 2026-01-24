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

package org.m415x.materialcalc.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Contenedor para colores personalizados que no están en el esquema estándar de Material 3.
 */
@Immutable
data class AppCustomColors(
    val warning: Color = Color.Unspecified,
    val onWarning: Color = Color.Unspecified,
    val warningContainer: Color = Color.Unspecified,
    val onWarningContainer: Color = Color.Unspecified,
)

/**
 * CompositionLocal para proveer los colores personalizados.
 */
val LocalAppCustomColors = staticCompositionLocalOf { AppCustomColors() }

/**
 * Extensión para acceder a los colores personalizados desde MaterialTheme.
 * Uso: MaterialTheme.customColors.warning
 */
val MaterialTheme.customColors: AppCustomColors
    @Composable
    @ReadOnlyComposable
    get() = LocalAppCustomColors.current

// --- DEFAULT COLORS ---
// Light
val customColorsLight = AppCustomColors(
    warning = ThemeDefaultColors.warningLight,
    onWarning = ThemeDefaultColors.onWarningLight,
    warningContainer = ThemeDefaultColors.warningContainerLight,
    onWarningContainer = ThemeDefaultColors.onWarningContainerLight
)
// Light High Contrast
val customColorsLightHighContrast = AppCustomColors(
    warning = ThemeDefaultColors.warningLightHighContrast,
    onWarning = ThemeDefaultColors.onWarningLightHighContrast,
    warningContainer = ThemeDefaultColors.warningContainerLightHighContrast,
    onWarningContainer = ThemeDefaultColors.onWarningContainerLightHighContrast
)
// Dark
val customColorsDark = AppCustomColors(
    warning = ThemeDefaultColors.warningDark,
    onWarning = ThemeDefaultColors.onWarningDark,
    warningContainer = ThemeDefaultColors.warningContainerDark,
    onWarningContainer = ThemeDefaultColors.onWarningContainerDark
)
// Dark High Contrast
val customColorsDarkHighContrast = AppCustomColors(
    warning = ThemeDefaultColors.warningDarkHighContrast,
    onWarning = ThemeDefaultColors.onWarningDarkHighContrast,
    warningContainer = ThemeDefaultColors.warningContainerDarkHighContrast,
    onWarningContainer = ThemeDefaultColors.onWarningContainerDarkHighContrast
)

// --- INDUSTRIAL COLORS ---
// Light
val industrialColorsLight = AppCustomColors(
    warning = ThemeIndustrialColors.warningLight,
    onWarning = ThemeIndustrialColors.onWarningLight,
    warningContainer = ThemeIndustrialColors.warningContainerLight,
    onWarningContainer = ThemeIndustrialColors.onWarningContainerLight
)
// Light High Contrast
val industrialColorsLightHighContrast = AppCustomColors(
    warning = ThemeIndustrialColors.warningLightHighContrast,
    onWarning = ThemeIndustrialColors.onWarningLightHighContrast,
    warningContainer = ThemeIndustrialColors.warningContainerLightHighContrast,
    onWarningContainer = ThemeIndustrialColors.onWarningContainerLightHighContrast
)
// Dark
val industrialColorsDark = AppCustomColors(
    warning = ThemeIndustrialColors.warningDark,
    onWarning = ThemeIndustrialColors.onWarningDark,
    warningContainer = ThemeIndustrialColors.warningContainerDark,
    onWarningContainer = ThemeIndustrialColors.onWarningContainerDark
)
// Dark High Contrast
val industrialColorsDarkHighContrast = AppCustomColors(
    warning = ThemeIndustrialColors.warningDarkHighContrast,
    onWarning = ThemeIndustrialColors.onWarningDarkHighContrast,
    warningContainer = ThemeIndustrialColors.warningContainerDarkHighContrast,
    onWarningContainer = ThemeIndustrialColors.onWarningContainerDarkHighContrast
)

// --- SECURITY COLORS ---
// Light
val securityColorsLight = AppCustomColors(
    warning = ThemeSecurityColors.warningLight,
    onWarning = ThemeSecurityColors.onWarningLight,
    warningContainer = ThemeSecurityColors.warningContainerLight,
    onWarningContainer = ThemeSecurityColors.onWarningContainerLight
)
// Light High Contrast
val securityColorsLightHighContrast = AppCustomColors(
    warning = ThemeSecurityColors.warningLightHighContrast,
    onWarning = ThemeSecurityColors.onWarningLightHighContrast,
    warningContainer = ThemeSecurityColors.warningContainerLightHighContrast,
    onWarningContainer = ThemeSecurityColors.onWarningContainerLightHighContrast
)
// Dark
val securityColorsDark = AppCustomColors(
    warning = ThemeSecurityColors.warningDark,
    onWarning = ThemeSecurityColors.onWarningDark,
    warningContainer = ThemeSecurityColors.warningContainerDark,
    onWarningContainer = ThemeSecurityColors.onWarningContainerDark
)
// Dark High Contrast
val securityColorsDarkHighContrast = AppCustomColors(
    warning = ThemeSecurityColors.warningDarkHighContrast,
    onWarning = ThemeSecurityColors.onWarningDarkHighContrast,
    warningContainer = ThemeSecurityColors.warningContainerDarkHighContrast,
    onWarningContainer = ThemeSecurityColors.onWarningContainerDarkHighContrast
)