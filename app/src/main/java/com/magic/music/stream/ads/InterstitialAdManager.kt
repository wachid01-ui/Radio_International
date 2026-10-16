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

    private val consentManager = GoogleMobileAdsConsentManager.getInstance(context)
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
     * Called whenever the user selects a radio station.
     * Regulates safe and non-intrusive ad appearance frequency:
     * - Appears every 4 station switches
     * - Minimum 2.5 minutes interval between ads
     *
     * @param activity Context Activity for presenting the full-screen ad dialog
     * @param onAdBeforeShow Called before ad presentation (e.g. to pause live radio streaming)
     * @param onProceedAction Called after ad is dismissed (or if ad is not ready) to resume/play radio
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
            // Not time for an ad yet, proceed with playing the radio station directly
            onProceedAction()
            if (interstitialAd == null && !isLoading) {
                loadAd()
            }
        }
    }
}
