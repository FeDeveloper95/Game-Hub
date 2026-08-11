package com.fedeveloper95.games.services

import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Icon
import android.os.Build
import com.fedeveloper95.games.MainActivity
import com.fedeveloper95.games.R
import com.fedeveloper95.games.services.mainactivity.GameApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ShortcutHelper {
    suspend fun updateDynamicShortcuts(context: Context, games: List<GameApp>) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1 && games.isNotEmpty()) {
            withContext(Dispatchers.IO) {
                try {
                    val shortcutManager = context.getSystemService(ShortcutManager::class.java)
                    if (shortcutManager != null) {
                        val favorites = games.filter { it.isFavorite }.take(4)
                        val shortcuts = favorites.mapIndexed { index, game ->
                            val intent = Intent(context, MainActivity::class.java).apply {
                                action = Intent.ACTION_VIEW
                                putExtra("LAUNCH_PKG", game.packageName)
                            }

                            val pm = context.packageManager
                            val iconBitmap = try {
                                val drawable = pm.getApplicationIcon(game.packageName)
                                val size = 108
                                val padding = 20
                                val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
                                val canvas = Canvas(bitmap)
                                drawable.setBounds(padding, padding, size - padding, size - padding)
                                drawable.draw(canvas)
                                bitmap
                            } catch (e: Exception) {
                                null
                            }

                            val icon = if (iconBitmap != null) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    Icon.createWithAdaptiveBitmap(iconBitmap)
                                } else {
                                    Icon.createWithBitmap(iconBitmap)
                                }
                            } else {
                                Icon.createWithResource(context, R.mipmap.ic_launcher)
                            }

                            ShortcutInfo.Builder(context, "shortcut_${game.packageName}")
                                .setShortLabel(game.customName ?: game.name)
                                .setLongLabel(game.customName ?: game.name)
                                .setIcon(icon)
                                .setIntent(intent)
                                .setRank(index)
                                .build()
                        }
                        shortcutManager.dynamicShortcuts = shortcuts
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}