package com.magic.music.stream.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import com.google.android.gms.ads.MobileAds
import com.magic.music.stream.ads.GoogleMobileAdsConsentManager
import com.magic.music.stream.ui.screens.SingleRadioScreen
import com.magic.music.stream.ui.theme.MagicMusicTheme
import com.magic.music.stream.ui.viewmodel.RadioViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

class MainActivity : ComponentActivity() {

    private val viewModel: RadioViewModel by viewModels()
    private val isMobileAdsInitialized = AtomicBoolean(false)
    private lateinit var consentManager: GoogleMobileAdsConsentManager

    private val requestNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->
        // Permission result handled
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        requestNotificationPermission()

        // Inisialisasi UMP Consent Manager (GDPR)
        consentManager = GoogleMobileAdsConsentManager.getInstance(this)
        consentManager.gatherConsent(this) { consentError ->
            if (consentError != null) {
                android.util.Log.w("UMP", "Consent error: ${consentError.message}")
            }
            if (consentManager.canRequestAds) {
                initializeMobileAdsSdk()
            }
        }

        if (consentManager.canRequestAds) {
            initializeMobileAdsSdk()
        }

        setContent {
            MagicMusicTheme {
                SingleRadioScreen(viewModel = viewModel)
            }
        }
    }

    private fun initializeMobileAdsSdk() {
        if (isMobileAdsInitialized.getAndSet(true)) return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                MobileAds.initialize(this@MainActivity) {}
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
