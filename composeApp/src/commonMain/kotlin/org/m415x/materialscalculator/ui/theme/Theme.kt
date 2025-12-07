package org.m415x.materialscalculator.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

/**
 * Color scheme para el tema claro.
 * 
 * @property primary Color principal.
 * @property onPrimary Contraste con el color principal.
 * @property primaryContainer Contenedor del color principal.
 * @property onPrimaryContainer Contraste con el contenedor del color principal.
 * @property secondary Color secundario.
 * @property onSecondary Contraste con el color secundario.
 * @property secondaryContainer Contenedor del color secundario.
 * @property onSecondaryContainer Contraste con el contenedor del color secundario.
 * @property tertiary Color terciario.
 * @property onTertiary Contraste con el color terciario.
 * @property tertiaryContainer Contenedor del color terciario.
 * @property onTertiaryContainer Contraste con el contenedor del color terciario.
 * @property error Color de error.
 * @property onError Contraste con el color de error.
 * @property errorContainer Contenedor del color de error.
 * @property onErrorContainer Contraste con el contenedor del color de error.
 * @property background Color de fondo.
 * @property onBackground Contraste con el color de fondo.
 * @property surface Color de superficie.
 * @property onSurface Contraste con el color de superficie.
 * @property surfaceVariant Color de superficie variante.
 * @property onSurfaceVariant Contraste con el color de superficie variante.
 * @property outline Color de contorno.
 * @property outlineVariant Contenedor del color de contorno.
 * @property scrim Color de escudo.
 * @property inverseSurface Color de superficie invertida.
 * @property inverseOnSurface Contraste con el color de superficie invertida.
 * @property inversePrimary Color primario invertido.
 * @property surfaceDim Color de superficie disminuido.
 * @property surfaceBright Color de superficie brillante.
 * @property surfaceContainerLowest Contenedor de superficie más bajo.
 * @property surfaceContainerLow Contenedor de superficie bajo.
 * @property surfaceContainer Contenedor de superficie.
 * @property surfaceContainerHigh Contenedor de superficie alto.
 * @property surfaceContainerHighest Contenedor de superficie más alto.
 */
private val lightScheme = lightColorScheme(
    primary = primaryLight,
    onPrimary = onPrimaryLight,
    primaryContainer = primaryContainerLight,
    onPrimaryContainer = onPrimaryContainerLight,
    secondary = secondaryLight,
    onSecondary = onSecondaryLight,
    secondaryContainer = secondaryContainerLight,
    onSecondaryContainer = onSecondaryContainerLight,
    tertiary = tertiaryLight,
    onTertiary = onTertiaryLight,
    tertiaryContainer = tertiaryContainerLight,
    onTertiaryContainer = onTertiaryContainerLight,
    error = errorLight,
    onError = onErrorLight,
    errorContainer = errorContainerLight,
    onErrorContainer = onErrorContainerLight,
    background = backgroundLight,
    onBackground = onBackgroundLight,
    surface = surfaceLight,
    onSurface = onSurfaceLight,
    surfaceVariant = surfaceVariantLight,
    onSurfaceVariant = onSurfaceVariantLight,
    outline = outlineLight,
    outlineVariant = outlineVariantLight,
    scrim = scrimLight,
    inverseSurface = inverseSurfaceLight,
    inverseOnSurface = inverseOnSurfaceLight,
    inversePrimary = inversePrimaryLight,
    surfaceDim = surfaceDimLight,
    surfaceBright = surfaceBrightLight,
    surfaceContainerLowest = surfaceContainerLowestLight,
    surfaceContainerLow = surfaceContainerLowLight,
    surfaceContainer = surfaceContainerLight,
    surfaceContainerHigh = surfaceContainerHighLight,
    surfaceContainerHighest = surfaceContainerHighestLight,
)

/**
 * Color scheme para el tema oscuro.
 * 
 * @property primary Color principal.
 * @property onPrimary Contraste con el color principal.
 * @property primaryContainer Contenedor del color principal.
 * @property onPrimaryContainer Contraste con el contenedor del color principal.
 * @property secondary Color secundario.
 * @property onSecondary Contraste con el color secundario.
 * @property secondaryContainer Contenedor del color secundario.
 * @property onSecondaryContainer Contraste con el contenedor del color secundario.
 * @property tertiary Color terciario.
 * @property onTertiary Contraste con el color terciario.
 * @property tertiaryContainer Contenedor del color terciario.
 * @property onTertiaryContainer Contraste con el contenedor del color terciario.
 * @property error Color de error.
 * @property onError Contraste con el color de error.
 * @property errorContainer Contenedor del color de error.
 * @property onErrorContainer Contraste con el contenedor del color de error.
 * @property background Color de fondo.
 * @property onBackground Contraste con el color de fondo.
 * @property surface Color de superficie.
 * @property onSurface Contraste con el color de superficie.
 * @property surfaceVariant Color de superficie variante.
 * @property onSurfaceVariant Contraste con el color de superficie variante.
 * @property outline Color de contorno.
 * @property outlineVariant Contenedor del color de contorno.
 * @property scrim Color de escudo.
 * @property inverseSurface Color de superficie invertida.
 * @property inverseOnSurface Contraste con el color de superficie invertida.
 * @property inversePrimary Color primario invertido.
 * @property surfaceDim Color de superficie disminuido.
 * @property surfaceBright Color de superficie brillante.
 * @property surfaceContainerLowest Contenedor de superficie más bajo.
 * @property surfaceContainerLow Contenedor de superficie bajo.
 * @property surfaceContainer Contenedor de superficie.
 * @property surfaceContainerHigh Contenedor de superficie alto.
 * @property surfaceContainerHighest Contenedor de superficie más alto.
 */
private val darkScheme = darkColorScheme(
    primary = primaryDark,
    onPrimary = onPrimaryDark,
    primaryContainer = primaryContainerDark,
    onPrimaryContainer = onPrimaryContainerDark,
    secondary = secondaryDark,
    onSecondary = onSecondaryDark,
    secondaryContainer = secondaryContainerDark,
    onSecondaryContainer = onSecondaryContainerDark,
    tertiary = tertiaryDark,
    onTertiary = onTertiaryDark,
    tertiaryContainer = tertiaryContainerDark,
    onTertiaryContainer = onTertiaryContainerDark,
    error = errorDark,
    onError = onErrorDark,
    errorContainer = errorContainerDark,
    onErrorContainer = onErrorContainerDark,
    background = backgroundDark,
    onBackground = onBackgroundDark,
    surface = surfaceDark,
    onSurface = onSurfaceDark,
    surfaceVariant = surfaceVariantDark,
    onSurfaceVariant = onSurfaceVariantDark,
    outline = outlineDark,
    outlineVariant = outlineVariantDark,
    scrim = scrimDark,
    inverseSurface = inverseSurfaceDark,
    inverseOnSurface = inverseOnSurfaceDark,
    inversePrimary = inversePrimaryDark,
    surfaceDim = surfaceDimDark,
    surfaceBright = surfaceBrightDark,
    surfaceContainerLowest = surfaceContainerLowestDark,
    surfaceContainerLow = surfaceContainerLowDark,
    surfaceContainer = surfaceContainerDark,
    surfaceContainerHigh = surfaceContainerHighDark,
    surfaceContainerHighest = surfaceContainerHighestDark,
)

/**
 * Color scheme para el tema claro con contraste medio.
 * 
 * @property primary Color principal.
 * @property onPrimary Contraste con el color principal.
 * @property primaryContainer Contenedor del color principal.
 * @property onPrimaryContainer Contraste con el contenedor del color principal.
 * @property secondary Color secundario.
 * @property onSecondary Contraste con el color secundario.
 * @property secondaryContainer Contenedor del color secundario.
 * @property onSecondaryContainer Contraste con el contenedor del color secundario.
 * @property tertiary Color terciario.
 * @property onTertiary Contraste con el color terciario.
 * @property tertiaryContainer Contenedor del color terciario.
 * @property onTertiaryContainer Contraste con el contenedor del color terciario.
 * @property error Color de error.
 * @property onError Contraste con el color de error.
 * @property errorContainer Contenedor del color de error.
 * @property onErrorContainer Contraste con el contenedor del color de error.
 * @property background Color de fondo.
 * @property onBackground Contraste con el color de fondo.
 * @property surface Color de superficie.
 * @property onSurface Contraste con el color de superficie.
 * @property surfaceVariant Color de superficie variante.
 * @property onSurfaceVariant Contraste con el color de superficie variante.
 * @property outline Color de contorno.
 * @property outlineVariant Contenedor del color de contorno.
 * @property scrim Color de escudo.
 * @property inverseSurface Color de superficie invertida.
 * @property inverseOnSurface Contraste con el color de superficie invertida.
 * @property inversePrimary Color primario invertido.
 * @property surfaceDim Color de superficie disminuido.
 * @property surfaceBright Color de superficie brillante.
 * @property surfaceContainerLowest Contenedor de superficie más bajo.
 * @property surfaceContainerLow Contenedor de superficie bajo.
 * @property surfaceContainer Contenedor de superficie.
 * @property surfaceContainerHigh Contenedor de superficie alto.
 * @property surfaceContainerHighest Contenedor de superficie más alto.
 */
private val mediumContrastLightColorScheme = lightColorScheme(
    primary = primaryLightMediumContrast,
    onPrimary = onPrimaryLightMediumContrast,
    primaryContainer = primaryContainerLightMediumContrast,
    onPrimaryContainer = onPrimaryContainerLightMediumContrast,
    secondary = secondaryLightMediumContrast,
    onSecondary = onSecondaryLightMediumContrast,
    secondaryContainer = secondaryContainerLightMediumContrast,
    onSecondaryContainer = onSecondaryContainerLightMediumContrast,
    tertiary = tertiaryLightMediumContrast,
    onTertiary = onTertiaryLightMediumContrast,
    tertiaryContainer = tertiaryContainerLightMediumContrast,
    onTertiaryContainer = onTertiaryContainerLightMediumContrast,
    error = errorLightMediumContrast,
    onError = onErrorLightMediumContrast,
    errorContainer = errorContainerLightMediumContrast,
    onErrorContainer = onErrorContainerLightMediumContrast,
    background = backgroundLightMediumContrast,
    onBackground = onBackgroundLightMediumContrast,
    surface = surfaceLightMediumContrast,
    onSurface = onSurfaceLightMediumContrast,
    surfaceVariant = surfaceVariantLightMediumContrast,
    onSurfaceVariant = onSurfaceVariantLightMediumContrast,
    outline = outlineLightMediumContrast,
    outlineVariant = outlineVariantLightMediumContrast,
    scrim = scrimLightMediumContrast,
    inverseSurface = inverseSurfaceLightMediumContrast,
    inverseOnSurface = inverseOnSurfaceLightMediumContrast,
    inversePrimary = inversePrimaryLightMediumContrast,
    surfaceDim = surfaceDimLightMediumContrast,
    surfaceBright = surfaceBrightLightMediumContrast,
    surfaceContainerLowest = surfaceContainerLowestLightMediumContrast,
    surfaceContainerLow = surfaceContainerLowLightMediumContrast,
    surfaceContainer = surfaceContainerLightMediumContrast,
    surfaceContainerHigh = surfaceContainerHighLightMediumContrast,
    surfaceContainerHighest = surfaceContainerHighestLightMediumContrast,
)

/**
 * Color scheme para el tema claro con contraste alto.
 * 
 * @property primary Color principal.
 * @property onPrimary Contraste con el color principal.
 * @property primaryContainer Contenedor del color principal.
 * @property onPrimaryContainer Contraste con el contenedor del color principal.
 * @property secondary Color secundario.
 * @property onSecondary Contraste con el color secundario.
 * @property secondaryContainer Contenedor del color secundario.
 * @property onSecondaryContainer Contraste con el contenedor del color secundario.
 * @property tertiary Color terciario.
 * @property onTertiary Contraste con el color terciario.
 * @property tertiaryContainer Contenedor del color terciario.
 * @property onTertiaryContainer Contraste con el contenedor del color terciario.
 * @property error Color de error.
 * @property onError Contraste con el color de error.
 * @property errorContainer Contenedor del color de error.
 * @property onErrorContainer Contraste con el contenedor del color de error.
 * @property background Color de fondo.
 * @property onBackground Contraste con el color de fondo.
 * @property surface Color de superficie.
 * @property onSurface Contraste con el color de superficie.
 * @property surfaceVariant Color de superficie variante.
 * @property onSurfaceVariant Contraste con el color de superficie variante.
 * @property outline Color de contorno.
 * @property outlineVariant Contenedor del color de contorno.
 * @property scrim Color de escudo.
 * @property inverseSurface Color de superficie invertida.
 * @property inverseOnSurface Contraste con el color de superficie invertida.
 * @property inversePrimary Color primario invertido.
 * @property surfaceDim Color de superficie disminuido.
 * @property surfaceBright Color de superficie brillante.
 * @property surfaceContainerLowest Contenedor de superficie más bajo.
 * @property surfaceContainerLow Contenedor de superficie bajo.
 * @property surfaceContainer Contenedor de superficie.
 * @property surfaceContainerHigh Contenedor de superficie alto.
 * @property surfaceContainerHighest Contenedor de superficie más alto.
 */
private val highContrastLightColorScheme = lightColorScheme(
    primary = primaryLightHighContrast,
    onPrimary = onPrimaryLightHighContrast,
    primaryContainer = primaryContainerLightHighContrast,
    onPrimaryContainer = onPrimaryContainerLightHighContrast,
    secondary = secondaryLightHighContrast,
    onSecondary = onSecondaryLightHighContrast,
    secondaryContainer = secondaryContainerLightHighContrast,
    onSecondaryContainer = onSecondaryContainerLightHighContrast,
    tertiary = tertiaryLightHighContrast,
    onTertiary = onTertiaryLightHighContrast,
    tertiaryContainer = tertiaryContainerLightHighContrast,
    onTertiaryContainer = onTertiaryContainerLightHighContrast,
    error = errorLightHighContrast,
    onError = onErrorLightHighContrast,
    errorContainer = errorContainerLightHighContrast,
    onErrorContainer = onErrorContainerLightHighContrast,
    background = backgroundLightHighContrast,
    onBackground = onBackgroundLightHighContrast,
    surface = surfaceLightHighContrast,
    onSurface = onSurfaceLightHighContrast,
    surfaceVariant = surfaceVariantLightHighContrast,
    onSurfaceVariant = onSurfaceVariantLightHighContrast,
    outline = outlineLightHighContrast,
    outlineVariant = outlineVariantLightHighContrast,
    scrim = scrimLightHighContrast,
    inverseSurface = inverseSurfaceLightHighContrast,
    inverseOnSurface = inverseOnSurfaceLightHighContrast,
    inversePrimary = inversePrimaryLightHighContrast,
    surfaceDim = surfaceDimLightHighContrast,
    surfaceBright = surfaceBrightLightHighContrast,
    surfaceContainerLowest = surfaceContainerLowestLightHighContrast,
    surfaceContainerLow = surfaceContainerLowLightHighContrast,
    surfaceContainer = surfaceContainerLightHighContrast,
    surfaceContainerHigh = surfaceContainerHighLightHighContrast,
    surfaceContainerHighest = surfaceContainerHighestLightHighContrast,
)

/**
 * Color scheme para el tema oscuro con contraste medio.
 * 
 * @property primary Color principal.
 * @property onPrimary Contraste con el color principal.
 * @property primaryContainer Contenedor del color principal.
 * @property onPrimaryContainer Contraste con el contenedor del color principal.
 * @property secondary Color secundario.
 * @property onSecondary Contraste con el color secundario.
 * @property secondaryContainer Contenedor del color secundario.
 * @property onSecondaryContainer Contraste con el contenedor del color secundario.
 * @property tertiary Color terciario.
 * @property onTertiary Contraste con el color terciario.
 * @property tertiaryContainer Contenedor del color terciario.
 * @property onTertiaryContainer Contraste con el contenedor del color terciario.
 * @property error Color de error.
 * @property onError Contraste con el color de error.
 * @property errorContainer Contenedor del color de error.
 * @property onErrorContainer Contraste con el contenedor del color de error.
 * @property background Color de fondo.
 * @property onBackground Contraste con el color de fondo.
 * @property surface Color de superficie.
 * @property onSurface Contraste con el color de superficie.
 * @property surfaceVariant Color de superficie variante.
 * @property onSurfaceVariant Contraste con el color de superficie variante.
 * @property outline Color de contorno.
 * @property outlineVariant Contenedor del color de contorno.
 * @property scrim Color de escudo.
 * @property inverseSurface Color de superficie invertida.
 * @property inverseOnSurface Contraste con el color de superficie invertida.
 * @property inversePrimary Color primario invertido.
 * @property surfaceDim Color de superficie disminuido.
 * @property surfaceBright Color de superficie brillante.
 * @property surfaceContainerLowest Contenedor de superficie más bajo.
 * @property surfaceContainerLow Contenedor de superficie bajo.
 * @property surfaceContainer Contenedor de superficie.
 * @property surfaceContainerHigh Contenedor de superficie alto.
 * @property surfaceContainerHighest Contenedor de superficie más alto.
 */
private val mediumContrastDarkColorScheme = darkColorScheme(
    primary = primaryDarkMediumContrast,
    onPrimary = onPrimaryDarkMediumContrast,
    primaryContainer = primaryContainerDarkMediumContrast,
    onPrimaryContainer = onPrimaryContainerDarkMediumContrast,
    secondary = secondaryDarkMediumContrast,
    onSecondary = onSecondaryDarkMediumContrast,
    secondaryContainer = secondaryContainerDarkMediumContrast,
    onSecondaryContainer = onSecondaryContainerDarkMediumContrast,
    tertiary = tertiaryDarkMediumContrast,
    onTertiary = onTertiaryDarkMediumContrast,
    tertiaryContainer = tertiaryContainerDarkMediumContrast,
    onTertiaryContainer = onTertiaryContainerDarkMediumContrast,
    error = errorDarkMediumContrast,
    onError = onErrorDarkMediumContrast,
    errorContainer = errorContainerDarkMediumContrast,
    onErrorContainer = onErrorContainerDarkMediumContrast,
    background = backgroundDarkMediumContrast,
    onBackground = onBackgroundDarkMediumContrast,
    surface = surfaceDarkMediumContrast,
    onSurface = onSurfaceDarkMediumContrast,
    surfaceVariant = surfaceVariantDarkMediumContrast,
    onSurfaceVariant = onSurfaceVariantDarkMediumContrast,
    outline = outlineDarkMediumContrast,
    outlineVariant = outlineVariantDarkMediumContrast,
    scrim = scrimDarkMediumContrast,
    inverseSurface = inverseSurfaceDarkMediumContrast,
    inverseOnSurface = inverseOnSurfaceDarkMediumContrast,
    inversePrimary = inversePrimaryDarkMediumContrast,
    surfaceDim = surfaceDimDarkMediumContrast,
    surfaceBright = surfaceBrightDarkMediumContrast,
    surfaceContainerLowest = surfaceContainerLowestDarkMediumContrast,
    surfaceContainerLow = surfaceContainerLowDarkMediumContrast,
    surfaceContainer = surfaceContainerDarkMediumContrast,
    surfaceContainerHigh = surfaceContainerHighDarkMediumContrast,
    surfaceContainerHighest = surfaceContainerHighestDarkMediumContrast,
)

/**
 * Color scheme para el tema oscuro con contraste alto.
 * 
 * @property primary Color principal.
 * @property onPrimary Contraste con el color principal.
 * @property primaryContainer Contenedor del color principal.
 * @property onPrimaryContainer Contraste con el contenedor del color principal.
 * @property secondary Color secundario.
 * @property onSecondary Contraste con el color secundario.
 * @property secondaryContainer Contenedor del color secundario.
 * @property onSecondaryContainer Contraste con el contenedor del color secundario.
 * @property tertiary Color terciario.
 * @property onTertiary Contraste con el color terciario.
 * @property tertiaryContainer Contenedor del color terciario.
 * @property onTertiaryContainer Contraste con el contenedor del color terciario.
 * @property error Color de error.
 * @property onError Contraste con el color de error.
 * @property errorContainer Contenedor del color de error.
 * @property onErrorContainer Contraste con el contenedor del color de error.
 * @property background Color de fondo.
 * @property onBackground Contraste con el color de fondo.
 * @property surface Color de superficie.
 * @property onSurface Contraste con el color de superficie.
 * @property surfaceVariant Color de superficie variante.
 * @property onSurfaceVariant Contraste con el color de superficie variante.
 * @property outline Color de contorno.
 * @property outlineVariant Contenedor del color de contorno.
 * @property scrim Color de escudo.
 * @property inverseSurface Color de superficie invertida.
 * @property inverseOnSurface Contraste con el color de superficie invertida.
 * @property inversePrimary Color primario invertido.
 * @property surfaceDim Color de superficie disminuido.
 * @property surfaceBright Color de superficie brillante.
 * @property surfaceContainerLowest Contenedor de superficie más bajo.
 * @property surfaceContainerLow Contenedor de superficie bajo.
 * @property surfaceContainer Contenedor de superficie.
 * @property surfaceContainerHigh Contenedor de superficie alto.
 * @property surfaceContainerHighest Contenedor de superficie más alto.
 */
private val highContrastDarkColorScheme = darkColorScheme(
    primary = primaryDarkHighContrast,
    onPrimary = onPrimaryDarkHighContrast,
    primaryContainer = primaryContainerDarkHighContrast,
    onPrimaryContainer = onPrimaryContainerDarkHighContrast,
    secondary = secondaryDarkHighContrast,
    onSecondary = onSecondaryDarkHighContrast,
    secondaryContainer = secondaryContainerDarkHighContrast,
    onSecondaryContainer = onSecondaryContainerDarkHighContrast,
    tertiary = tertiaryDarkHighContrast,
    onTertiary = onTertiaryDarkHighContrast,
    tertiaryContainer = tertiaryContainerDarkHighContrast,
    onTertiaryContainer = onTertiaryContainerDarkHighContrast,
    error = errorDarkHighContrast,
    onError = onErrorDarkHighContrast,
    errorContainer = errorContainerDarkHighContrast,
    onErrorContainer = onErrorContainerDarkHighContrast,
    background = backgroundDarkHighContrast,
    onBackground = onBackgroundDarkHighContrast,
    surface = surfaceDarkHighContrast,
    onSurface = onSurfaceDarkHighContrast,
    surfaceVariant = surfaceVariantDarkHighContrast,
    onSurfaceVariant = onSurfaceVariantDarkHighContrast,
    outline = outlineDarkHighContrast,
    outlineVariant = outlineVariantDarkHighContrast,
    scrim = scrimDarkHighContrast,
    inverseSurface = inverseSurfaceDarkHighContrast,
    inverseOnSurface = inverseOnSurfaceDarkHighContrast,
    inversePrimary = inversePrimaryDarkHighContrast,
    surfaceDim = surfaceDimDarkHighContrast,
    surfaceBright = surfaceBrightDarkHighContrast,
    surfaceContainerLowest = surfaceContainerLowestDarkHighContrast,
    surfaceContainerLow = surfaceContainerLowDarkHighContrast,
    surfaceContainer = surfaceContainerDarkHighContrast,
    surfaceContainerHigh = surfaceContainerHighDarkHighContrast,
    surfaceContainerHighest = surfaceContainerHighestDarkHighContrast,
)

/**
 * Composable que define el tema de la aplicación.
 * 
 * @param themeMode Cómo se debe comportar
 * @param contrastMode Contraste
 * @param content Contenido
 */
@Composable
fun AppTheme(
    themeMode: ThemeMode = ThemeMode.System, // Cómo se debe comportar
    contrastMode: ContrastMode = ContrastMode.Standard, // Contraste
    content: @Composable () -> Unit
) {
    // 1. Determinar el estado Dark/Light REAL
    val actualDarkTheme = when (themeMode) {
        ThemeMode.System -> isSystemInDarkTheme()
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
    }

    // 2. Seleccionar el esquema de colores basado en el modo y el contraste
    val colorScheme = when {
        // Modo Oscuro y Alto Contraste (para máxima legibilidad nocturna)
        actualDarkTheme && contrastMode == ContrastMode.HighContrast -> highContrastDarkColorScheme
        // Modo Oscuro estándar
        actualDarkTheme -> darkScheme
        // Modo Claro y Alto Contraste (ideal para exteriores y glare)
        !actualDarkTheme && contrastMode == ContrastMode.HighContrast -> highContrastLightColorScheme
        // Modo Claro estándar
        else -> lightScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = getAppTypography(),
        content = content
    )
}

