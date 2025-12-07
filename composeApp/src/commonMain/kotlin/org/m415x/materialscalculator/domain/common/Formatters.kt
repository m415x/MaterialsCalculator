package org.m415x.materialscalculator.domain.common

import kotlin.math.ceil
import org.m415x.materialscalculator.domain.model.*
import org.m415x.materialscalculator.ui.common.roundToDecimals

const val APP_NAME: String = "Materials Calculator"

/**
 * Convierte una cantidad total (kg/litros) en unidades contenedoras (bolsas, bidones, etc.)
 * formateadas como texto pluralizado.
 *
 * @param capacidadContenedor Tamaño de la unidad (ej: 50 para bolsa de cemento).
 * @param nombreUnidad Nombre del contenedor (ej: "bolsa", "balde").
 * @param sufijoPlural Terminación para plural (default "s", puedes usar "es").
 */
fun Double.toPresentacion(
    capacidadContenedor: Number = 1,
    nombreUnidad: String = "bolsa",
    sufijoPlural: String = "s"
): String {
    // 1. Calculamos la cantidad redondeando hacia arriba
    val cantidad = ceil(this / capacidadContenedor.toDouble()).toInt()

    // 2. Determinamos si lleva plural
    val sufijo = if (cantidad != 1) sufijoPlural else ""

    // 3. Devolvemos el string limpio
    return "$cantidad $nombreUnidad$sufijo"
}

/**
 * Genera un texto para compartir el resultado de un cálculo de hormigón.
 *
 * @param ancho Ancho de la obra en metros.
 * @param largo Largo de la obra en metros.
 * @param espesor Espesor de la obra en metros.
 * @param tipoHormigon Tipo de hormigón utilizado.
 */
fun ResultadoHormigon.toShareText(
    ancho: Double,
    largo: Double,
    espesor: Double,
    tipoHormigon: TipoHormigon
): String {
    return """
        *CÁLCULO DE HORMIGÓN*
        =========================
        
        *DETALLE DE OBRA*
        -------------------------
        *Dimensiones:* $ancho x $largo m
        *Espesor:* ${espesor.metersToCm} cm
        *Volumen Total:* ${volumenTotalM3.roundToDecimals(2)} m³
        
        *Hormigón:* ${tipoHormigon.name} (${tipoHormigon.resistencia})
            └ ${tipoHormigon.usos}
        
        
        *MATERIALES ESTIMADOS*
        -------------------------
        • Cemento: ${cementoKg.roundToDecimals(1)} kg
            └ aprox. ${cementoKg.toPresentacion(bolsaCementoKg)} (${bolsaCementoKg} kg)
        • Arena: ${arenaM3.roundToDecimals(2)} m³
        • Piedra/Grava: ${piedraM3.roundToDecimals(2)} m³
        • Agua: ${aguaLitros.roundToDecimals(1)} Lt
        
        *Proporción estimada:* 
        $dosificacionMezcla
        
        _________________________
        _Generado con ${APP_NAME}_
    """.trimIndent()
}

/**
 * Genera un texto para compartir el resultado de un cálculo de muro.
 *
 * @param largo Largo de la obra en metros.
 * @param alto Alto de la obra en metros.
 * @param tipoLadrillo Tipo de ladrillo utilizado.
 * @param detalleLadrillo Detalle del ladrillo.
 * @param aberturas Lista de aberturas en la obra.
 */
fun ResultadoMuro.toShareText(
    largo: Double,
    alto: Double,
    tipoLadrillo: TipoLadrillo,
    detalleLadrillo: String,
    aberturas: List<Abertura>
): String {
    // 1. Cálculos auxiliares para el reporte
    val superficieBruta = largo * alto
    val superficieAberturas = aberturas.sumOf { it.anchoMetros * it.altoMetros * it.cantidad }

    // 2. Construcción del texto de Aberturas
    val detalleAberturas =
        if (aberturas.isEmpty()) {
            "   (Sin aberturas)"
        } else {
            aberturas.joinToString("\n") { ab ->
                "   • ${ab.cantidad} x ${ab.nombre}: ${ab.anchoMetros} x ${ab.altoMetros} m"
            }
        }

    return """
        *CÁLCULO DE MURO*
        =========================
        
        *DETALLE DE OBRA*
        -------------------------
        *Dimensiones:* $largo x $alto m
        *Ladrillo:* ${tipoLadrillo.nombre} 
            └ Dimensiones $detalleLadrillo
        
        *Superficies:*
            • Total Muro: ${superficieBruta.roundToDecimals(2)} m²
            • Aberturas:  ${superficieAberturas.roundToDecimals(2)} m²
            • Real a cubrir: ${areaNetaM2.roundToDecimals(2)} m²
        
        *Aberturas:*
        $detalleAberturas
        
        
        *MATERIALES ESTIMADOS*
        -------------------------
        *Ladrillos:* $cantidadLadrillos U
        
        *Mortero (${morteroM3.roundToDecimals(2)} m³):*
        
        • Cemento: ${cementoKg.roundToDecimals(1)} kg
            └ aprox. ${cementoKg.toPresentacion(bolsaCementoKg)} (${bolsaCementoKg} kg)
        ${if (calKg > 0) "• Cal: ${calKg.roundToDecimals(1)} kg" else ""}
        ${if (calKg > 0) "  └ aprox. ${calKg.toPresentacion(bolsaCalKg)} (${bolsaCalKg} kg)" else ""}
        • Arena: ${arenaTotalM3.roundToDecimals(2)} m³
        • Agua: ${aguaLitros.roundToDecimals(1)} Lt
        
        *Proporción estimada:* 
        $proporcionMezcla
        
        _________________________
        _Generado con ${APP_NAME}_
    """.trimIndent()
}

/**
 * Genera un texto para compartir el resultado de un cálculo de estructura.
 *
 * @param largo Largo de la obra en metros.
 * @param ladoA Lado A de la obra en metros.
 * @param ladoB Lado B de la obra en metros.
 * @param isCircular Indica si la estructura es circular.
 * @param tipoHormigon Tipo de hormigón utilizado.
 * @param separacionEstribosCm Separación entre estribos en centímetros.
 */
fun ResultadoEstructura.toShareText(
    largo: Double,
    ladoA: Double, // Input en Metros (Ancho o Diámetro)
    ladoB: Double, // Input en Metros (Alto, o 0 si es circular)
    isCircular: Boolean,
    tipoHormigon: TipoHormigon,
    separacionEstribosCm: Double // Para mostrar cada cuánto van
): String {
    // 1. Definimos la geometría para el texto
    val detalleGeometria =
        if (isCircular) {
            "Columna Circular: Ø $ladoA m"
        } else {
            "Rectangular: $ladoA x $ladoB m"
        }
    return """
        *CÁLCULO DE ARMADURA*
        =========================
        
        *DETALLE DE OBRA*
        -------------------------
        *Largo Total:* $largo m
        $detalleGeometria
        
        
        *1. HORMIGÓN (${volumenHormigonM3.roundToDecimals(2)} m³)*
        -------------------------
        Tipo: ${tipoHormigon.name} (${tipoHormigon.resistencia})
            
        • Cemento: ${cementoKg.roundToDecimals(1)} kg
            └ aprox. ${cementoKg.toPresentacion(bolsaCementoKg)} (${bolsaCementoKg} kg)
        • Arena: ${arenaM3.roundToDecimals(2)} m³
        • Piedra: ${piedraM3.roundToDecimals(2)} m³
        • Agua: ${aguaLitros.roundToDecimals(0)} Lt
        
        *Proporción estimada:* 
        $dosificacionHormigon
        
        
        *2. ARMADURA (HIERROS)*
        -------------------------
        *Principal (Longitudinal):*
            Varillas: Ø ${diametroPrincipal.mm} mm
            Total Peso: ${hierroPrincipalKg.roundToDecimals(1)} kg
                *Comprar:* $cantidadHierroPrincipal barras de 12 m
        
        *Estribos (Transversal):*
            Hierro: Ø ${diametroEstribo.mm} mm
            Separación: cada ${separacionEstribosCm.roundToDecimals(0)} cm
            Total Peso: ${hierroEstribosKg.roundToDecimals(1)} kg
                *Comprar:* $cantidadHierroEstribos barras de 12 m
        
        _________________________
        _Generado con ${APP_NAME}_
    """.trimIndent()
}

/**
 * Genera un texto para compartir el resultado de un cálculo de revoque.
 *
 * @param largo Largo de la obra en metros.
 * @param alto Alto de la obra en metros.
 * @param espesorGruesoMetros Espesor grueso de la obra en metros.
 * @param ambasCaras Indica si la revoque tiene ambas caras.
 */
fun ResultadoRevoque.toShareText(
    largo: Double,
    alto: Double,
    espesorGruesoMetros: Double,
    ambasCaras: Boolean
): String {

    val detalleCaras = if (ambasCaras) "(Ambas caras)" else "(Una sola cara)"

    return """
        *CÁLCULO DE REVOQUE*
        =========================
        
        *DETALLE DE OBRA*
        -------------------------
        *Pared:* $largo x $alto m
        *Superficie Total:* ${areaTotalM2.roundToDecimals(2)} m²
        $detalleCaras
        
        
        *1. REVOQUE GRUESO (Jaharro)*
        -------------------------
        *Espesor:* ${espesorGruesoMetros.metersToCm} cm
        *Volumen:* ${volumenGruesoM3.roundToDecimals(2)} m³
        
        • Cemento: ${gruesoCementoKg.roundToDecimals(1)} kg
            └ aprox. ${gruesoCementoKg.toPresentacion(bolsaCementoKg)} (${bolsaCementoKg} kg)
        • Cal Hidratada: ${gruesoCalKg.roundToDecimals(1)} kg
            └ aprox. ${gruesoCalKg.toPresentacion(bolsaCalKg)} (${bolsaCalKg} kg)
        • Arena Común: ${gruesoArenaM3.roundToDecimals(2)} m³
        
        *Proporción estimada:* 
        $dosificacionGrueso
        
        
        *2. REVOQUE FINO (Enlucido)*
        -------------------------
        *Opción A*
        • Premezcla: ${finoPremezclaKg.roundToDecimals(1)} kg
            └ aprox. ${finoPremezclaKg.toPresentacion(bolsaFinoPremezclaKg)} (${bolsaFinoPremezclaKg} kg)
        
        *Opción B (Tradicional: A la cal)*
        • Cal Aérea: ${finoCalKg.roundToDecimals(1)} kg
            └ aprox. ${finoCalKg.toPresentacion(bolsaCalKg)} (${bolsaCalKg} kg)
        • Arena Fina: ${finoArenaM3.roundToDecimals(2)} m³
        • Cemento: (Mínimo para ligar)
        
        Proporción estimada: 
        $dosificacionFino
        
        _________________________
        _Generado con ${APP_NAME}_
    """.trimIndent()
}