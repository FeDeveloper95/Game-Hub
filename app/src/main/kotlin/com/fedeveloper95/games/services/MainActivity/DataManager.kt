package com.fedeveloper95.games.services.mainactivity

import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Process
import android.provider.MediaStore
import android.util.LruCache
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Gamepad
import androidx.compose.material.icons.rounded.SmartToy
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.VideogameAsset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import kotlin.math.roundToInt

data class GameApp(
    val name: String,
    val packageName: String,
    val launchIntent: Intent?,
    val launchCount: Int = 0,
    val totalPlayTime: Long = 0,
    val customName: String? = null,
    val customIconUri: String? = null,
    val isFavorite: Boolean = false
)

class GameViewModel : ViewModel() {
    private val _games = MutableStateFlow<List<GameApp>>(emptyList())
    val games: StateFlow<List<GameApp>> = _games

    private val _allApps = MutableStateFlow<List<GameApp>>(emptyList())
    val allApps: StateFlow<List<GameApp>> = _allApps

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val HIDDEN_GAMES_PREF = "hidden_games"
    private val MANUAL_GAMES_PREF = "manual_games"
    private val FAVORITE_GAMES_PREF = "favorite_games"
    private val GAME_ORDER_PREF = "game_order"
    private val PREF_PREFIX_COUNT = "play_count_"
    private val PREF_SORT_TYPE = "pref_sort_type"
    private val PREF_STATS_INTERVAL = "pref_stats_interval"

    private val PREF_CUSTOM_NAME_PREFIX = "custom_name_"
    private val PREF_CUSTOM_ICON_PREFIX = "custom_icon_"

    private var loadJob: Job? = null

    private val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
    private val cacheSize = maxMemory / 8
    private val iconCache = object : LruCache<String, ImageBitmap>(cacheSize) {
        override fun sizeOf(key: String, bitmap: ImageBitmap): Int {
            return (bitmap.width * bitmap.height * 4) / 1024
        }
    }

    private val vectorIconCache = LruCache<String, Pair<ImageVector, Color>>(100)

    fun getCachedBitmap(key: String): ImageBitmap? {
        return iconCache.get(key)
    }

    fun getCachedVectorIcon(key: String): Pair<ImageVector, Color>? {
        return vectorIconCache.get(key)
    }

    suspend fun loadIcon(context: Context, packageName: String, customIconUri: String?): Any? = withContext(Dispatchers.IO) {
        val cacheKey = customIconUri ?: packageName

        val cachedVector = vectorIconCache.get(cacheKey)
        if (cachedVector != null) return@withContext cachedVector

        val cachedBitmap = iconCache.get(cacheKey)
        if (cachedBitmap != null) return@withContext cachedBitmap

        try {
            if (customIconUri?.startsWith("builtin://") == true) {
                val parts = customIconUri.removePrefix("builtin://").split("#")
                val iconName = parts[0]
                val color = if (parts.size > 1) Color(parts[1].toInt()) else Color.Gray

                val iconVector = when (iconName) {
                    "star" -> Icons.Rounded.Star
                    "controller" -> Icons.Rounded.SportsEsports
                    "bolt" -> Icons.Rounded.Bolt
                    "heart" -> Icons.Rounded.Favorite
                    "gamepad" -> Icons.Rounded.Gamepad
                    "videogame" -> Icons.Rounded.VideogameAsset
                    "toy" -> Icons.Rounded.SmartToy
                    else -> Icons.Rounded.SportsEsports
                }
                val vectorData = Pair(iconVector, color)
                vectorIconCache.put(cacheKey, vectorData)
                return@withContext vectorData
            }

            val bitmap = if (customIconUri?.startsWith("app://") == true) {
                val targetPkg = customIconUri.removePrefix("app://")
                context.packageManager.getApplicationIcon(targetPkg).toBitmap()
            } else if (!customIconUri.isNullOrEmpty()) {
                val uri = Uri.parse(customIconUri)
                val androidBitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val source = ImageDecoder.createSource(context.contentResolver, uri)
                    ImageDecoder.decodeBitmap(source)
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                }
                androidBitmap.copy(Bitmap.Config.ARGB_8888, false)
            } else {
                context.packageManager.getApplicationIcon(packageName).toBitmap()
            }

            val MAX_ICON_SIZE = 192
            val finalBitmap = if (bitmap.width > MAX_ICON_SIZE || bitmap.height > MAX_ICON_SIZE) {
                val ratio = minOf(MAX_ICON_SIZE.toFloat() / bitmap.width, MAX_ICON_SIZE.toFloat() / bitmap.height)
                val width = (bitmap.width * ratio).roundToInt()
                val height = (bitmap.height * ratio).roundToInt()
                Bitmap.createScaledBitmap(bitmap, width, height, true)
            } else {
                bitmap
            }

            val imageBitmap = finalBitmap.asImageBitmap()
            iconCache.put(cacheKey, imageBitmap)
            return@withContext imageBitmap

        } catch (e: Exception) {
            e.printStackTrace()
            try {
                val fallbackBitmap = context.packageManager.getApplicationIcon(packageName).toBitmap().asImageBitmap()
                iconCache.put(packageName, fallbackBitmap)
                return@withContext fallbackBitmap
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
        return@withContext null
    }

    fun clearIconCache() {
        iconCache.evictAll()
        vectorIconCache.evictAll()
    }

    fun loadGames(context: Context) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _isLoading.value = true
            val packageManager = context.packageManager
            val prefs = context.getSharedPreferences("game_hub_prefs", Context.MODE_PRIVATE)
            val settings = context.getSharedPreferences("game_hub_settings", Context.MODE_PRIVATE)
            val customDataPrefs = context.getSharedPreferences("game_hub_custom_data", Context.MODE_PRIVATE)

            val loadedData = withContext(Dispatchers.IO) {
                try {
                    val hiddenGames = getPrefsSet(context, HIDDEN_GAMES_PREF)
                    val manualGames = getPrefsSet(context, MANUAL_GAMES_PREF)
                    val favoriteGames = getPrefsSet(context, FAVORITE_GAMES_PREF)
                    val savedOrder = getSavedOrder(context)
                    val sortType = settings.getString(PREF_SORT_TYPE, "Alphabetical") ?: "Alphabetical"
                    val statsInterval = settings.getFloat(PREF_STATS_INTERVAL, 3f).roundToInt()

                    val intent = Intent(Intent.ACTION_MAIN, null)
                    intent.addCategory(Intent.CATEGORY_LAUNCHER)
                    val resolveInfos = packageManager.queryIntentActivities(intent, 0)

                    val gamesList = mutableListOf<GameApp>()
                    val allAppsList = mutableListOf<GameApp>()

                    val usageStats = getUsageStats(context, statsInterval)

                    for (resolveInfo in resolveInfos) {
                        val packageName = resolveInfo.activityInfo.packageName ?: continue
                        val originalName = resolveInfo.loadLabel(packageManager).toString()
                        val launchIntent = packageManager.getLaunchIntentForPackage(packageName) ?: continue

                        val appInfo = try {
                            packageManager.getApplicationInfo(packageName, 0)
                        } catch (e: PackageManager.NameNotFoundException) { continue }

                        val isDeclaredGame = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            appInfo.category == ApplicationInfo.CATEGORY_GAME
                        } else {
                            (appInfo.flags and ApplicationInfo.FLAG_IS_GAME) != 0
                        }

                        val isManualGame = manualGames.contains(packageName)
                        val isHidden = hiddenGames.contains(packageName)
                        val isFavorite = favoriteGames.contains(packageName)
                        val count = prefs.getInt(PREF_PREFIX_COUNT + packageName, 0)
                        val time = usageStats[packageName] ?: 0L

                        val customName = customDataPrefs.getString(PREF_CUSTOM_NAME_PREFIX + packageName, null)
                        val customIconUri = customDataPrefs.getString(PREF_CUSTOM_ICON_PREFIX + packageName, null)

                        val displayName = if (!customName.isNullOrBlank()) customName else originalName

                        val app = GameApp(
                            name = displayName,
                            packageName = packageName,
                            launchIntent = launchIntent,
                            launchCount = count,
                            totalPlayTime = time,
                            customName = customName,
                            customIconUri = customIconUri,
                            isFavorite = isFavorite
                        )

                        if (!isDeclaredGame || isHidden) {
                            if (!isManualGame) allAppsList.add(app)
                        }

                        if ((isDeclaredGame || isManualGame) && !isHidden) {
                            gamesList.add(app)
                        }
                    }

                    val sortedGames = when (sortType) {
                        "Time" -> gamesList.sortedByDescending { it.totalPlayTime }
                        "Custom" -> {
                            if (savedOrder.isNotEmpty()) {
                                gamesList.sortedBy { game ->
                                    val index = savedOrder.indexOf(game.packageName)
                                    if (index != -1) index else Int.MAX_VALUE
                                }
                            } else {
                                gamesList.sortedBy { it.name }
                            }
                        }
                        else -> gamesList.sortedBy { it.name }
                    }.sortedByDescending { it.isFavorite }

                    Pair(sortedGames, allAppsList.sortedBy { it.name })
                } catch (e: Exception) {
                    Pair(emptyList(), emptyList())
                }
            }

            loadedData.first.forEach { game ->
                launch(Dispatchers.IO) {
                    loadIcon(context, game.packageName, game.customIconUri)
                }
            }

            _games.value = loadedData.first
            _allApps.value = loadedData.second
            _isLoading.value = false
        }
    }

    fun updateCustomGameData(context: Context, packageName: String, customName: String?, customIconUri: String?, isFavorite: Boolean) {
        val customDataPrefs = context.getSharedPreferences("game_hub_custom_data", Context.MODE_PRIVATE)
        val editor = customDataPrefs.edit()

        if (customName.isNullOrBlank()) {
            editor.remove(PREF_CUSTOM_NAME_PREFIX + packageName)
        } else {
            editor.putString(PREF_CUSTOM_NAME_PREFIX + packageName, customName)
        }

        if (customIconUri.isNullOrBlank()) {
            editor.remove(PREF_CUSTOM_ICON_PREFIX + packageName)
        } else {
            editor.putString(PREF_CUSTOM_ICON_PREFIX + packageName, customIconUri)
        }
        editor.commit()

        val cacheKey = customIconUri ?: packageName
        iconCache.remove(cacheKey)
        vectorIconCache.remove(cacheKey)

        if (isFavorite) {
            addToPrefs(context, FAVORITE_GAMES_PREF, packageName)
        } else {
            removeFromPrefs(context, FAVORITE_GAMES_PREF, packageName)
        }

        loadGames(context)
    }

    private fun getUsageStats(context: Context, intervalIndex: Int): Map<String, Long> {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName)
        } else {
            appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName)
        }

        if (mode != AppOpsManager.MODE_ALLOWED) return emptyMap()

        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val calendar = Calendar.getInstance()
        val endTime = calendar.timeInMillis

        when (intervalIndex) {
            0 -> calendar.add(Calendar.DAY_OF_YEAR, -1)
            1 -> calendar.add(Calendar.WEEK_OF_YEAR, -1)
            2 -> calendar.add(Calendar.MONTH, -1)
            3 -> calendar.add(Calendar.YEAR, -1)
        }
        val startTime = calendar.timeInMillis

        val stats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime)
        val usageMap = mutableMapOf<String, Long>()

        if (stats != null) {
            for (usageStat in stats) {
                val current = usageMap[usageStat.packageName] ?: 0L
                usageMap[usageStat.packageName] = current + usageStat.totalTimeInForeground
            }
        }
        return usageMap
    }

    fun incrementLaunchCount(context: Context, packageName: String) {
        val prefs = context.getSharedPreferences("game_hub_prefs", Context.MODE_PRIVATE)
        val currentCount = prefs.getInt(PREF_PREFIX_COUNT + packageName, 0)
        prefs.edit().putInt(PREF_PREFIX_COUNT + packageName, currentCount + 1).commit()

        val updatedGames = _games.value.map { game ->
            if (game.packageName == packageName) {
                game.copy(launchCount = currentCount + 1)
            } else {
                game
            }
        }
        _games.value = updatedGames
    }

    fun updateGamesOrder(newOrder: List<GameApp>) {
        _games.value = newOrder
    }

    fun saveOrder(context: Context) {
        val orderList = _games.value.map { it.packageName }
        val orderString = orderList.joinToString(",")
        val prefs = context.getSharedPreferences("game_hub_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString(GAME_ORDER_PREF, orderString).commit()
    }

    fun switchToCustomSort(context: Context) {
        saveOrder(context)
        val prefs = context.getSharedPreferences("game_hub_settings", Context.MODE_PRIVATE)
        prefs.edit().putString(PREF_SORT_TYPE, "Custom").commit()
    }

    private fun getSavedOrder(context: Context): List<String> {
        val prefs = context.getSharedPreferences("game_hub_prefs", Context.MODE_PRIVATE)
        val orderString = prefs.getString(GAME_ORDER_PREF, "") ?: ""
        return if (orderString.isNotEmpty()) orderString.split(",") else emptyList()
    }

    fun hideGame(context: Context, packageName: String) {
        removeFromPrefs(context, MANUAL_GAMES_PREF, packageName)
        addToPrefs(context, HIDDEN_GAMES_PREF, packageName)
        removeFromPrefs(context, FAVORITE_GAMES_PREF, packageName)

        val customDataPrefs = context.getSharedPreferences("game_hub_custom_data", Context.MODE_PRIVATE)
        customDataPrefs.edit()
            .remove(PREF_CUSTOM_NAME_PREFIX + packageName)
            .remove(PREF_CUSTOM_ICON_PREFIX + packageName)
            .commit()

        loadGames(context)
    }

    fun addManualGame(context: Context, packageName: String) {
        removeFromPrefs(context, HIDDEN_GAMES_PREF, packageName)
        addToPrefs(context, MANUAL_GAMES_PREF, packageName)
        loadGames(context)
    }

    private fun getPrefsSet(context: Context, key: String): Set<String> {
        val prefs: SharedPreferences = context.getSharedPreferences("game_hub_prefs", Context.MODE_PRIVATE)
        return prefs.getStringSet(key, emptySet()) ?: emptySet()
    }

    private fun addToPrefs(context: Context, key: String, value: String) {
        val prefs = context.getSharedPreferences("game_hub_prefs", Context.MODE_PRIVATE)
        val set = prefs.getStringSet(key, null)?.toMutableSet() ?: mutableSetOf()
        set.add(value)
        prefs.edit().putStringSet(key, set).commit()
    }

    private fun removeFromPrefs(context: Context, key: String, value: String) {
        val prefs = context.getSharedPreferences("game_hub_prefs", Context.MODE_PRIVATE)
        val set = prefs.getStringSet(key, null)?.toMutableSet() ?: mutableSetOf()
        if (set.contains(value)) {
            set.remove(value)
            prefs.edit().putStringSet(key, set).commit()
        }
    }
}

object GamesCacheManager {
    fun saveGames(context: Context, games: List<GameApp>) {
        val prefs = context.getSharedPreferences("game_hub_cache", Context.MODE_PRIVATE)
        val data = games.joinToString("|||") {
            "${it.packageName}:::${it.name}:::${it.customName ?: ""}:::${it.customIconUri ?: ""}:::${it.isFavorite}:::${it.launchCount}:::${it.totalPlayTime}"
        }
        prefs.edit().putString("cached_games", data).apply()
    }

    fun loadGames(context: Context): List<GameApp> {
        val prefs = context.getSharedPreferences("game_hub_cache", Context.MODE_PRIVATE)
        val data = prefs.getString("cached_games", "") ?: ""
        if (data.isEmpty()) return emptyList()

        val pm = context.packageManager
        return data.split("|||").mapNotNull {
            val parts = it.split(":::")
            if (parts.size >= 7) {
                val pkg = parts[0]
                val intent = pm.getLaunchIntentForPackage(pkg)
                GameApp(
                    packageName = pkg,
                    name = parts[1],
                    customName = parts[2].takeIf { it.isNotEmpty() },
                    customIconUri = parts[3].takeIf { it.isNotEmpty() },
                    isFavorite = parts[4].toBoolean(),
                    launchCount = parts[5].toIntOrNull() ?: 0,
                    totalPlayTime = parts[6].toLongOrNull() ?: 0L,
                    launchIntent = intent
                )
            } else null
        }
    }
}

object AllAppsCacheManager {
    fun saveApps(context: Context, apps: List<GameApp>) {
        val prefs = context.getSharedPreferences("game_hub_all_apps_cache", Context.MODE_PRIVATE)
        val data = apps.joinToString("|||") { "${it.packageName}:::${it.name}" }
        prefs.edit().putString("cached_all_apps", data).apply()
    }

    fun loadApps(context: Context): List<GameApp> {
        val prefs = context.getSharedPreferences("game_hub_all_apps_cache", Context.MODE_PRIVATE)
        val data = prefs.getString("cached_all_apps", "") ?: ""
        if (data.isEmpty()) return emptyList()

        val pm = context.packageManager
        return data.split("|||").mapNotNull {
            val parts = it.split(":::")
            if (parts.size >= 2) {
                val pkg = parts[0]
                GameApp(
                    packageName = pkg,
                    name = parts[1],
                    launchIntent = pm.getLaunchIntentForPackage(pkg)
                )
            } else null
        }
    }
}