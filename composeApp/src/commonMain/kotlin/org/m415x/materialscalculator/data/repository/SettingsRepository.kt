package org.m415x.materialscalculator.data.repository

import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.coroutines.getBooleanFlow
import com.russhwolf.settings.coroutines.getDoubleFlow
import com.russhwolf.settings.coroutines.getIntFlow
import com.russhwolf.settings.coroutines.getStringFlow
import com.russhwolf.settings.set
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

import org.m415x.materialscalculator.domain.model.CustomBrick
import org.m415x.materialscalculator.domain.model.CustomIron
import org.m415x.materialscalculator.domain.model.CustomRecipe
import org.m415x.materialscalculator.ui.theme.ContrastMode
import org.m415x.materialscalculator.ui.theme.ThemeMode

/**
 * Repositorio de configuración que maneja la persistencia de preferencias del usuario.
 *
 * @property settings Instancia de ObservableSettings para la persistencia de datos.
 */
@OptIn(ExperimentalSettingsApi::class)
class SettingsRepository(private val settings: ObservableSettings) {
    /**
     * Claves para la persistencia de datos.
     *
     * @property KEY_THEME Clave para el modo de tema.
     * @property KEY_CONTRAST Clave para el modo de contraste.
     * @property KEY_OUTDOOR_MODE Clave para el modo de outdoor.
     * @property KEY_BAG_CEMENT Clave para el peso de la bolsa de cemento.
     * @property KEY_BAG_LIME Clave para el peso de la bolsa de cal.
     * @property KEY_BAG_PREMIX Clave para el peso de la bolsa de premezclado.
     * @property KEY_VOL_BUCKET Clave para el volumen del balde.
     * @property KEY_VOL_BARROW Clave para el volumen de la carretilla.
     */
    private val KEY_THEME = "theme_mode"
    private val KEY_CONTRAST = "contrast_mode"
    private val KEY_OUTDOOR_MODE = "outdoor_mode_enabled"
    private val KEY_BAG_CEMENT = "weight_bag_cement"
    private val KEY_BAG_LIME = "weight_bag_lime"
    private val KEY_BAG_PREMIX = "weight_bag_premix"
    private val KEY_VOL_BUCKET = "vol_bucket_liters"
    private val KEY_VOL_BARROW = "vol_barrow_liters"
    private val KEY_WASTE_CONCRETE = "waste_concrete_pct"
    private val KEY_WASTE_MORTAR = "waste_mortar_pct" // Asiento
    private val KEY_WASTE_BRICKS = "waste_bricks_pct"
    private val KEY_WASTE_IRON_MAIN = "waste_iron_main_pct"
    private val KEY_WASTE_IRON_STIRRUP = "waste_iron_stirrup_pct"
    private val KEY_WASTE_PLASTER = "waste_plaster_pct" // Revoque
    private val KEY_THICKNESS_FINE = "thickness_fine_mm" // En milímetros para la UI
    private val KEY_CUSTOM_BRICKS = "custom_bricks_list_json"
    private val KEY_HIDDEN_BRICK_IDS = "hidden_static_bricks_json"
    private val KEY_CUSTOM_IRONS = "custom_irons_json"
    private val KEY_HIDDEN_IRONS = "hidden_static_irons_json"

    private val KEY_CUSTOM_RECIPES = "custom_recipes_json"
    private val KEY_HIDDEN_RECIPES = "hidden_static_recipes_json"
    private val KEY_DEF_BRICK = "default_brick_id"
    private val KEY_DEF_CONCRETE_GEN = "default_concrete_general_id" // Pisos, contrapisos
    private val KEY_DEF_CONCRETE_STR = "default_concrete_struct_id"  // Vigas, columnas

    // Configuración de JSON (Lenient ayuda a ser flexible)
    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Flujos de configuración.
     *
     * @property themeMode Flujo de configuración del modo de tema.
     * @property contrastMode Flujo de configuración del modo de contraste.
     * @property outdoorMode Flujo de configuración del modo de outdoor.
     * @property bagCementKg Flujo de configuración del peso de la bolsa de cemento.
     * @property bagLimeKg Flujo de configuración del peso de la bolsa de cal.
     * @property bagPremixKg Flujo de configuración del peso de la bolsa de premezclado.
     * @property bucketCapacityLiters Flujo de configuración del volumen del balde.
     * @property barrowCapacityLiters Flujo de configuración del volumen de la carretilla.
     */
    private inline fun <reified T> getListFlow(key: String): Flow<List<T>> =
        settings.getStringFlow(key, "[]").map {
            try {
                if (it.isBlank()) emptyList() else json.decodeFromString(it)
            } catch (e: Exception) {
                emptyList()
            }
        }

    private fun getSetFlow(key: String): Flow<Set<String>> =
        settings.getStringFlow(key, "[]").map {
            try {
                if (it.isBlank()) emptySet() else json.decodeFromString(it)
            } catch (e: Exception) {
                emptySet()
            }
        }

    val themeMode: Flow<ThemeMode> = settings
        .getStringFlow(KEY_THEME, ThemeMode.System.name)
        .map { name ->
            try { ThemeMode.valueOf(name) } catch (e: Exception) { ThemeMode.System }
        }
    val contrastMode: Flow<ContrastMode> = settings
        .getStringFlow(KEY_CONTRAST, ContrastMode.Standard.name)
        .map { name ->
            try { ContrastMode.valueOf(name) } catch (e: Exception) { ContrastMode.Standard }
        }
    val outdoorMode: Flow<Boolean> = settings.getBooleanFlow(KEY_OUTDOOR_MODE, false)
    val bagCementKg: Flow<Int> = settings.getIntFlow(KEY_BAG_CEMENT, DEFAULT_BAG_CEMENT)
    val bagLimeKg: Flow<Int> = settings.getIntFlow(KEY_BAG_LIME, DEFAULT_BAG_LIME)
    val bagPremixKg: Flow<Int> = settings.getIntFlow(KEY_BAG_PREMIX, DEFAULT_BAG_PREMIX)
    val bucketCapacityLiters: Flow<Double> = settings.getDoubleFlow(KEY_VOL_BUCKET, DEFAULT_BUCKET_VOL)
    val barrowCapacityLiters: Flow<Double> = settings.getDoubleFlow(KEY_VOL_BARROW, DEFAULT_BARROW_VOL)
    val wasteConcretePct = settings.getDoubleFlow(KEY_WASTE_CONCRETE, DEFAULT_WASTE_CONCRETE)
    val wasteMortarPct = settings.getDoubleFlow(KEY_WASTE_MORTAR, DEFAULT_WASTE_MORTAR)
    val wasteBricksPct = settings.getDoubleFlow(KEY_WASTE_BRICKS, DEFAULT_WASTE_BRICK)
    val wasteIronMainPct = settings.getDoubleFlow(KEY_WASTE_IRON_MAIN, DEFAULT_WASTE_IRON_MAIN)
    val wasteIronStirrupPct = settings.getDoubleFlow(KEY_WASTE_IRON_STIRRUP, DEFAULT_WASTE_IRON_STIRRUP)
    val wastePlasterPct = settings.getDoubleFlow(KEY_WASTE_PLASTER, DEFAULT_WASTE_PLASTER)
    val fineThicknessMm = settings.getDoubleFlow(KEY_THICKNESS_FINE, DEFAULT_THICKNESS_FINE)
    val customBricks: Flow<List<CustomBrick>> = settings.getStringFlow(KEY_CUSTOM_BRICKS, "[]")
        .map { jsonString ->
            try {
                if (jsonString.isBlank()) emptyList()
                else json.decodeFromString<List<CustomBrick>>(jsonString)
            } catch (e: Exception) {
                emptyList() // Si falla, devolvemos lista vacía para no romper la app
            }
        }
    val hiddenBrickIds: Flow<Set<String>> = settings.getStringFlow(KEY_HIDDEN_BRICK_IDS, "[]")
        .map { jsonStr ->
            try {
                if (jsonStr.isBlank()) emptySet()
                else json.decodeFromString<Set<String>>(jsonStr)
            } catch (e: Exception) {
                emptySet()
            }
        }
    val customIrons: Flow<List<CustomIron>> = getListFlow(KEY_CUSTOM_IRONS)
    val hiddenIronIds: Flow<Set<String>> = getSetFlow(KEY_HIDDEN_IRONS)
    val customRecipes: Flow<List<CustomRecipe>> = getListFlow(KEY_CUSTOM_RECIPES)
    val hiddenRecipeIds: Flow<Set<String>> = getSetFlow(KEY_HIDDEN_RECIPES)
    val defaultBrickId = settings.getStringFlow(KEY_DEF_BRICK, "")
    val defaultConcreteGenId = settings.getStringFlow(KEY_DEF_CONCRETE_GEN, "")
    val defaultConcreteStrId = settings.getStringFlow(KEY_DEF_CONCRETE_STR, "")

    /**
     * Funciones de escritura.
     *
     * @property saveThemeMode Guarda el modo de tema.
     * @property saveContrastMode Guarda el modo de contraste.
     * @property saveOutdoorMode Guarda el modo de outdoor.
     * @property saveBagWeight Guarda el peso de la bolsa.
     * @property saveVolumeCapacity Guarda el volumen de recipientes.
     */
    private inline fun <reified T> saveItemToList(key: String, item: T, crossinline idSelector: (T) -> String) {
        val list = try {
            json.decodeFromString<MutableList<T>>(settings.getString(key, "[]"))
        } catch (e: Exception) {
            mutableListOf()
        }
        val index = list.indexOfFirst { idSelector(it) == idSelector(item) }
        if (index != -1) list[index] = item else list.add(item)
        settings.putString(key, json.encodeToString(list))
    }

    private inline fun <reified T> deleteItemFromList(key: String, crossinline predicate: (T) -> Boolean) {
        val list = try {
            json.decodeFromString<MutableList<T>>(settings.getString(key, "[]"))
        } catch (e: Exception) {
            return
        }
        list.removeAll { predicate(it) }
        settings.putString(key, json.encodeToString(list))
    }

    private fun addToSet(key: String, value: String) {
        val set = try {
            json.decodeFromString<MutableSet<String>>(settings.getString(key, "[]"))
        } catch (e: Exception) {
            mutableSetOf()
        }
        set.add(value)
        settings.putString(key, json.encodeToString(set))
    }

    private fun removeFromSet(key: String, value: String) {
        val set = try {
            json.decodeFromString<MutableSet<String>>(settings.getString(key, "[]"))
        } catch (e: Exception) {
            mutableSetOf()
        }
        set.remove(value)
        settings.putString(key, json.encodeToString(set))
    }
    fun saveThemeMode(mode: ThemeMode) {
        settings[KEY_THEME] = mode.name
    }
    fun saveContrastMode(mode: ContrastMode) {
        settings[KEY_CONTRAST] = mode.name
    }

    fun saveOutdoorMode(enabled: Boolean) {
        settings.putBoolean(KEY_OUTDOOR_MODE, enabled)
    }

    fun saveBagWeight(type: String, kg: Int) {
        when (type) {
            "cement" -> settings.putInt(KEY_BAG_CEMENT, kg)
            "lime" -> settings.putInt(KEY_BAG_LIME, kg)
            "premix" -> settings.putInt(KEY_BAG_PREMIX, kg)
        }
    }

    fun saveVolumeCapacity(type: String, liters: Double) {
        when (type) {
            "bucket" -> settings.putDouble(KEY_VOL_BUCKET, liters)
            "barrow" -> settings.putDouble(KEY_VOL_BARROW, liters)
        }
    }

    fun saveWaste(type: String, percentage: Double) {
        val key = when (type) {
            "concrete" -> KEY_WASTE_CONCRETE
            "mortar" -> KEY_WASTE_MORTAR
            "bricks" -> KEY_WASTE_BRICKS
            "iron_main" -> KEY_WASTE_IRON_MAIN
            "iron_stirrup" -> KEY_WASTE_IRON_STIRRUP
            "plaster" -> KEY_WASTE_PLASTER
            else -> return
        }
        settings.putDouble(key, percentage)
    }

    fun saveFineThickness(mm: Double) {
        settings.putDouble(KEY_THICKNESS_FINE, mm)
    }

    // Agregar o Actualizar un ladrillo
    fun saveCustomBrick(brick: CustomBrick) {
        // Obtenemos la lista actual (síncrona para editarla)
        val currentJson = settings.getString(KEY_CUSTOM_BRICKS, "[]")
        val currentList: MutableList<CustomBrick> = try {
            if (currentJson.isBlank()) mutableListOf()
            else json.decodeFromString<List<CustomBrick>>(currentJson).toMutableList()
        } catch (e: Exception) {
            mutableListOf()
        }

        // Buscamos si ya existe (por ID) para actualizarlo, sino lo agregamos
        val index = currentList.indexOfFirst { it.id == brick.id }
        if (index != -1) {
            currentList[index] = brick // Actualizar
        } else {
            currentList.add(brick) // Agregar nuevo
        }

        // Guardamos de nuevo a JSON
        val newJson = json.encodeToString(currentList)
        settings.putString(KEY_CUSTOM_BRICKS, newJson)
    }

    // Eliminar un ladrillo
    fun deleteCustomBrick(id: String) {
        val currentJson = settings.getString(KEY_CUSTOM_BRICKS, "[]")
        val currentList: MutableList<CustomBrick> = try {
            if (currentJson.isBlank()) mutableListOf()
            else json.decodeFromString<List<CustomBrick>>(currentJson).toMutableList()
        } catch (e: Exception) {
            return
        }

        // Removemos
        currentList.removeAll { it.id == id }

        val newJson = json.encodeToString(currentList)
        settings.putString(KEY_CUSTOM_BRICKS, newJson)
    }

    // Ocultar un ladrillo de fábrica
    fun hideStaticBrick(brickId: String) {
        val currentJson = settings.getString(KEY_HIDDEN_BRICK_IDS, "[]")
        val currentSet = try {
            json.decodeFromString<MutableSet<String>>(currentJson)
        } catch (e: Exception) {
            mutableSetOf()
        }

        currentSet.add(brickId)
        settings.putString(KEY_HIDDEN_BRICK_IDS, json.encodeToString(currentSet))
    }

    // Restaurar un ladrillo (o todos)
    fun restoreStaticBrick(brickId: String) {
        val currentJson = settings.getString(KEY_HIDDEN_BRICK_IDS, "[]")
        val currentSet = try {
            json.decodeFromString<MutableSet<String>>(currentJson)
        } catch (e: Exception) {
            mutableSetOf()
        }

        currentSet.remove(brickId)
        settings.putString(KEY_HIDDEN_BRICK_IDS, json.encodeToString(currentSet))
    }

    fun saveCustomIron(item: CustomIron) = saveItemToList(KEY_CUSTOM_IRONS, item) { it.id }
    fun deleteCustomIron(id: String) = deleteItemFromList<CustomIron>(KEY_CUSTOM_IRONS) { it.id == id }
    fun hideStaticIron(id: String) = addToSet(KEY_HIDDEN_IRONS, id)
    fun restoreStaticIron(id: String) = removeFromSet(KEY_HIDDEN_IRONS, id)
    fun saveCustomRecipe(item: CustomRecipe) = saveItemToList(KEY_CUSTOM_RECIPES, item) { it.id }
    fun deleteCustomRecipe(id: String) = deleteItemFromList<CustomRecipe>(KEY_CUSTOM_RECIPES) { it.id == id }
    fun hideStaticRecipe(id: String) = addToSet(KEY_HIDDEN_RECIPES, id)
    fun restoreStaticRecipe(id: String) = removeFromSet(KEY_HIDDEN_RECIPES, id)
    fun saveDefaultBrick(id: String) = settings.putString(KEY_DEF_BRICK, id)
    fun saveDefaultConcreteGen(id: String) = settings.putString(KEY_DEF_CONCRETE_GEN, id)
    fun saveDefaultConcreteStr(id: String) = settings.putString(KEY_DEF_CONCRETE_STR, id)

    // COMPANION OBJECT: Constantes públicas accesibles desde la UI
    companion object Defaults {
        const val DEFAULT_BAG_CEMENT = 25
        const val DEFAULT_BAG_LIME = 25
        const val DEFAULT_BAG_PREMIX = 25
        const val DEFAULT_BUCKET_VOL = 10.0
        const val DEFAULT_BARROW_VOL = 90.0
        const val DEFAULT_WASTE_CONCRETE = 5.0
        const val DEFAULT_WASTE_MORTAR = 15.0
        const val DEFAULT_WASTE_BRICK = 5.0
        const val DEFAULT_WASTE_IRON_MAIN = 10.0
        const val DEFAULT_WASTE_IRON_STIRRUP = 5.0
        const val DEFAULT_WASTE_PLASTER = 10.0
        const val DEFAULT_THICKNESS_FINE = 3.0
    }
}