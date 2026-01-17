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

package org.m415x.materialcalc.data.repository

import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.coroutines.getBooleanFlow
import com.russhwolf.settings.coroutines.getDoubleFlow
import com.russhwolf.settings.coroutines.getIntFlow
import com.russhwolf.settings.coroutines.getStringFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.m415x.materialcalc.domain.model.CustomBrick
import org.m415x.materialcalc.domain.model.CustomIron
import org.m415x.materialcalc.domain.model.CustomRecipe
import org.m415x.materialcalc.ui.theme.ColorPalette
import org.m415x.materialcalc.ui.theme.ContrastMode
import org.m415x.materialcalc.ui.theme.ThemeMode

/**
 * Repositorio para la gestión de las configuraciones de la aplicación.
 *
 * @property settings Repositorio de configuraciones.
 */
@OptIn(ExperimentalSettingsApi::class)
class SettingsRepository(private val settings: ObservableSettings) {
    /**
     * Claves para la persistencia de datos.
     */
    private val KEY_THEME = "theme_mode"
    private val KEY_CONTRAST = "contrast_mode"
    private val KEY_COLOR_PALETTE = "color_palette"
    private val KEY_OUTDOOR_MODE = "outdoor_mode_enabled"
    private val KEY_BAG_CEMENT = "weight_bag_cement"
    private val KEY_BAG_LIME = "weight_bag_lime"
    private val KEY_BAG_PREMIX = "weight_bag_premix"
    private val KEY_VOL_BUCKET = "vol_bucket_liters"
    private val KEY_VOL_BARROW = "vol_barrow_liters"
    private val KEY_VOL_MIXER = "vol_mixer_liters"
    private val KEY_WASTE_CONCRETE = "waste_concrete_pct"
    private val KEY_WASTE_MORTAR = "waste_mortar_pct"
    private val KEY_WASTE_BRICKS = "waste_bricks_pct"
    private val KEY_WASTE_IRON_MAIN = "waste_iron_main_pct"
    private val KEY_WASTE_IRON_STIRRUP = "waste_iron_stirrup_pct"
    private val KEY_WASTE_PLASTER = "waste_plaster_pct"
    private val KEY_THICKNESS_FINE = "thickness_fine_mm"
    private val KEY_CUSTOM_BRICKS = "custom_bricks_list_json"
    private val KEY_HIDDEN_BRICK_IDS = "hidden_static_bricks_json"
    private val KEY_CUSTOM_IRONS = "custom_irons_json"
    private val KEY_HIDDEN_IRONS = "hidden_static_irons_json"
    private val KEY_CUSTOM_RECIPES = "custom_recipes_json"
    private val KEY_HIDDEN_RECIPES = "hidden_static_recipes_json"
    private val KEY_DEF_BRICK = "default_brick_id"
    private val KEY_DEF_CONCRETE_GEN = "default_concrete_general_id"
    private val KEY_DEF_CONCRETE_STR = "default_concrete_struct_id"
    private val KEY_DEF_PLASTER_ROUGH = "default_plaster_rough_id"

    // Serialización JSON
    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Flujos para listas y conjuntos serializados en JSON para la observación de cambios en las configuraciones
     */
    private inline fun <reified T> getListFlow(key: String): Flow<List<T>> =
        settings.getStringFlow(key, "[]").map {
            try {
                if (it.isBlank()) emptyList<T>()
                else json.decodeFromString<List<T>>(it)
            } catch (e: Exception) {
                emptyList<T>()
            }
        }

    private fun getSetFlow(key: String): Flow<Set<String>> =
        settings.getStringFlow(key, "[]").map {
            try {
                if (it.isBlank()) emptySet<String>()
                else json.decodeFromString<Set<String>>(it)
            } catch (e: Exception) {
                emptySet<String>()
            }
        }

    val themeMode: Flow<ThemeMode> = settings.getStringFlow(KEY_THEME, ThemeMode.System.name)
        .map { name ->
            try {
                ThemeMode.valueOf(name)
            } catch (e: Exception) {
                ThemeMode.System
            }
        }

    val contrastMode: Flow<ContrastMode> = settings.getStringFlow(KEY_CONTRAST, ContrastMode.Standard.name)
        .map { name ->
            try {
                ContrastMode.valueOf(name)
            } catch (e: Exception) {
                ContrastMode.Standard
            }
        }

    val colorPalette: Flow<ColorPalette> = settings.getStringFlow(KEY_COLOR_PALETTE, ColorPalette.Default.name)
        .map { name ->
            try {
                ColorPalette.valueOf(name)
            } catch (e: Exception) {
                ColorPalette.Default
            }
        }

    val outdoorMode: Flow<Boolean> = settings.getBooleanFlow(KEY_OUTDOOR_MODE, false)
    val bagCementKg: Flow<Int> = settings.getIntFlow(KEY_BAG_CEMENT, Defaults.DEFAULT_BAG_CEMENT)
    val bagLimeKg: Flow<Int> = settings.getIntFlow(KEY_BAG_LIME, Defaults.DEFAULT_BAG_LIME)
    val bagPremixKg: Flow<Int> = settings.getIntFlow(KEY_BAG_PREMIX, Defaults.DEFAULT_BAG_PREMIX)
    val bucketCapacityLiters: Flow<Double> = settings.getDoubleFlow(KEY_VOL_BUCKET, Defaults.DEFAULT_BUCKET_VOL)
    val barrowCapacityLiters: Flow<Double> = settings.getDoubleFlow(KEY_VOL_BARROW, Defaults.DEFAULT_BARROW_VOL)
    val mixerCapacityLiters: Flow<Double> = settings.getDoubleFlow(KEY_VOL_MIXER, Defaults.DEFAULT_MIXER_VOL)
    val wasteConcretePct = settings.getDoubleFlow(KEY_WASTE_CONCRETE, Defaults.DEFAULT_WASTE_CONCRETE)
    val wasteMortarPct = settings.getDoubleFlow(KEY_WASTE_MORTAR, Defaults.DEFAULT_WASTE_MORTAR)
    val wasteBricksPct = settings.getDoubleFlow(KEY_WASTE_BRICKS, Defaults.DEFAULT_WASTE_BRICK)
    val wasteIronMainPct = settings.getDoubleFlow(KEY_WASTE_IRON_MAIN, Defaults.DEFAULT_WASTE_IRON_MAIN)
    val wasteIronStirrupPct = settings.getDoubleFlow(KEY_WASTE_IRON_STIRRUP, Defaults.DEFAULT_WASTE_IRON_STIRRUP)
    val wastePlasterPct = settings.getDoubleFlow(KEY_WASTE_PLASTER, Defaults.DEFAULT_WASTE_PLASTER)
    val fineThicknessMm = settings.getDoubleFlow(KEY_THICKNESS_FINE, Defaults.DEFAULT_THICKNESS_FINE)
    val customBricks: Flow<List<CustomBrick>> = getListFlow(KEY_CUSTOM_BRICKS)
    val hiddenBrickIds: Flow<Set<String>> = getSetFlow(KEY_HIDDEN_BRICK_IDS)
    val customIrons: Flow<List<CustomIron>> = getListFlow(KEY_CUSTOM_IRONS)
    val hiddenIronIds: Flow<Set<String>> = getSetFlow(KEY_HIDDEN_IRONS)
    val customRecipes: Flow<List<CustomRecipe>> = getListFlow(KEY_CUSTOM_RECIPES)
    val hiddenRecipeIds: Flow<Set<String>> = getSetFlow(KEY_HIDDEN_RECIPES)

    // Valores por defecto
    val defaultBrickId: Flow<String> = settings.getStringFlow(KEY_DEF_BRICK, "LADRILLON")
    val defaultConcreteGenId: Flow<String> = settings.getStringFlow(KEY_DEF_CONCRETE_GEN, "H13")
    val defaultConcreteStrId: Flow<String> = settings.getStringFlow(KEY_DEF_CONCRETE_STR, "H17")
    val defaultPlasterRoughId: Flow<String> = settings.getStringFlow(KEY_DEF_PLASTER_ROUGH, "STD_JAHARRO")

    /**
     * Funciones genéricas para guardar y eliminar elementos en listas y conjuntos serializados en JSON.
     */
    private inline fun <reified T> saveItemToList(key: String, item: T, crossinline idSelector: (T) -> String) {
        val list = try {
            json.decodeFromString<MutableList<T>>(settings.getString(key, "[]"))
        } catch (e: Exception) {
            mutableListOf<T>()
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
            mutableSetOf<String>()
        }
        set.add(value)
        settings.putString(key, json.encodeToString(set))
    }

    private fun removeFromSet(key: String, value: String) {
        val set = try {
            json.decodeFromString<MutableSet<String>>(settings.getString(key, "[]"))
        } catch (e: Exception) {
            mutableSetOf<String>()
        }
        set.remove(value)
        settings.putString(key, json.encodeToString(set))
    }

    fun saveThemeMode(mode: ThemeMode) = settings.putString(KEY_THEME, mode.name)
    fun saveContrastMode(mode: ContrastMode) = settings.putString(KEY_CONTRAST, mode.name)
    fun saveColorPalette(palette: ColorPalette) = settings.putString(KEY_COLOR_PALETTE, palette.name)
    fun saveOutdoorMode(enabled: Boolean) = settings.putBoolean(KEY_OUTDOOR_MODE, enabled)

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
            "mixer" -> settings.putDouble(KEY_VOL_MIXER, liters)
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

    fun saveFineThickness(mm: Double) = settings.putDouble(KEY_THICKNESS_FINE, mm)
    fun saveCustomBrick(brick: CustomBrick) = saveItemToList(KEY_CUSTOM_BRICKS, brick) { it.id }
    fun deleteCustomBrick(id: String) = deleteItemFromList<CustomBrick>(KEY_CUSTOM_BRICKS) { it.id == id }
    fun hideStaticBrick(brickId: String) = addToSet(KEY_HIDDEN_BRICK_IDS, brickId)
    fun restoreStaticBrick(id: String) = removeFromSet(KEY_HIDDEN_BRICK_IDS, id)
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
    fun saveDefaultPlasterRough(id: String) = settings.putString(KEY_DEF_PLASTER_ROUGH, id)

    /**
     * Valores por defecto.
     */
    companion object Defaults {
        const val DEFAULT_BAG_CEMENT = 25
        const val DEFAULT_BAG_LIME = 25
        const val DEFAULT_BAG_PREMIX = 25
        const val DEFAULT_BUCKET_VOL = 10.0
        const val DEFAULT_BARROW_VOL = 90.0
        const val DEFAULT_MIXER_VOL = 80.0
        const val DEFAULT_WASTE_CONCRETE = 5.0
        const val DEFAULT_WASTE_MORTAR = 15.0
        const val DEFAULT_WASTE_BRICK = 5.0
        const val DEFAULT_WASTE_IRON_MAIN = 10.0
        const val DEFAULT_WASTE_IRON_STIRRUP = 5.0
        const val DEFAULT_WASTE_PLASTER = 10.0
        const val DEFAULT_THICKNESS_FINE = 3.0
    }
}