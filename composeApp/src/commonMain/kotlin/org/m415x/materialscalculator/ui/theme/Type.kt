package org.m415x.materialscalculator.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import org.jetbrains.compose.resources.Font // Importante: Usar este Font, no el de ui.text
import materialscalculator.composeapp.generated.resources.Res // Esta importación se genera sola (ver nota abajo)
import materialscalculator.composeapp.generated.resources.source_code_pro_light
import materialscalculator.composeapp.generated.resources.source_code_pro_regular
import materialscalculator.composeapp.generated.resources.source_code_pro_medium
import materialscalculator.composeapp.generated.resources.source_code_pro_bold
import materialscalculator.composeapp.generated.resources.inter_regular
import materialscalculator.composeapp.generated.resources.inter_bold

/**
 * Familia de fuentes para el cuerpo.
 * 
 * @return FontFamily
 */
val bodyFontFamily @Composable get() = FontFamily(
    Font(Res.font.source_code_pro_light, weight = FontWeight.Light),
    Font(Res.font.source_code_pro_regular, weight = FontWeight.Normal),
    Font(Res.font.source_code_pro_medium, weight = FontWeight.Medium),
    Font(Res.font.source_code_pro_bold, weight = FontWeight.Bold)
)

/**
 * Familia de fuentes para el display.
 * 
 * @return FontFamily
 */
val displayFontFamily @Composable get() = FontFamily(
    Font(Res.font.inter_regular, weight = FontWeight.Normal),
    Font(Res.font.inter_bold, weight = FontWeight.Bold),
)

/**
 * Aplicamos a la tipografía
 * 
 * @return Typography
 */
@Composable
fun getAppTypography(): Typography {
    val body = bodyFontFamily
    val display = displayFontFamily
    val baseline = Typography()

    return Typography(
        displayLarge = baseline.displayLarge.copy(fontFamily = display),
        displayMedium = baseline.displayMedium.copy(fontFamily = display),
        displaySmall = baseline.displaySmall.copy(fontFamily = display),
        headlineLarge = baseline.headlineLarge.copy(fontFamily = display),
        headlineMedium = baseline.headlineMedium.copy(fontFamily = display),
        headlineSmall = baseline.headlineSmall.copy(fontFamily = display),
        titleLarge = baseline.titleLarge.copy(fontFamily = display),
        titleMedium = baseline.titleMedium.copy(fontFamily = display),
        titleSmall = baseline.titleSmall.copy(fontFamily = display),
        bodyLarge = baseline.bodyLarge.copy(fontFamily = body),
        bodyMedium = baseline.bodyMedium.copy(fontFamily = body),
        bodySmall = baseline.bodySmall.copy(fontFamily = body),
        labelLarge = baseline.labelLarge.copy(fontFamily = body),
        labelMedium = baseline.labelMedium.copy(fontFamily = body),
        labelSmall = baseline.labelSmall.copy(fontFamily = body),
    )
}