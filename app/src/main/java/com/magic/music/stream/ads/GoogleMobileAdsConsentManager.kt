package com.magic.music.stream.ads

import android.app.Activity
import android.content.Context
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.FormError
import com.google.android.ump.UserMessagingPlatform

/**
 * Google Mobile Ads Consent Manager (UMP SDK)
 *
 * Mengelola permintaan persetujuan privasi GDPR (Eropa/UK)
 * secara otomatis sesuai pedoman resmi Google IAB TCF v2.2.
 */
class GoogleMobileAdsConsentManager private constructor(context: Context) {

    private val consentInformation: ConsentInformation =
        UserMessagingPlatform.getConsentInformation(context)

    /**
     * Mengecek apakah iklan sudah diizinkan untuk diminta (canRequestAds)
     */
    val canRequestAds: Boolean
        get() = consentInformation.canRequestAds()

    /**
     * Mengecek apakah opsi privasi (Privacy Options) diperlukan untuk ditampilkan di menu
     */
    val isPrivacyOptionsRequired: Boolean
        get() = consentInformation.privacyOptionsRequirementStatus ==
                ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED

    /**
     * Meminta pembaruan info persetujuan dan menampilkan formulir GDPR jika diperlukan.
     * Jika pengguna berada di luar EEA/UK (misal: Indonesia), formulir tidak akan muncul
     * dan callback onConsentGatheringComplete langsung dipanggil seketika.
     */
    fun gatherConsent(
        activity: Activity,
        onConsentGatheringComplete: (FormError?) -> Unit
    ) {
        val params = ConsentRequestParameters.Builder()
            .setTagForUnderAgeOfConsent(false)
            .build()

        consentInformation.requestConsentInfoUpdate(
            activity,
            params,
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(
                    activity
                ) { formError ->
                    onConsentGatheringComplete(formError)
                }
            },
            { requestConsentError ->
                onConsentGatheringComplete(requestConsentError)
            }
        )
    }

    /**
     * Menampilkan kembali formulir opsi privasi jika pengguna ingin mengubah persetujuan mereka nanti.
     */
    fun showPrivacyOptionsForm(
        activity: Activity,
        onConsentFormDismissedListener: (FormError?) -> Unit
    ) {
        UserMessagingPlatform.showPrivacyOptionsForm(
            activity,
            onConsentFormDismissedListener
        )
    }

    companion object {
        @Volatile
        private var instance: GoogleMobileAdsConsentManager? = null

        fun getInstance(context: Context): GoogleMobileAdsConsentManager {
            return instance ?: synchronized(this) {
                instance ?: GoogleMobileAdsConsentManager(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }
}
