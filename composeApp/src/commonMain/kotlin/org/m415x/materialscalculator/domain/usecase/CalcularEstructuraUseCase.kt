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
        ladoACm: Double,    // Ancho
        ladoBCm: Double,    // Alto/Profundidad (Si es circular, ignora esto)
        esCircular: Boolean = false, // Para columnas redondas

        // Configuración Hormigón
        tipoHormigon: TipoHormigon,

        // Configuración Armadura Principal (Los hierros largos)
        diametroPrincipal: DiametroHierro,
        cantidadVarillas: Int,  // Ej. 4 hierros

        // Configuración Estribos (Los anillos)
        diametroEstribo: DiametroHierro,
        separacionEstriboCm: Double = 0.20 // Cada 20 cm estándar
    ): ResultadoEstructura {

        // NORMALIZACIÓN: Convertimos todo a METROS ahora mismo.
        // Así el resto de la lógica no se preocupa por las unidades.
        val aMetros = ladoACm / 100.0
        val bMetros = ladoBCm / 100.0
        val sepMetros = separacionEstriboCm / 100.0

        // --- 1. Calcular Volumen de Hormigón ---
        val volumenM3 = if (esCircular) {
            // Pi * radio^2 * largo (ladoAMetros actúa como Diámetro)
            val radio = aMetros / 2
            PI * radio.pow(2) * largoMetros
        } else {
            // Rectangular: A * B * Largo
            aMetros * bMetros * largoMetros
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

        // Barras comerciales de 12 metros para hierro principal (cálculo aproximado para compra)
        val hierroPrincipalAComprar = ceil(longitudTotalPrincipal / 12.0).toInt()

        // --- 3. Calcular Estribos ---
        val pesoMetroEstribo = repository.getPesoHierroPorMetro(diametroEstribo)

        // Cantidad de estribos = Largo / Separación
        val cantidadEstribos = ceil(largoMetros / sepMetros).toInt()

        // Longitud de 1 estribo (Perímetro - recubrimiento + ganchos)
        // NOTA: Si pones 1cm x 1cm, (0.01 - 0.05) dará negativo.
        // coerceAtLeast(0.0) evitará números negativos, pero técnicamente
        // una columna de 1x1cm no puede tener estribos (solo ganchos).
        val longitudEstribo = if (esCircular) {
            // Perímetro círculo (restando recubrimiento de 2.5cm por lado = 0.05m)
            val diametroReal = (aMetros - 0.05).coerceAtLeast(0.0)
            (PI * diametroReal) + 0.15 // +15cm para los ganchos de cierre
        } else {
            // Perímetro rectángulo (restando recubrimiento)
            val aReal = (aMetros - 0.05).coerceAtLeast(0.0)
            val bReal = (bMetros - 0.05).coerceAtLeast(0.0)
            (2 * aReal + 2 * bReal) + 0.15 // +15cm para ganchos
        }

        // Longitud TOTAL de todos los estribos
        // Le agregamos un 5% de desperdicio porque al cortar estribos siempre sobran pedacitos
        val longitudTotalEstribos = (cantidadEstribos * longitudEstribo) * 1.05

        val pesoTotalEstribos = longitudTotalEstribos * pesoMetroEstribo

        // Barras comerciales de 12 metros para estribos (cálculo aproximado para compra)
        val hierroEstribosAComprar = ceil(longitudTotalEstribos / 12.0).toInt()


        return ResultadoEstructura(
            volumenHormigonM3 = volumenM3,
            cementoBolsas = cementoBolsas,
            arenaM3 = arenaTotal,
            piedraM3 = piedraTotal,
            hierroPrincipalKg = pesoTotalPrincipal,
            hierroEstribosKg = pesoTotalEstribos,
            cantidadHierroPrincipal = hierroPrincipalAComprar,
            cantidadHierroEstribos = hierroEstribosAComprar
        )
    }
}