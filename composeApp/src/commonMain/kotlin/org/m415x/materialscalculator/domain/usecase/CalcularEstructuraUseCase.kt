package org.m415x.materialscalculator.domain.usecase

import org.m415x.materialscalculator.domain.repository.MaterialRepository
import org.m415x.materialscalculator.domain.model.*
import kotlin.math.PI
import kotlin.math.ceil
import kotlin.math.pow

class CalcularEstructuraUseCase(private val repository: MaterialRepository) {

    operator fun invoke(
        // Dimensiones generales
        largoMetros: Double,    // Largo de la viga o alto de la columna
        ladoAMetros: Double,    // Ancho
        ladoBMetros: Double,    // Alto/Profundidad (Si es circular, ignora esto)
        esCircular: Boolean = false, // Para columnas redondas

        // Configuración Hormigón
        tipoHormigon: TipoHormigon,

        // Configuración Armadura Principal (Los hierros largos)
        diametroPrincipal: DiametroHierro,
        cantidadVarillas: Int,  // Ej. 4 hierros

        // Configuración Estribos (Los anillos)
        diametroEstribo: DiametroHierro,
        separacionEstriboMetros: Double = 0.20 // Cada 20 cm estándar
    ): ResultadoEstructura {

        // --- 1. Calcular Volumen de Hormigón ---
        val volumenM3 = if (esCircular) {
            // Pi * radio^2 * largo (ladoAMetros actúa como Diámetro)
            val radio = ladoAMetros / 2
            PI * radio.pow(2) * largoMetros
        } else {
            // Rectangular: A * B * Largo
            ladoAMetros * ladoBMetros * largoMetros
        }

        // Obtenemos receta del hormigón (reutilizando lógica del repo)
        val receta = repository.getDosificacionHormigon(tipoHormigon)
            ?: throw IllegalArgumentException("Hormigón no encontrado")

        // Calculamos materiales húmedos
        val cementoTotalKg = volumenM3 * receta.cementoKg
        val cementoBolsas = ceil(cementoTotalKg / 25.0).toInt() // Bolsa 25kg
        val arenaTotal = volumenM3 * receta.arenaM3
        val piedraTotal = volumenM3 * receta.piedraM3


        // --- 2. Calcular Hierro Principal ---
        val pesoMetroPrincipal: Double = repository.getPesoHierroPorMetro(diametroPrincipal)

        // Longitud total lineal de hierros = Cantidad * LargoViga
        // Agregamos 10% de desperdicio por empalmes y puntas
        val longitudTotalPrincipal = (cantidadVarillas * largoMetros) * 1.10
        val pesoTotalPrincipal = longitudTotalPrincipal * pesoMetroPrincipal

        // Barras comerciales de 12 metros (cálculo aproximado para compra)
        val barrasAComprar = ceil(longitudTotalPrincipal / 12.0).toInt()


        // --- 3. Calcular Estribos ---
        val pesoMetroEstribo = repository.getPesoHierroPorMetro(diametroEstribo)

        // Cantidad de estribos = Largo / Separación
        val cantidadEstribos = ceil(largoMetros / separacionEstriboMetros).toInt()

        // Longitud de 1 estribo
        val longitudEstribo = if (esCircular) {
            // Perímetro círculo (restando recubrimiento de 2.5cm por lado = 0.05m)
            val diametroReal = (ladoAMetros - 0.05).coerceAtLeast(0.0)
            (PI * diametroReal) + 0.15 // +15cm para los ganchos de cierre
        } else {
            // Perímetro rectángulo (restando recubrimiento)
            val aReal = (ladoAMetros - 0.05).coerceAtLeast(0.0)
            val bReal = (ladoBMetros - 0.05).coerceAtLeast(0.0)
            (2 * aReal + 2 * bReal) + 0.15 // +15cm para ganchos
        }

        val longitudTotalEstribos = cantidadEstribos * longitudEstribo
        val pesoTotalEstribos = longitudTotalEstribos * pesoMetroEstribo


        return ResultadoEstructura(
            volumenHormigonM3 = volumenM3,
            cementoBolsas = cementoBolsas,
            arenaM3 = arenaTotal,
            piedraM3 = piedraTotal,
            hierroPrincipalKg = pesoTotalPrincipal,
            hierroEstribosKg = pesoTotalEstribos,
            cantidadBarrasAcero = barrasAComprar
        )
    }
}