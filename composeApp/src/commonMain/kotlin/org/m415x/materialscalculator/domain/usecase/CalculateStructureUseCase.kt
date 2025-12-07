package org.m415x.materialscalculator.domain.usecase

import org.m415x.materialscalculator.domain.common.WasteRegistry
import org.m415x.materialscalculator.domain.common.calculateWetMaterials
import org.m415x.materialscalculator.domain.repository.MaterialRepository
import org.m415x.materialscalculator.domain.model.*
import kotlin.math.PI
import kotlin.math.ceil
import kotlin.math.pow

/**
 * Calcula los materiales para una estructura.
 *
 * @param repository Repositorio de materiales.
 */
class CalculateStructureUseCase(private val repository: MaterialRepository) {

    /**
     * Calcula los materiales para una estructura.
     *
     * @param largoMetros Largo de la estructura en metros.
     * @param ladoAMetros Lado A de la estructura en metros.
     * @param ladoBMetros Lado B de la estructura en metros.
     * @param isCircular Indica si la estructura es circular.
     * @param tipoHormigon Tipo de hormigón.
     * @param diametroPrincipal Diámetro principal de hierro.
     * @param cantidadVarillas Cantidad de varillas.
     * @param diametroEstribo Diámetro de estribo de hierro.
     * @param separacionEstriboMetros Separación de estribo en metros.
     * @param longitudComercialHierroMetros Longitud comercial de hierro en metros.
     * @param pesoBolsaCementoKg Peso de la bolsa de cemento en kg.
     * @return Resultado del cálculo.
     */
    operator fun invoke(
        // Dimensiones generales
        largoMetros: Double,
        ladoAMetros: Double,
        ladoBMetros: Double,
        isCircular: Boolean = false,

        // Configuración Hormigón
        tipoHormigon: TipoHormigon,

        // Configuración Armadura Principal (Los hierros largos)
        diametroPrincipal: DiametroHierro,
        cantidadVarillas: Int,  // Ej. 4 hierros

        // Configuración Estribos (Los anillos)
        diametroEstribo: DiametroHierro,
        separacionEstriboMetros: Double,

        // Configuraciones opcionales (con defaults)
        longitudComercialHierroMetros: Int = 12,
        pesoBolsaCementoKg: Int = 25
    ): ResultadoEstructura {

        // ============================================================
        // 1. CÁLCULO DE HORMIGÓN (Usando el Motor Unificado)
        // ============================================================

        // A. Volumen Geométrico
        val volumenGeometrico = if (isCircular) {
            val radio = ladoAMetros / 2
            PI * radio.pow(2) * largoMetros
        } else {
            ladoAMetros * ladoBMetros * largoMetros
        }

        // B. Datos y Desperdicios
        val receta = repository.getDosificacionHormigon(tipoHormigon)
            ?: throw IllegalArgumentException("Hormigón no encontrado")

        val desperdicioHormigon = WasteRegistry.getForConcrete(tipoHormigon)

        // C. Cálculo automático de materiales húmedos
        val matsConcreto = calculateWetMaterials(
            volumenM3 = volumenGeometrico,
            receta = receta,
            desperdicio = desperdicioHormigon,
            pesoBolsaCemento = pesoBolsaCementoKg
        )

        // ============================================================
        // 2. CÁLCULO DE ARMADURA (HIERROS)
        // ============================================================

        // Obtenemos desperdicios específicos del registro
        val desperdicioPrincipal = WasteRegistry.getForIron(isEstribo = false)
        val desperdicioEstribos = WasteRegistry.getForIron(isEstribo = true)

        // --- A. Hierro Principal ---
        val pesoMetroPrincipal = repository.getPesoHierroPorMetro(diametroPrincipal)

        val longitudTotalPrincipal = (cantidadVarillas * largoMetros) * (1 + desperdicioPrincipal)
        val pesoTotalPrincipal = longitudTotalPrincipal * pesoMetroPrincipal

        val barrasPrincipalComprar = ceil(longitudTotalPrincipal / longitudComercialHierroMetros).toInt()

        // --- B. Estribos ---
        val pesoMetroEstribo = repository.getPesoHierroPorMetro(diametroEstribo)
        val cantidadEstribos = ceil(largoMetros / separacionEstriboMetros).toInt()

        // Geometría del estribo (Longitud de una vuelta)
        val longitudUnEstribo = if (isCircular) {
            val diametroReal = (ladoAMetros - 0.05).coerceAtLeast(0.0) // Restamos recubrimiento
            (PI * diametroReal) + 0.15 // +15cm ganchos
        } else {
            val aReal = (ladoAMetros - 0.05).coerceAtLeast(0.0)
            val bReal = (ladoBMetros - 0.05).coerceAtLeast(0.0)
            (2 * aReal + 2 * bReal) + 0.15 // +15cm ganchos
        }

        val longitudTotalEstribos = (cantidadEstribos * longitudUnEstribo) * (1 + desperdicioEstribos)
        val pesoTotalEstribos = longitudTotalEstribos * pesoMetroEstribo

        val barrasEstribosComprar = ceil(longitudTotalEstribos / longitudComercialHierroMetros).toInt()

        // ============================================================
        // 3. RESULTADO FINAL
        // ============================================================
        return ResultadoEstructura(
            // Hormigón (Viene del motor)
            volumenHormigonM3 = volumenGeometrico * (1 + desperdicioHormigon),
            cementoKg = matsConcreto.cementoKg,
            arenaM3 = matsConcreto.arenaM3,
            piedraM3 = matsConcreto.piedraM3,
            aguaLitros = matsConcreto.aguaLitros,
            bolsaCementoKg = pesoBolsaCementoKg,
            dosificacionHormigon = receta.dosificacionMezcla,
            porcentajeDesperdicioHormigon = desperdicioHormigon,

            // Armadura (Calculada aquí)
            diametroPrincipal = diametroPrincipal,
            diametroEstribo = diametroEstribo,
            hierroPrincipalKg = pesoTotalPrincipal,
            hierroEstribosKg = pesoTotalEstribos,
            hierroPrincipalMetros = longitudTotalPrincipal,
            hierroEstribosMetros = longitudTotalEstribos,
            cantidadHierroPrincipal = barrasPrincipalComprar,
            cantidadHierroEstribos = barrasEstribosComprar,
            longitudComercialHierroMetros = longitudComercialHierroMetros,
            porcentajeDesperdicioHierroPrincipal = desperdicioPrincipal,
            porcentajeDesperdicioHierroEstribos = desperdicioEstribos
        )
    }
}