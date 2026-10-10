package com.magic.music.stream

import android.app.Application
import com.google.android.gms.ads.MobileAds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MagicMusicApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // Inisialisasi Google Mobile Ads SDK di background thread
        CoroutineScope(Dispatchers.IO).launch {
            try {
                MobileAds.initialize(this@MagicMusicApp) {}
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
