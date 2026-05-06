package com.notifiq.core.designsystem.component

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.util.LruCache

object AppIconCache {
    private const val MAX_CACHE_SIZE = 50

    private val cache: LruCache<String, Drawable> = object : LruCache<String, Drawable>(MAX_CACHE_SIZE) {
        override fun sizeOf(key: String, drawable: Drawable): Int {
            return 1
        }
    }

    fun getAppIcon(context: Context, packageName: String): Drawable? {
        val cached = cache.get(packageName)
        if (cached != null) {
            return cached
        }

        return try {
            val drawable = context.packageManager.getApplicationIcon(packageName)
            cache.put(packageName, drawable)
            drawable
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
    }

    fun clearCache() {
        cache.evictAll()
    }

    fun remove(packageName: String) {
        cache.remove(packageName)
    }
}