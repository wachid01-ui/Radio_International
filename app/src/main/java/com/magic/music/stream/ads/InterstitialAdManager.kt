package com.magic.music.stream.ads

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

class InterstitialAdManager(private val context: Context) {

    private var interstitialAd: InterstitialAd? = null
    private var isLoading = false
    private var switchStationCount = 0
    private var lastAdShownTimestamp = 0L

    init {
        loadAd()
    }

    fun loadAd() {
        if (isLoading || interstitialAd != null) return
        isLoading = true

        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            context,
            AdConfig.INTERSTITIAL_AD_UNIT_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    isLoading = false
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    interstitialAd = null
                    isLoading = false
                }
            }
        )
    }

    /**
     * Dipanggil setiap kali pengguna memilih stasiun radio.
     * Mengatur frekuensi kemunculan iklan yang aman dan tidak mengganggu:
     * - Muncul setiap 4 kali ganti stasiun
     * - Jeda minimal 2.5 menit antar iklan
     *
     * @param activity Context Activity untuk menampilkan dialog iklan layar penuh
     * @param onAdBeforeShow Dipanggil sebelum iklan tayang (untuk mem-pause streaming radio)
     * @param onProceedAction Dipanggil setelah iklan ditutup (atau jika iklan belum waktunya tayang) untuk memutar radio
     */
    fun showIfReady(
        activity: Activity,
        onAdBeforeShow: () -> Unit = {},
        onProceedAction: () -> Unit
    ) {
        switchStationCount++
        val currentTime = System.currentTimeMillis()
        val timeSinceLastAd = currentTime - lastAdShownTimestamp

        val isCountReady = switchStationCount >= AdConfig.STATIONS_PER_AD
        val isIntervalPassed = timeSinceLastAd >= AdConfig.MIN_INTERVAL_BETWEEN_ADS_MS
        val isAdAvailable = interstitialAd != null

        if (isCountReady && isIntervalPassed && isAdAvailable) {
            val ad = interstitialAd
            ad?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdShowedFullScreenContent() {
                    onAdBeforeShow()
                }

                override fun onAdDismissedFullScreenContent() {
                    interstitialAd = null
                    lastAdShownTimestamp = System.currentTimeMillis()
                    switchStationCount = 0
                    loadAd()
                    onProceedAction()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    interstitialAd = null
                    loadAd()
                    onProceedAction()
                }
            }
            ad?.show(activity)
        } else {
            // Belum waktunya iklan tayang, lanjutkan putar stasiun radio langsung
            onProceedAction()
            if (interstitialAd == null && !isLoading) {
                loadAd()
            }
        }
    }
}
