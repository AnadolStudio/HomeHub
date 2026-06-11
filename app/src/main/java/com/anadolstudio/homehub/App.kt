package com.anadolstudio.homehub

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.anadolstudio.homehub.di.DI
import com.anadolstudio.homehub.feature.common.data.PreferencesStorage
import com.anadolstudio.utils.timber.PrettyLoggingTree
import javax.inject.Inject
import timber.log.Timber

class App : Application(), ImageLoaderFactory {

    @Inject
    lateinit var preferences: PreferencesStorage

    override fun onCreate() {
        super.onCreate()

        DI.init(this)
        DI.appComponent.inject(this)
        AppCompatDelegate.setDefaultNightMode(preferences.nightMode)

        Timber.plant(PrettyLoggingTree(this, getString(R.string.app_name)))
    }

    override fun newImageLoader(): ImageLoader = ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                        .maxSizePercent(IMAGE_MEMORY_CACHE_PERCENT)
                        .build()
            }
            .diskCache {
                DiskCache.Builder()
                        .directory(cacheDir.resolve(IMAGE_DISK_CACHE_DIR))
                        .maxSizePercent(IMAGE_DISK_CACHE_PERCENT)
                        .build()
            }
            .respectCacheHeaders(false)
            .allowRgb565(true)
            .build()

    private companion object {
        const val IMAGE_DISK_CACHE_DIR = "image_cache"
        const val IMAGE_MEMORY_CACHE_PERCENT = 0.25
        const val IMAGE_DISK_CACHE_PERCENT = 0.02
    }
}
