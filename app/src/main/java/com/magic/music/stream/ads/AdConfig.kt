package com.magic.music.stream.ads

/**
 * Konfigurasi Iklan Google AdMob
 *
 * CATATAN PENTING:
 * Saat ini aplikasi menggunakan Test Ad Unit ID resmi dari Google AdMob.
 * Iklan uji coba (Test Ads) aman 100% untuk dicoba berkali-kali tanpa risiko penalti.
 *
 * KETIKA SUDAH SIAP RILIS DENGAN AKUN ASLI:
 * Cukup ganti nilai string ID di bawah ini dengan ID dari dashboard AdMob Anda.
 */
object AdConfig {
    // Official Google Test App ID: ca-app-pub-3940256099942544~3347511713 (disimpan di AndroidManifest.xml)

    // Test Adaptive & Standard Banner Ad Unit ID
    const val BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"

    // Test Interstitial Ad Unit ID
    const val INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"

    // Aturan Frekuensi Interstitial yang Aman & Optimal:
    // 1. Muncul setiap kali user berganti stasiun sebanyak STATIONS_PER_AD kali
    const val STATIONS_PER_AD = 4

    // 2. Batas waktu minimal antar iklan interstitial (150 detik = 2.5 menit)
    // Mencegah user terganggu jika mereka mengganti banyak stasiun dalam waktu singkat
    const val MIN_INTERVAL_BETWEEN_ADS_MS = 150_000L
}
