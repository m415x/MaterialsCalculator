package org.m415x.materialscalculator.data.repository

import org.m415x.materialscalculator.domain.repository.MaterialRepository
import org.m415x.materialscalculator.domain.model.*

class StaticMaterialRepository : MaterialRepository {

    /**
     * Base de datos interna de dosificaciones de hormigones.
     * Aquí es donde "guardamos" las tablas que buscamos.
     */
    private val hormigonDB = mapOf(

        // H8: Hormigón de limpieza (pobre). Poco cemento.
        // Usos: Contrapisos, carpetas, nivelación.
        TipoHormigon.H8 to DosificacionHormigon(
            cementoKg = 180.0,
            arenaM3 = 0.55,
            piedraM3 = 0.9,
            relacionAguaCemento = 0.6
        ),

        // H13: Hormigón simple (no estructural o estructuras muy livianas).
        TipoHormigon.H13 to DosificacionHormigon(
            cementoKg = 260.0, // Mínimo según CIRSOC
            arenaM3 = 0.63,    // Proporción 1:3:4 aprox.
            piedraM3 = 0.84,
            relacionAguaCemento = 0.5
        ),

        // H17: Hormigón estándar para viviendas pequeñas (bases, encadenados).
        TipoHormigon.H17 to DosificacionHormigon(
            cementoKg = 300.0, // Mínimo según CIRSOC
            arenaM3 = 0.67,    // Proporción 1:3:3
            piedraM3 = 0.67,
            relacionAguaCemento = 0.5
        ),

        // H21: Hormigón ESTRUCTURAL estándar (Losas, vigas, columnas).
        // Es el más utilizado en construcción tradicional.
        TipoHormigon.H21 to DosificacionHormigon(
            cementoKg = 350.0,
            arenaM3 = 0.55,
            piedraM3 = 0.75,
            relacionAguaCemento = 0.45
        ),

        // H25: Hormigón de alta resistencia.
        // Usos: Columnas muy cargadas, estructuras importantes.
        TipoHormigon.H25 to DosificacionHormigon(
            cementoKg = 380.0,
            arenaM3 = 0.5,
            piedraM3 = 0.75,
            relacionAguaCemento = 0.42
        ),

        // H30: Hormigón de muy alta resistencia.
        // NOTA: Difícil de lograr a mano sin aditivos fluidificantes por la poca agua.
        TipoHormigon.H30 to DosificacionHormigon(
            cementoKg = 430.0,
            arenaM3 = 0.45,
            piedraM3 = 0.70,
            relacionAguaCemento = 0.40
        )
    )

    /**
     * Mapeo de dimensiones y juntas sugeridas.
     * Se asume colocación estándar (no panderete/canto, salvo especificación).
     */
    private val ladrilloDB = mapOf(
        // Ladrillo macizo: Junta más gruesa (1.5 cm a 2.0 cm)
        TipoLadrillo.COMUN to PropiedadesLadrillo(
            anchoMuro = 0.12,
            altoUnidad = 0.05,
            largoUnidad = 0.25,
            espesorJunta = 0.015
        ),
        TipoLadrillo.LADRILLON to PropiedadesLadrillo(
            anchoMuro = 0.18,
            altoUnidad = 0.05,
            largoUnidad = 0.25,
            espesorJunta = 0.015
        ),

        // Ladrillos Huecos (Cerámicos): Junta media (1.0 cm a 1.5 cm)
        // Nota: El "Ancho" del enum es el espesor del muro.
        TipoLadrillo.CERAMICO_8 to PropiedadesLadrillo(
            anchoMuro = 0.08,
            altoUnidad = 0.18,
            largoUnidad = 0.33,
            espesorJunta = 0.012
        ),
        TipoLadrillo.CERAMICO_12 to PropiedadesLadrillo(
            anchoMuro = 0.12,
            altoUnidad = 0.18,
            largoUnidad = 0.33,
            espesorJunta = 0.012
        ),
        TipoLadrillo.CERAMICO_18 to PropiedadesLadrillo(
            anchoMuro = 0.18,
            altoUnidad = 0.18,
            largoUnidad = 0.33,
            espesorJunta = 0.012
        ),

        // Bloques de Hormigón: Junta fina (1.0 cm)
        TipoLadrillo.BLOQUE_10 to PropiedadesLadrillo(
            anchoMuro = 0.09,
            altoUnidad = 0.19,
            largoUnidad = 0.39,
            espesorJunta = 0.01
        ),
        TipoLadrillo.BLOQUE_15 to PropiedadesLadrillo(
            anchoMuro = 0.13,
            altoUnidad = 0.19,
            largoUnidad = 0.39,
            espesorJunta = 0.01
        ),
        TipoLadrillo.BLOQUE_20 to PropiedadesLadrillo(
            anchoMuro = 0.19,
            altoUnidad = 0.19,
            largoUnidad = 0.39,
            espesorJunta = 0.01
        )
    )

    // Peso en Kg por metro lineal según tabla estándar
    private val pesosHierro = mapOf(
        DiametroHierro.HIERRO_6 to 0.222,
        DiametroHierro.HIERRO_8 to 0.395,
        DiametroHierro.HIERRO_10 to 0.617,
        DiametroHierro.HIERRO_12 to 0.888,
        DiametroHierro.HIERRO_16 to 1.580
    )

    // Base de datos de mezclas (Valores estándar por m3 de mortero)
    private val mezclaCalReforzada = DosificacionMortero(
        cementoKg = 210.0,
        calKg = 150.0,
        arenaM3 = 1.0,
        relacionAguaCemento = 0.6
    ) // 1:3 (+cemento)

    private val mezclaCementoArena = DosificacionMortero(
        cementoKg = 350.0,
        calKg = 0.0,
        arenaM3 = 1.1,
        relacionAguaCemento = 0.5
    )   // 1:3 (Para bloques)

    // Dosificación para 1 m3 de Revoque Grueso (1/4 Cemento : 1 Cal : 3 Arena)
    // Rendimiento estimado con desperdicio estándar
    private val recetaGrueso = DosificacionMortero(
        cementoKg = 75.0,  // Aprox 3 bolsas por m3 (es una mezcla "bastarda", lleva menos cemento que un concreto)
        calKg = 160.0,     // Mucha cal para plasticidad
        arenaM3 = 1.1,      // Arena común
        relacionAguaCemento = 0.6
    )

    // Dosificación para 1 m3 de Revoque Fino Tradicional (1/8 Cemento : 1 Aérea : 2 Arena Fina)
    private val recetaFino = DosificacionMortero(
        cementoKg = 30.0,  // Muy poco, solo para ligar
        calKg = 250.0,     // Pura cal aérea
        arenaM3 = 1.0,      // Arena fina (voladora)
        relacionAguaCemento = 0.5
    )

    override fun getDosificacionHormigon(tipo: TipoHormigon) = hormigonDB[tipo]

    override fun getPropiedadesLadrillo(tipo: TipoLadrillo) = ladrilloDB[tipo]

    override fun getDosificacionMortero(tipo: TipoLadrillo): DosificacionMortero {
        return when (tipo) {
            TipoLadrillo.BLOQUE_10,
            TipoLadrillo.BLOQUE_15,
            TipoLadrillo.BLOQUE_20 -> mezclaCementoArena
            else -> mezclaCalReforzada
        }
    }

    override fun getPesoHierroPorMetro(diametro: DiametroHierro): Double {
        return pesosHierro[diametro] ?: 0.0
    }

    fun getRecetaGrueso() = recetaGrueso
    fun getRecetaFino() = recetaFino
}