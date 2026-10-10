package com.magic.music.stream.ads

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.magic.music.stream.ui.theme.BackgroundDark

@Composable
fun AdaptiveBannerAd(
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp

    Box(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(BackgroundDark),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            modifier = Modifier.fillMaxWidth(),
            factory = { context ->
                AdView(context).apply {
                    adUnitId = AdConfig.BANNER_AD_UNIT_ID
                    val adSize = AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, screenWidthDp)
                    setAdSize(adSize)
                    adListener = object : AdListener() {
                        override fun onAdLoaded() {
                            Log.d("AdMobBanner", "Adaptive Banner Loaded successfully!")
                        }
                        override fun onAdFailedToLoad(error: LoadAdError) {
                            Log.e("AdMobBanner", "Adaptive Banner Failed to load: ${error.message} (code: ${error.code})")
                        }
                    }
                    loadAd(AdRequest.Builder().build())
                }
            }
        )
    }
}
