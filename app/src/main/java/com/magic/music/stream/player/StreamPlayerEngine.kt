package com.magic.music.stream.player

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient

class StreamPlayerEngine(
    private val context: Context,
    private val onStateChanged: (isPlaying: Boolean) -> Unit,
    private val onProgress: (currentPositionMs: Long, totalDurationMs: Long) -> Unit,
    private val onEnded: () -> Unit,
    private val onError: (String) -> Unit
) {
    private var webView: WebView? = null
    private val mainHandler = Handler(Looper.getMainLooper())
    private var isReady = false
    private var currentVideoId: String? = null
    private var isUserPaused = false

    init {
        mainHandler.post {
            initWebView()
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebView() {
        try {
            val appCtx = context.applicationContext
            webView = WebView(appCtx).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.databaseEnabled = true
                settings.mediaPlaybackRequiresUserGesture = false
                settings.cacheMode = WebSettings.LOAD_DEFAULT
                settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                settings.userAgentString = "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Mobile Safari/537.36"

                CookieManager.getInstance().setAcceptCookie(true)
                try {
                    CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                webChromeClient = WebChromeClient()
                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                        return false
                    }
                }

                addJavascriptInterface(WebAppInterface(), "AndroidBridge")
            }

            loadPlayerHtml()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadPlayerHtml() {
        val html = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>
                    html, body { margin:0; padding:0; background-color:#000; overflow:hidden; width:100%; height:100%; }
                    #player { width:100%; height:100%; }
                </style>
                <script>
                    // Background & Screen-Off Protection: Spoof Document Visibility
                    Object.defineProperty(document, 'hidden', { get: function() { return false; }, configurable: true });
                    Object.defineProperty(document, 'visibilityState', { get: function() { return 'visible'; }, configurable: true });
                    Object.defineProperty(document, 'webkitVisibilityState', { get: function() { return 'visible'; }, configurable: true });
                    window.addEventListener('visibilitychange', function(e) { e.stopImmediatePropagation(); }, true);
                    document.addEventListener('visibilitychange', function(e) { e.stopImmediatePropagation(); }, true);
                    window.addEventListener('blur', function(e) { e.stopImmediatePropagation(); }, true);
                    window.addEventListener('pagehide', function(e) { e.stopImmediatePropagation(); }, true);
                </script>
            </head>
            <body>
                <div id="player"></div>
                <script>
                    var tag = document.createElement('script');
                    tag.src = "https://www.youtube.com/iframe_api";
                    var firstScriptTag = document.getElementsByTagName('script')[0];
                    firstScriptTag.parentNode.insertBefore(tag, firstScriptTag);

                    var player = null;
                    var isPlayerReady = false;
                    var pendingVid = null;
                    var progressInterval = null;
                    var userPaused = false;

                    function onYouTubeIframeAPIReady() {
                        player = new YT.Player('player', {
                            height: '100%',
                            width: '100%',
                            playerVars: {
                                'autoplay': 1,
                                'playsinline': 1,
                                'controls': 0,
                                'rel': 0,
                                'origin': 'https://www.youtube-nocookie.com',
                                'enablejsapi': 1,
                                'loop': 1,
                                'fs': 0,
                                'modestbranding': 1
                            },
                            events: {
                                'onReady': onPlayerReady,
                                'onStateChange': onPlayerStateChange,
                                'onError': onPlayerError
                            }
                        });
                    }

                    function onPlayerReady(event) {
                        isPlayerReady = true;
                        try {
                            player.unMute();
                            player.setVolume(100);
                        } catch(e){}
                        if (window.AndroidBridge) {
                            window.AndroidBridge.postReady();
                        }
                        if (pendingVid) {
                            var v = pendingVid;
                            pendingVid = null;
                            window.loadAndPlay(v);
                        }
                    }

                    function startTimer() {
                        clearInterval(progressInterval);
                        progressInterval = setInterval(function() {
                            if (player && typeof player.getCurrentTime === 'function' && typeof player.getDuration === 'function') {
                                var curr = player.getCurrentTime() || 0;
                                var dur = player.getDuration() || 0;
                                if (dur > 0 && window.AndroidBridge) {
                                    window.AndroidBridge.postProgress(curr, dur);
                                }
                            }
                        }, 400);
                    }

                    function onPlayerStateChange(event) {
                        // 0: ENDED, 1: PLAYING, 2: PAUSED, 3: BUFFERING
                        if (event.data === 0) {
                            if (window.AndroidBridge) window.AndroidBridge.postEnded();
                        } else if (event.data === 1) {
                            startTimer();
                            try {
                                player.unMute();
                                player.setVolume(100);
                            } catch(e){}
                            if (window.AndroidBridge) window.AndroidBridge.postState(true);
                        } else if (event.data === 2) {
                            // If paused by OS background/screen-off rather than user, immediately resume
                            if (!userPaused) {
                                setTimeout(function() {
                                    if (!userPaused && player && typeof player.playVideo === 'function') {
                                        player.playVideo();
                                    }
                                }, 50);
                            } else {
                                if (window.AndroidBridge) window.AndroidBridge.postState(false);
                            }
                        }
                    }

                    function onPlayerError(event) {
                        if (window.AndroidBridge) {
                            window.AndroidBridge.postError("Error code: " + event.data);
                        }
                    }

                    window.loadAndPlay = function(videoId) {
                        userPaused = false;
                        if (isPlayerReady && player && typeof player.loadVideoById === 'function') {
                            player.loadVideoById({
                                videoId: videoId,
                                startSeconds: 0
                            });
                            player.unMute();
                            player.setVolume(100);
                            player.playVideo();
                            startTimer();
                        } else {
                            pendingVid = videoId;
                        }
                    };

                    window.appPlay = function() {
                        userPaused = false;
                        if (player && typeof player.playVideo === 'function') {
                            player.playVideo();
                        }
                    };

                    window.appPause = function() {
                        userPaused = true;
                        if (player && typeof player.pauseVideo === 'function') {
                            player.pauseVideo();
                        }
                    };

                    window.appSeek = function(seconds) {
                        if (player && typeof player.seekTo === 'function') {
                            player.seekTo(seconds, true);
                        }
                    };
                </script>
            </body>
            </html>
        """.trimIndent()

        webView?.loadDataWithBaseURL("https://www.youtube-nocookie.com", html, "text/html", "UTF-8", null)
    }

    fun play(videoId: String) {
        currentVideoId = videoId
        isUserPaused = false
        mainHandler.post {
            if (isReady && webView != null) {
                webView?.evaluateJavascript("window.loadAndPlay('$videoId');", null)
            } else {
                if (webView == null) {
                    initWebView()
                }
            }
        }
    }

    fun resume() {
        isUserPaused = false
        mainHandler.post {
            webView?.evaluateJavascript("window.appPlay();", null)
        }
    }

    fun pause() {
        isUserPaused = true
        mainHandler.post {
            webView?.evaluateJavascript("window.appPause();", null)
        }
    }

    fun seekTo(positionMs: Long) {
        mainHandler.post {
            val seconds = positionMs / 1000.0
            webView?.evaluateJavascript("window.appSeek($seconds);", null)
        }
    }

    fun release() {
        mainHandler.post {
            isReady = false
            currentVideoId = null
            webView?.destroy()
            webView = null
        }
    }

    private inner class WebAppInterface {
        @JavascriptInterface
        fun postReady() {
            mainHandler.post {
                isReady = true
                currentVideoId?.let { vid ->
                    play(vid)
                }
            }
        }

        @JavascriptInterface
        fun postState(isPlaying: Boolean) {
            mainHandler.post {
                onStateChanged(isPlaying)
            }
        }

        @JavascriptInterface
        fun postProgress(currentSec: Double, totalSec: Double) {
            val currMs = (currentSec * 1000).toLong()
            val totalMs = (totalSec * 1000).toLong()
            mainHandler.post {
                onProgress(currMs, totalMs)
            }
        }

        @JavascriptInterface
        fun postEnded() {
            mainHandler.post {
                onEnded()
            }
        }

        @JavascriptInterface
        fun postError(err: String) {
            Log.e("StreamPlayerEngine", "Player error: $err")
            mainHandler.post {
                onError(err)
            }
        }
    }
}
