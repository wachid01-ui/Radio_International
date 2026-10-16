package com.magic.music.stream.ads

/**
 * Google AdMob Ad Configuration
 *
 * IMPORTANT NOTE:
 * The application currently uses official Google AdMob Test Ad Unit IDs.
 * Test Ads are 100% safe to test repeatedly without risk of penalty.
 *
 * WHEN READY FOR RELEASE WITH REAL ACCOUNT:
 * Simply replace the ID strings below with the IDs from your AdMob dashboard.
 */
object AdConfig {
    // Official Google Test App ID: ca-app-pub-3940256099942544~3347511713 (stored in AndroidManifest.xml)

    // Test Adaptive & Standard Banner Ad Unit ID
    const val BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"

    // Test Interstitial Ad Unit ID
    const val INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"

    // Safe & Optimal Interstitial Frequency Rules:
    // 1. Shows every time the user switches stations STATIONS_PER_AD times
    const val STATIONS_PER_AD = 4

    // 2. Minimum interval between interstitial ads (150 seconds = 2.5 minutes)
    // Prevents user disruption if they switch many stations in a short time
    const val MIN_INTERVAL_BETWEEN_ADS_MS = 150_000L
}
