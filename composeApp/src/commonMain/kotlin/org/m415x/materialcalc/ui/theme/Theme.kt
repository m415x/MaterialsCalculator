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

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

enum class ColorPalette {
    Default,
    Industrial,
    Security
}

// --- DEFAULT SCHEMES ---
private val defaultLightScheme = lightColorScheme(
    primary = ThemeDefaultColors.primaryLight,
    onPrimary = ThemeDefaultColors.onPrimaryLight,
    primaryContainer = ThemeDefaultColors.primaryContainerLight,
    onPrimaryContainer = ThemeDefaultColors.onPrimaryContainerLight,
    secondary = ThemeDefaultColors.secondaryLight,
    onSecondary = ThemeDefaultColors.onSecondaryLight,
    secondaryContainer = ThemeDefaultColors.secondaryContainerLight,
    onSecondaryContainer = ThemeDefaultColors.onSecondaryContainerLight,
    tertiary = ThemeDefaultColors.tertiaryLight,
    onTertiary = ThemeDefaultColors.onTertiaryLight,
    tertiaryContainer = ThemeDefaultColors.tertiaryContainerLight,
    onTertiaryContainer = ThemeDefaultColors.onTertiaryContainerLight,
    error = ThemeDefaultColors.errorLight,
    onError = ThemeDefaultColors.onErrorLight,
    errorContainer = ThemeDefaultColors.errorContainerLight,
    onErrorContainer = ThemeDefaultColors.onErrorContainerLight,
    background = ThemeDefaultColors.backgroundLight,
    onBackground = ThemeDefaultColors.onBackgroundLight,
    surface = ThemeDefaultColors.surfaceLight,
    onSurface = ThemeDefaultColors.onSurfaceLight,
    surfaceVariant = ThemeDefaultColors.surfaceVariantLight,
    onSurfaceVariant = ThemeDefaultColors.onSurfaceVariantLight,
    outline = ThemeDefaultColors.outlineLight,
    outlineVariant = ThemeDefaultColors.outlineVariantLight,
    scrim = ThemeDefaultColors.scrimLight,
    inverseSurface = ThemeDefaultColors.inverseSurfaceLight,
    inverseOnSurface = ThemeDefaultColors.inverseOnSurfaceLight,
    inversePrimary = ThemeDefaultColors.inversePrimaryLight,
    surfaceDim = ThemeDefaultColors.surfaceDimLight,
    surfaceBright = ThemeDefaultColors.surfaceBrightLight,
    surfaceContainerLowest = ThemeDefaultColors.surfaceContainerLowestLight,
    surfaceContainerLow = ThemeDefaultColors.surfaceContainerLowLight,
    surfaceContainer = ThemeDefaultColors.surfaceContainerLight,
    surfaceContainerHigh = ThemeDefaultColors.surfaceContainerHighLight,
    surfaceContainerHighest = ThemeDefaultColors.surfaceContainerHighestLight,
)

private val defaultDarkScheme = darkColorScheme(
    primary = ThemeDefaultColors.primaryDark,
    onPrimary = ThemeDefaultColors.onPrimaryDark,
    primaryContainer = ThemeDefaultColors.primaryContainerDark,
    onPrimaryContainer = ThemeDefaultColors.onPrimaryContainerDark,
    secondary = ThemeDefaultColors.secondaryDark,
    onSecondary = ThemeDefaultColors.onSecondaryDark,
    secondaryContainer = ThemeDefaultColors.secondaryContainerDark,
    onSecondaryContainer = ThemeDefaultColors.onSecondaryContainerDark,
    tertiary = ThemeDefaultColors.tertiaryDark,
    onTertiary = ThemeDefaultColors.onTertiaryDark,
    tertiaryContainer = ThemeDefaultColors.tertiaryContainerDark,
    onTertiaryContainer = ThemeDefaultColors.onTertiaryContainerDark,
    error = ThemeDefaultColors.errorDark,
    onError = ThemeDefaultColors.onErrorDark,
    errorContainer = ThemeDefaultColors.errorContainerDark,
    onErrorContainer = ThemeDefaultColors.onErrorContainerDark,
    background = ThemeDefaultColors.backgroundDark,
    onBackground = ThemeDefaultColors.onBackgroundDark,
    surface = ThemeDefaultColors.surfaceDark,
    onSurface = ThemeDefaultColors.onSurfaceDark,
    surfaceVariant = ThemeDefaultColors.surfaceVariantDark,
    onSurfaceVariant = ThemeDefaultColors.onSurfaceVariantDark,
    outline = ThemeDefaultColors.outlineDark,
    outlineVariant = ThemeDefaultColors.outlineVariantDark,
    scrim = ThemeDefaultColors.scrimDark,
    inverseSurface = ThemeDefaultColors.inverseSurfaceDark,
    inverseOnSurface = ThemeDefaultColors.inverseOnSurfaceDark,
    inversePrimary = ThemeDefaultColors.inversePrimaryDark,
    surfaceDim = ThemeDefaultColors.surfaceDimDark,
    surfaceBright = ThemeDefaultColors.surfaceBrightDark,
    surfaceContainerLowest = ThemeDefaultColors.surfaceContainerLowestDark,
    surfaceContainerLow = ThemeDefaultColors.surfaceContainerLowDark,
    surfaceContainer = ThemeDefaultColors.surfaceContainerDark,
    surfaceContainerHigh = ThemeDefaultColors.surfaceContainerHighDark,
    surfaceContainerHighest = ThemeDefaultColors.surfaceContainerHighestDark,
)

private val defaultLightHighContrastScheme = lightColorScheme(
    primary = ThemeDefaultColors.primaryLightHighContrast,
    onPrimary = ThemeDefaultColors.onPrimaryLightHighContrast,
    primaryContainer = ThemeDefaultColors.primaryContainerLightHighContrast,
    onPrimaryContainer = ThemeDefaultColors.onPrimaryContainerLightHighContrast,
    secondary = ThemeDefaultColors.secondaryLightHighContrast,
    onSecondary = ThemeDefaultColors.onSecondaryLightHighContrast,
    secondaryContainer = ThemeDefaultColors.secondaryContainerLightHighContrast,
    onSecondaryContainer = ThemeDefaultColors.onSecondaryContainerLightHighContrast,
    tertiary = ThemeDefaultColors.tertiaryLightHighContrast,
    onTertiary = ThemeDefaultColors.onTertiaryLightHighContrast,
    tertiaryContainer = ThemeDefaultColors.tertiaryContainerLightHighContrast,
    onTertiaryContainer = ThemeDefaultColors.onTertiaryContainerLightHighContrast,
    error = ThemeDefaultColors.errorLightHighContrast,
    onError = ThemeDefaultColors.onErrorLightHighContrast,
    errorContainer = ThemeDefaultColors.errorContainerLightHighContrast,
    onErrorContainer = ThemeDefaultColors.onErrorContainerLightHighContrast,
    background = ThemeDefaultColors.backgroundLightHighContrast,
    onBackground = ThemeDefaultColors.onBackgroundLightHighContrast,
    surface = ThemeDefaultColors.surfaceLightHighContrast,
    onSurface = ThemeDefaultColors.onSurfaceLightHighContrast,
    surfaceVariant = ThemeDefaultColors.surfaceVariantLightHighContrast,
    onSurfaceVariant = ThemeDefaultColors.onSurfaceVariantLightHighContrast,
    outline = ThemeDefaultColors.outlineLightHighContrast,
    outlineVariant = ThemeDefaultColors.outlineVariantLightHighContrast,
    scrim = ThemeDefaultColors.scrimLightHighContrast,
    inverseSurface = ThemeDefaultColors.inverseSurfaceLightHighContrast,
    inverseOnSurface = ThemeDefaultColors.inverseOnSurfaceLightHighContrast,
    inversePrimary = ThemeDefaultColors.inversePrimaryLightHighContrast,
    surfaceDim = ThemeDefaultColors.surfaceDimLightHighContrast,
    surfaceBright = ThemeDefaultColors.surfaceBrightLightHighContrast,
    surfaceContainerLowest = ThemeDefaultColors.surfaceContainerLowestLightHighContrast,
    surfaceContainerLow = ThemeDefaultColors.surfaceContainerLowLightHighContrast,
    surfaceContainer = ThemeDefaultColors.surfaceContainerLightHighContrast,
    surfaceContainerHigh = ThemeDefaultColors.surfaceContainerHighLightHighContrast,
    surfaceContainerHighest = ThemeDefaultColors.surfaceContainerHighestLightHighContrast,
)

private val defaultDarkHighContrastScheme = darkColorScheme(
    primary = ThemeDefaultColors.primaryDarkHighContrast,
    onPrimary = ThemeDefaultColors.onPrimaryDarkHighContrast,
    primaryContainer = ThemeDefaultColors.primaryContainerDarkHighContrast,
    onPrimaryContainer = ThemeDefaultColors.onPrimaryContainerDarkHighContrast,
    secondary = ThemeDefaultColors.secondaryDarkHighContrast,
    onSecondary = ThemeDefaultColors.onSecondaryDarkHighContrast,
    secondaryContainer = ThemeDefaultColors.secondaryContainerDarkHighContrast,
    onSecondaryContainer = ThemeDefaultColors.onSecondaryContainerDarkHighContrast,
    tertiary = ThemeDefaultColors.tertiaryDarkHighContrast,
    onTertiary = ThemeDefaultColors.onTertiaryDarkHighContrast,
    tertiaryContainer = ThemeDefaultColors.tertiaryContainerDarkHighContrast,
    onTertiaryContainer = ThemeDefaultColors.onTertiaryContainerDarkHighContrast,
    error = ThemeDefaultColors.errorDarkHighContrast,
    onError = ThemeDefaultColors.onErrorDarkHighContrast,
    errorContainer = ThemeDefaultColors.errorContainerDarkHighContrast,
    onErrorContainer = ThemeDefaultColors.onErrorContainerDarkHighContrast,
    background = ThemeDefaultColors.backgroundDarkHighContrast,
    onBackground = ThemeDefaultColors.onBackgroundDarkHighContrast,
    surface = ThemeDefaultColors.surfaceDarkHighContrast,
    onSurface = ThemeDefaultColors.onSurfaceDarkHighContrast,
    surfaceVariant = ThemeDefaultColors.surfaceVariantDarkHighContrast,
    onSurfaceVariant = ThemeDefaultColors.onSurfaceVariantDarkHighContrast,
    outline = ThemeDefaultColors.outlineDarkHighContrast,
    outlineVariant = ThemeDefaultColors.outlineVariantDarkHighContrast,
    scrim = ThemeDefaultColors.scrimDarkHighContrast,
    inverseSurface = ThemeDefaultColors.inverseSurfaceDarkHighContrast,
    inverseOnSurface = ThemeDefaultColors.inverseOnSurfaceDarkHighContrast,
    inversePrimary = ThemeDefaultColors.inversePrimaryDarkHighContrast,
    surfaceDim = ThemeDefaultColors.surfaceDimDarkHighContrast,
    surfaceBright = ThemeDefaultColors.surfaceBrightDarkHighContrast,
    surfaceContainerLowest = ThemeDefaultColors.surfaceContainerLowestDarkHighContrast,
    surfaceContainerLow = ThemeDefaultColors.surfaceContainerLowDarkHighContrast,
    surfaceContainer = ThemeDefaultColors.surfaceContainerDarkHighContrast,
    surfaceContainerHigh = ThemeDefaultColors.surfaceContainerHighDarkHighContrast,
    surfaceContainerHighest = ThemeDefaultColors.surfaceContainerHighestDarkHighContrast,
)

// --- INDUSTRIAL SCHEMES ---
private val industrialLightScheme = lightColorScheme(
    primary = ThemeIndustrialColors.primaryLight,
    onPrimary = ThemeIndustrialColors.onPrimaryLight,
    primaryContainer = ThemeIndustrialColors.primaryContainerLight,
    onPrimaryContainer = ThemeIndustrialColors.onPrimaryContainerLight,
    secondary = ThemeIndustrialColors.secondaryLight,
    onSecondary = ThemeIndustrialColors.onSecondaryLight,
    secondaryContainer = ThemeIndustrialColors.secondaryContainerLight,
    onSecondaryContainer = ThemeIndustrialColors.onSecondaryContainerLight,
    tertiary = ThemeIndustrialColors.tertiaryLight,
    onTertiary = ThemeIndustrialColors.onTertiaryLight,
    tertiaryContainer = ThemeIndustrialColors.tertiaryContainerLight,
    onTertiaryContainer = ThemeIndustrialColors.onTertiaryContainerLight,
    error = ThemeIndustrialColors.errorLight,
    onError = ThemeIndustrialColors.onErrorLight,
    errorContainer = ThemeIndustrialColors.errorContainerLight,
    onErrorContainer = ThemeIndustrialColors.onErrorContainerLight,
    background = ThemeIndustrialColors.backgroundLight,
    onBackground = ThemeIndustrialColors.onBackgroundLight,
    surface = ThemeIndustrialColors.surfaceLight,
    onSurface = ThemeIndustrialColors.onSurfaceLight,
    surfaceVariant = ThemeIndustrialColors.surfaceVariantLight,
    onSurfaceVariant = ThemeIndustrialColors.onSurfaceVariantLight,
    outline = ThemeIndustrialColors.outlineLight,
    outlineVariant = ThemeIndustrialColors.outlineVariantLight,
    scrim = ThemeIndustrialColors.scrimLight,
    inverseSurface = ThemeIndustrialColors.inverseSurfaceLight,
    inverseOnSurface = ThemeIndustrialColors.inverseOnSurfaceLight,
    inversePrimary = ThemeIndustrialColors.inversePrimaryLight,
    surfaceDim = ThemeIndustrialColors.surfaceDimLight,
    surfaceBright = ThemeIndustrialColors.surfaceBrightLight,
    surfaceContainerLowest = ThemeIndustrialColors.surfaceContainerLowestLight,
    surfaceContainerLow = ThemeIndustrialColors.surfaceContainerLowLight,
    surfaceContainer = ThemeIndustrialColors.surfaceContainerLight,
    surfaceContainerHigh = ThemeIndustrialColors.surfaceContainerHighLight,
    surfaceContainerHighest = ThemeIndustrialColors.surfaceContainerHighestLight,
)

private val industrialDarkScheme = darkColorScheme(
    primary = ThemeIndustrialColors.primaryDark,
    onPrimary = ThemeIndustrialColors.onPrimaryDark,
    primaryContainer = ThemeIndustrialColors.primaryContainerDark,
    onPrimaryContainer = ThemeIndustrialColors.onPrimaryContainerDark,
    secondary = ThemeIndustrialColors.secondaryDark,
    onSecondary = ThemeIndustrialColors.onSecondaryDark,
    secondaryContainer = ThemeIndustrialColors.secondaryContainerDark,
    onSecondaryContainer = ThemeIndustrialColors.onSecondaryContainerDark,
    tertiary = ThemeIndustrialColors.tertiaryDark,
    onTertiary = ThemeIndustrialColors.onTertiaryDark,
    tertiaryContainer = ThemeIndustrialColors.tertiaryContainerDark,
    onTertiaryContainer = ThemeIndustrialColors.onTertiaryContainerDark,
    error = ThemeIndustrialColors.errorDark,
    onError = ThemeIndustrialColors.onErrorDark,
    errorContainer = ThemeIndustrialColors.errorContainerDark,
    onErrorContainer = ThemeIndustrialColors.onErrorContainerDark,
    background = ThemeIndustrialColors.backgroundDark,
    onBackground = ThemeIndustrialColors.onBackgroundDark,
    surface = ThemeIndustrialColors.surfaceDark,
    onSurface = ThemeIndustrialColors.onSurfaceDark,
    surfaceVariant = ThemeIndustrialColors.surfaceVariantDark,
    onSurfaceVariant = ThemeIndustrialColors.onSurfaceVariantDark,
    outline = ThemeIndustrialColors.outlineDark,
    outlineVariant = ThemeIndustrialColors.outlineVariantDark,
    scrim = ThemeIndustrialColors.scrimDark,
    inverseSurface = ThemeIndustrialColors.inverseSurfaceDark,
    inverseOnSurface = ThemeIndustrialColors.inverseOnSurfaceDark,
    inversePrimary = ThemeIndustrialColors.inversePrimaryDark,
    surfaceDim = ThemeIndustrialColors.surfaceDimDark,
    surfaceBright = ThemeIndustrialColors.surfaceBrightDark,
    surfaceContainerLowest = ThemeIndustrialColors.surfaceContainerLowestDark,
    surfaceContainerLow = ThemeIndustrialColors.surfaceContainerLowDark,
    surfaceContainer = ThemeIndustrialColors.surfaceContainerDark,
    surfaceContainerHigh = ThemeIndustrialColors.surfaceContainerHighDark,
    surfaceContainerHighest = ThemeIndustrialColors.surfaceContainerHighestDark,
)

private val industrialLightHighContrastScheme = lightColorScheme(
    primary = ThemeIndustrialColors.primaryLightHighContrast,
    onPrimary = ThemeIndustrialColors.onPrimaryLightHighContrast,
    primaryContainer = ThemeIndustrialColors.primaryContainerLightHighContrast,
    onPrimaryContainer = ThemeIndustrialColors.onPrimaryContainerLightHighContrast,
    secondary = ThemeIndustrialColors.secondaryLightHighContrast,
    onSecondary = ThemeIndustrialColors.onSecondaryLightHighContrast,
    secondaryContainer = ThemeIndustrialColors.secondaryContainerLightHighContrast,
    onSecondaryContainer = ThemeIndustrialColors.onSecondaryContainerLightHighContrast,
    tertiary = ThemeIndustrialColors.tertiaryLightHighContrast,
    onTertiary = ThemeIndustrialColors.onTertiaryLightHighContrast,
    tertiaryContainer = ThemeIndustrialColors.tertiaryContainerLightHighContrast,
    onTertiaryContainer = ThemeIndustrialColors.onTertiaryContainerLightHighContrast,
    error = ThemeIndustrialColors.errorLightHighContrast,
    onError = ThemeIndustrialColors.onErrorLightHighContrast,
    errorContainer = ThemeIndustrialColors.errorContainerLightHighContrast,
    onErrorContainer = ThemeIndustrialColors.onErrorContainerLightHighContrast,
    background = ThemeIndustrialColors.backgroundLightHighContrast,
    onBackground = ThemeIndustrialColors.onBackgroundLightHighContrast,
    surface = ThemeIndustrialColors.surfaceLightHighContrast,
    onSurface = ThemeIndustrialColors.onSurfaceLightHighContrast,
    surfaceVariant = ThemeIndustrialColors.surfaceVariantLightHighContrast,
    onSurfaceVariant = ThemeIndustrialColors.onSurfaceVariantLightHighContrast,
    outline = ThemeIndustrialColors.outlineLightHighContrast,
    outlineVariant = ThemeIndustrialColors.outlineVariantLightHighContrast,
    scrim = ThemeIndustrialColors.scrimLightHighContrast,
    inverseSurface = ThemeIndustrialColors.inverseSurfaceLightHighContrast,
    inverseOnSurface = ThemeIndustrialColors.inverseOnSurfaceLightHighContrast,
    inversePrimary = ThemeIndustrialColors.inversePrimaryLightHighContrast,
    surfaceDim = ThemeIndustrialColors.surfaceDimLightHighContrast,
    surfaceBright = ThemeIndustrialColors.surfaceBrightLightHighContrast,
    surfaceContainerLowest = ThemeIndustrialColors.surfaceContainerLowestLightHighContrast,
    surfaceContainerLow = ThemeIndustrialColors.surfaceContainerLowLightHighContrast,
    surfaceContainer = ThemeIndustrialColors.surfaceContainerLightHighContrast,
    surfaceContainerHigh = ThemeIndustrialColors.surfaceContainerHighLightHighContrast,
    surfaceContainerHighest = ThemeIndustrialColors.surfaceContainerHighestLightHighContrast,
)

private val industrialDarkHighContrastScheme = darkColorScheme(
    primary = ThemeIndustrialColors.primaryDarkHighContrast,
    onPrimary = ThemeIndustrialColors.onPrimaryDarkHighContrast,
    primaryContainer = ThemeIndustrialColors.primaryContainerDarkHighContrast,
    onPrimaryContainer = ThemeIndustrialColors.onPrimaryContainerDarkHighContrast,
    secondary = ThemeIndustrialColors.secondaryDarkHighContrast,
    onSecondary = ThemeIndustrialColors.onSecondaryDarkHighContrast,
    secondaryContainer = ThemeIndustrialColors.secondaryContainerDarkHighContrast,
    onSecondaryContainer = ThemeIndustrialColors.onSecondaryContainerDarkHighContrast,
    tertiary = ThemeIndustrialColors.tertiaryDarkHighContrast,
    onTertiary = ThemeIndustrialColors.onTertiaryDarkHighContrast,
    tertiaryContainer = ThemeIndustrialColors.tertiaryContainerDarkHighContrast,
    onTertiaryContainer = ThemeIndustrialColors.onTertiaryContainerDarkHighContrast,
    error = ThemeIndustrialColors.errorDarkHighContrast,
    onError = ThemeIndustrialColors.onErrorDarkHighContrast,
    errorContainer = ThemeIndustrialColors.errorContainerDarkHighContrast,
    onErrorContainer = ThemeIndustrialColors.onErrorContainerDarkHighContrast,
    background = ThemeIndustrialColors.backgroundDarkHighContrast,
    onBackground = ThemeIndustrialColors.onBackgroundDarkHighContrast,
    surface = ThemeIndustrialColors.surfaceDarkHighContrast,
    onSurface = ThemeIndustrialColors.onSurfaceDarkHighContrast,
    surfaceVariant = ThemeIndustrialColors.surfaceVariantDarkHighContrast,
    onSurfaceVariant = ThemeIndustrialColors.onSurfaceVariantDarkHighContrast,
    outline = ThemeIndustrialColors.outlineDarkHighContrast,
    outlineVariant = ThemeIndustrialColors.outlineVariantDarkHighContrast,
    scrim = ThemeIndustrialColors.scrimDarkHighContrast,
    inverseSurface = ThemeIndustrialColors.inverseSurfaceDarkHighContrast,
    inverseOnSurface = ThemeIndustrialColors.inverseOnSurfaceDarkHighContrast,
    inversePrimary = ThemeIndustrialColors.inversePrimaryDarkHighContrast,
    surfaceDim = ThemeIndustrialColors.surfaceDimDarkHighContrast,
    surfaceBright = ThemeIndustrialColors.surfaceBrightDarkHighContrast,
    surfaceContainerLowest = ThemeIndustrialColors.surfaceContainerLowestDarkHighContrast,
    surfaceContainerLow = ThemeIndustrialColors.surfaceContainerLowDarkHighContrast,
    surfaceContainer = ThemeIndustrialColors.surfaceContainerDarkHighContrast,
    surfaceContainerHigh = ThemeIndustrialColors.surfaceContainerHighDarkHighContrast,
    surfaceContainerHighest = ThemeIndustrialColors.surfaceContainerHighestDarkHighContrast,
)

private val securityLightScheme = lightColorScheme(
    primary = ThemeSecurityColors.primaryLight,
    onPrimary = ThemeSecurityColors.onPrimaryLight,
    primaryContainer = ThemeSecurityColors.primaryContainerLight,
    onPrimaryContainer = ThemeSecurityColors.onPrimaryContainerLight,
    secondary = ThemeSecurityColors.secondaryLight,
    onSecondary = ThemeSecurityColors.onSecondaryLight,
    secondaryContainer = ThemeSecurityColors.secondaryContainerLight,
    onSecondaryContainer = ThemeSecurityColors.onSecondaryContainerLight,
    tertiary = ThemeSecurityColors.tertiaryLight,
    onTertiary = ThemeSecurityColors.onTertiaryLight,
    tertiaryContainer = ThemeSecurityColors.tertiaryContainerLight,
    onTertiaryContainer = ThemeSecurityColors.onTertiaryContainerLight,
    error = ThemeSecurityColors.errorLight,
    onError = ThemeSecurityColors.onErrorLight,
    errorContainer = ThemeSecurityColors.errorContainerLight,
    onErrorContainer = ThemeSecurityColors.onErrorContainerLight,
    background = ThemeSecurityColors.backgroundLight,
    onBackground = ThemeSecurityColors.onBackgroundLight,
    surface = ThemeSecurityColors.surfaceLight,
    onSurface = ThemeSecurityColors.onSurfaceLight,
    surfaceVariant = ThemeSecurityColors.surfaceVariantLight,
    onSurfaceVariant = ThemeSecurityColors.onSurfaceVariantLight,
    outline = ThemeSecurityColors.outlineLight,
    outlineVariant = ThemeSecurityColors.outlineVariantLight,
    scrim = ThemeSecurityColors.scrimLight,
    inverseSurface = ThemeSecurityColors.inverseSurfaceLight,
    inverseOnSurface = ThemeSecurityColors.inverseOnSurfaceLight,
    inversePrimary = ThemeSecurityColors.inversePrimaryLight,
    surfaceDim = ThemeSecurityColors.surfaceDimLight,
    surfaceBright = ThemeSecurityColors.surfaceBrightLight,
    surfaceContainerLowest = ThemeSecurityColors.surfaceContainerLowestLight,
    surfaceContainerLow = ThemeSecurityColors.surfaceContainerLowLight,
    surfaceContainer = ThemeSecurityColors.surfaceContainerLight,
    surfaceContainerHigh = ThemeSecurityColors.surfaceContainerHighLight,
    surfaceContainerHighest = ThemeSecurityColors.surfaceContainerHighestLight,
)

private val securityDarkScheme = darkColorScheme(
    primary = ThemeSecurityColors.primaryDark,
    onPrimary = ThemeSecurityColors.onPrimaryDark,
    primaryContainer = ThemeSecurityColors.primaryContainerDark,
    onPrimaryContainer = ThemeSecurityColors.onPrimaryContainerDark,
    secondary = ThemeSecurityColors.secondaryDark,
    onSecondary = ThemeSecurityColors.onSecondaryDark,
    secondaryContainer = ThemeSecurityColors.secondaryContainerDark,
    onSecondaryContainer = ThemeSecurityColors.onSecondaryContainerDark,
    tertiary = ThemeSecurityColors.tertiaryDark,
    onTertiary = ThemeSecurityColors.onTertiaryDark,
    tertiaryContainer = ThemeSecurityColors.tertiaryContainerDark,
    onTertiaryContainer = ThemeSecurityColors.onTertiaryContainerDark,
    error = ThemeSecurityColors.errorDark,
    onError = ThemeSecurityColors.onErrorDark,
    errorContainer = ThemeSecurityColors.errorContainerDark,
    onErrorContainer = ThemeSecurityColors.onErrorContainerDark,
    background = ThemeSecurityColors.backgroundDark,
    onBackground = ThemeSecurityColors.onBackgroundDark,
    surface = ThemeSecurityColors.surfaceDark,
    onSurface = ThemeSecurityColors.onSurfaceDark,
    surfaceVariant = ThemeSecurityColors.surfaceVariantDark,
    onSurfaceVariant = ThemeSecurityColors.onSurfaceVariantDark,
    outline = ThemeSecurityColors.outlineDark,
    outlineVariant = ThemeSecurityColors.outlineVariantDark,
    scrim = ThemeSecurityColors.scrimDark,
    inverseSurface = ThemeSecurityColors.inverseSurfaceDark,
    inverseOnSurface = ThemeSecurityColors.inverseOnSurfaceDark,
    inversePrimary = ThemeSecurityColors.inversePrimaryDark,
    surfaceDim = ThemeSecurityColors.surfaceDimDark,
    surfaceBright = ThemeSecurityColors.surfaceBrightDark,
    surfaceContainerLowest = ThemeSecurityColors.surfaceContainerLowestDark,
    surfaceContainerLow = ThemeSecurityColors.surfaceContainerLowDark,
    surfaceContainer = ThemeSecurityColors.surfaceContainerDark,
    surfaceContainerHigh = ThemeSecurityColors.surfaceContainerHighDark,
    surfaceContainerHighest = ThemeSecurityColors.surfaceContainerHighestDark,
)

private val securityLightHighContrastScheme = lightColorScheme(
    primary = ThemeSecurityColors.primaryLightHighContrast,
    onPrimary = ThemeSecurityColors.onPrimaryLightHighContrast,
    primaryContainer = ThemeSecurityColors.primaryContainerLightHighContrast,
    onPrimaryContainer = ThemeSecurityColors.onPrimaryContainerLightHighContrast,
    secondary = ThemeSecurityColors.secondaryLightHighContrast,
    onSecondary = ThemeSecurityColors.onSecondaryLightHighContrast,
    secondaryContainer = ThemeSecurityColors.secondaryContainerLightHighContrast,
    onSecondaryContainer = ThemeSecurityColors.onSecondaryContainerLightHighContrast,
    tertiary = ThemeSecurityColors.tertiaryLightHighContrast,
    onTertiary = ThemeSecurityColors.onTertiaryLightHighContrast,
    tertiaryContainer = ThemeSecurityColors.tertiaryContainerLightHighContrast,
    onTertiaryContainer = ThemeSecurityColors.onTertiaryContainerLightHighContrast,
    error = ThemeSecurityColors.errorLightHighContrast,
    onError = ThemeSecurityColors.onErrorLightHighContrast,
    errorContainer = ThemeSecurityColors.errorContainerLightHighContrast,
    onErrorContainer = ThemeSecurityColors.onErrorContainerLightHighContrast,
    background = ThemeSecurityColors.backgroundLightHighContrast,
    onBackground = ThemeSecurityColors.onBackgroundLightHighContrast,
    surface = ThemeSecurityColors.surfaceLightHighContrast,
    onSurface = ThemeSecurityColors.onSurfaceLightHighContrast,
    surfaceVariant = ThemeSecurityColors.surfaceVariantLightHighContrast,
    onSurfaceVariant = ThemeSecurityColors.onSurfaceVariantLightHighContrast,
    outline = ThemeSecurityColors.outlineLightHighContrast,
    outlineVariant = ThemeSecurityColors.outlineVariantLightHighContrast,
    scrim = ThemeSecurityColors.scrimLightHighContrast,
    inverseSurface = ThemeSecurityColors.inverseSurfaceLightHighContrast,
    inverseOnSurface = ThemeSecurityColors.inverseOnSurfaceLightHighContrast,
    inversePrimary = ThemeSecurityColors.inversePrimaryLightHighContrast,
    surfaceDim = ThemeSecurityColors.surfaceDimLightHighContrast,
    surfaceBright = ThemeSecurityColors.surfaceBrightLightHighContrast,
    surfaceContainerLowest = ThemeSecurityColors.surfaceContainerLowestLightHighContrast,
    surfaceContainerLow = ThemeSecurityColors.surfaceContainerLowLightHighContrast,
    surfaceContainer = ThemeSecurityColors.surfaceContainerLightHighContrast,
    surfaceContainerHigh = ThemeSecurityColors.surfaceContainerHighLightHighContrast,
    surfaceContainerHighest = ThemeSecurityColors.surfaceContainerHighestLightHighContrast,
)

private val securityDarkHighContrastScheme = darkColorScheme(
    primary = ThemeSecurityColors.primaryDarkHighContrast,
    onPrimary = ThemeSecurityColors.onPrimaryDarkHighContrast,
    primaryContainer = ThemeSecurityColors.primaryContainerDarkHighContrast,
    onPrimaryContainer = ThemeSecurityColors.onPrimaryContainerDarkHighContrast,
    secondary = ThemeSecurityColors.secondaryDarkHighContrast,
    onSecondary = ThemeSecurityColors.onSecondaryDarkHighContrast,
    secondaryContainer = ThemeSecurityColors.secondaryContainerDarkHighContrast,
    onSecondaryContainer = ThemeSecurityColors.onSecondaryContainerDarkHighContrast,
    tertiary = ThemeSecurityColors.tertiaryDarkHighContrast,
    onTertiary = ThemeSecurityColors.onTertiaryDarkHighContrast,
    tertiaryContainer = ThemeSecurityColors.tertiaryContainerDarkHighContrast,
    onTertiaryContainer = ThemeSecurityColors.onTertiaryContainerDarkHighContrast,
    error = ThemeSecurityColors.errorDarkHighContrast,
    onError = ThemeSecurityColors.onErrorDarkHighContrast,
    errorContainer = ThemeSecurityColors.errorContainerDarkHighContrast,
    onErrorContainer = ThemeSecurityColors.onErrorContainerDarkHighContrast,
    background = ThemeSecurityColors.backgroundDarkHighContrast,
    onBackground = ThemeSecurityColors.onBackgroundDarkHighContrast,
    surface = ThemeSecurityColors.surfaceDarkHighContrast,
    onSurface = ThemeSecurityColors.onSurfaceDarkHighContrast,
    surfaceVariant = ThemeSecurityColors.surfaceVariantDarkHighContrast,
    onSurfaceVariant = ThemeSecurityColors.onSurfaceVariantDarkHighContrast,
    outline = ThemeSecurityColors.outlineDarkHighContrast,
    outlineVariant = ThemeSecurityColors.outlineVariantDarkHighContrast,
    scrim = ThemeSecurityColors.scrimDarkHighContrast,
    inverseSurface = ThemeSecurityColors.inverseSurfaceDarkHighContrast,
    inverseOnSurface = ThemeSecurityColors.inverseOnSurfaceDarkHighContrast,
    inversePrimary = ThemeSecurityColors.inversePrimaryDarkHighContrast,
    surfaceDim = ThemeSecurityColors.surfaceDimDarkHighContrast,
    surfaceBright = ThemeSecurityColors.surfaceBrightDarkHighContrast,
    surfaceContainerLowest = ThemeSecurityColors.surfaceContainerLowestDarkHighContrast,
    surfaceContainerLow = ThemeSecurityColors.surfaceContainerLowDarkHighContrast,
    surfaceContainer = ThemeSecurityColors.surfaceContainerDarkHighContrast,
    surfaceContainerHigh = ThemeSecurityColors.surfaceContainerHighDarkHighContrast,
    surfaceContainerHighest = ThemeSecurityColors.surfaceContainerHighestDarkHighContrast,
)

/**
 * Composable que define el tema de la aplicación.
 * 
 * @param themeMode Cómo se debe comportar
 * @param contrastMode Contraste
 * @param colorPalette Paleta de colores
 * @param content Contenido
 */
@Composable
fun AppTheme(
    themeMode: ThemeMode = ThemeMode.System, // Cómo se debe comportar
    contrastMode: ContrastMode = ContrastMode.Standard, // Contraste
    colorPalette: ColorPalette = ColorPalette.Default, // Nuevo parámetro
    content: @Composable () -> Unit
) {
    // 1. Determinar el estado Dark/Light REAL
    val actualDarkTheme = when (themeMode) {
        ThemeMode.System -> isSystemInDarkTheme()
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
    }

    // 2. Seleccionar el esquema de colores basado en el modo, contraste y paleta
    val colorScheme = when {
        // Prioridad: Alto Contraste
        contrastMode == ContrastMode.HighContrast -> {
            when (colorPalette) {
                ColorPalette.Default -> if (actualDarkTheme) defaultDarkHighContrastScheme else defaultLightHighContrastScheme
                ColorPalette.Industrial -> if (actualDarkTheme) industrialDarkHighContrastScheme else industrialLightHighContrastScheme
                ColorPalette.Security -> if (actualDarkTheme) securityDarkHighContrastScheme else securityLightHighContrastScheme
            }
        }
        // Luego: Paleta de Colores
        colorPalette == ColorPalette.Industrial -> {
            if (actualDarkTheme) industrialDarkScheme else industrialLightScheme
        }
        colorPalette == ColorPalette.Security -> {
            if (actualDarkTheme) securityDarkScheme else securityLightScheme
        }
        // Por defecto: Paleta Default
        else -> {
            if (actualDarkTheme) defaultDarkScheme else defaultLightScheme
        }
    }

    // 3. Seleccionar los colores personalizados
    val customColors = when {
        // Prioridad: Alto Contraste
        contrastMode == ContrastMode.HighContrast -> {
            when (colorPalette) {
                ColorPalette.Default -> if (actualDarkTheme) customColorsDarkHighContrast else customColorsLightHighContrast
                ColorPalette.Industrial -> if (actualDarkTheme) industrialColorsDarkHighContrast else industrialColorsLightHighContrast
                ColorPalette.Security -> if (actualDarkTheme) securityColorsDarkHighContrast else securityColorsLightHighContrast
            }
        }
        // Luego: Paleta de Colores
        colorPalette == ColorPalette.Industrial -> {
            if (actualDarkTheme) industrialColorsDark else industrialColorsLight
        }
        colorPalette == ColorPalette.Security -> {
            if (actualDarkTheme) securityColorsDark else securityColorsLight
        }
        // Por defecto: Paleta Default
        else -> {
            if (actualDarkTheme) customColorsDark else customColorsLight
        }
    }

    // 4. Proveer los colores personalizados y el tema estándar
    CompositionLocalProvider(
        LocalAppCustomColors provides customColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = getAppTypography(),
            content = content
        )
    }
}
