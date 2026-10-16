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
 * Manages GDPR privacy consent requests (Europe/UK)
 * automatically in compliance with official Google IAB TCF v2.2 guidelines.
 */
class GoogleMobileAdsConsentManager private constructor(context: Context) {

    private val consentInformation: ConsentInformation =
        UserMessagingPlatform.getConsentInformation(context)

    /**
     * Checks whether ads can be requested (canRequestAds)
     */
    val canRequestAds: Boolean
        get() = consentInformation.canRequestAds()

    /**
     * Checks whether privacy options are required to be displayed in the menu
     */
    val isPrivacyOptionsRequired: Boolean
        get() = consentInformation.privacyOptionsRequirementStatus ==
                ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED

    /**
     * Requests consent information update and displays the GDPR form if required.
     * If the user is located outside EEA/UK, the form will not appear
     * and the onConsentGatheringComplete callback is triggered immediately.
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
     * Re-displays the privacy options form if the user wishes to modify consent later.
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
