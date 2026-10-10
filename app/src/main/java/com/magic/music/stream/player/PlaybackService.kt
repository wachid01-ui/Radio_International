package com.magic.music.stream.player

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.wifi.WifiManager
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.magic.music.stream.data.model.Track
import com.magic.music.stream.ui.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PlaybackService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var wakeLock: PowerManager.WakeLock? = null
    private var wifiLock: WifiManager.WifiLock? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        acquireLocks()
        instance = this
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        when (action) {
            ACTION_PLAY -> playerControllerCallback?.togglePlayPause()
            ACTION_PAUSE -> playerControllerCallback?.togglePlayPause()
            ACTION_NEXT -> playerControllerCallback?.playNext()
            ACTION_PREV -> playerControllerCallback?.playPrevious()
            ACTION_STOP -> {
                playerControllerCallback?.togglePlayPause()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
            ACTION_UPDATE -> {
                val title = intent.getStringExtra(EXTRA_TITLE) ?: "Magic Music"
                val artist = intent.getStringExtra(EXTRA_ARTIST) ?: "Streaming"
                val isPlaying = intent.getBooleanExtra(EXTRA_IS_PLAYING, true)
                val artworkUrl = intent.getStringExtra(EXTRA_ARTWORK) ?: ""

                // 1. Immediately post foreground notification to satisfy Android 8+ foreground service requirements
                val initialNotification = buildNotification(title, artist, isPlaying, null)
                startForeground(NOTIFICATION_ID, initialNotification)

                // 2. Asynchronously load high-res album art and update notification
                updateNotificationWithArtwork(title, artist, isPlaying, artworkUrl)
            }
        }
        return START_STICKY
    }

    private fun acquireLocks() {
        if (wakeLock == null) {
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "MagicMusicStream:WakeLock"
            ).apply {
                setReferenceCounted(false)
                acquire(24 * 60 * 60 * 1000L) // 24 hours
            }
        }

        if (wifiLock == null) {
            try {
                val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
                wifiLock = wifiManager.createWifiLock(
                    WifiManager.WIFI_MODE_FULL_HIGH_PERF,
                    "MagicMusicStream:WifiLock"
                ).apply {
                    setReferenceCounted(false)
                    acquire()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun releaseLocks() {
        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
            wakeLock = null
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            if (wifiLock?.isHeld == true) {
                wifiLock?.release()
            }
            wifiLock = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updateNotificationWithArtwork(title: String, artist: String, isPlaying: Boolean, artworkUrl: String) {
        if (artworkUrl.isBlank()) return
        serviceScope.launch {
            val bitmap = loadBitmap(artworkUrl)
            if (bitmap != null) {
                val notification = buildNotification(title, artist, isPlaying, bitmap)
                val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                manager.notify(NOTIFICATION_ID, notification)
            }
        }
    }

    private suspend fun loadBitmap(url: String): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val loader = ImageLoader(this@PlaybackService)
            val request = ImageRequest.Builder(this@PlaybackService)
                .data(url)
                .allowHardware(false)
                .build()
            val result = (loader.execute(request) as? SuccessResult)?.drawable
            (result as? BitmapDrawable)?.bitmap
        } catch (e: Exception) {
            null
        }
    }

    private fun buildNotification(
        title: String,
        artist: String,
        isPlaying: Boolean,
        largeIcon: Bitmap?
    ): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val prevPendingIntent = PendingIntent.getService(
            this,
            1,
            Intent(this, PlaybackService::class.java).apply { action = ACTION_PREV },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val playPausePendingIntent = PendingIntent.getService(
            this,
            2,
            Intent(this, PlaybackService::class.java).apply {
                action = if (isPlaying) ACTION_PAUSE else ACTION_PLAY
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val nextPendingIntent = PendingIntent.getService(
            this,
            3,
            Intent(this, PlaybackService::class.java).apply { action = ACTION_NEXT },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentTitle(title)
            .setContentText(artist)
            .setContentIntent(openAppPendingIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(isPlaying)
            .setOnlyAlertOnce(true)
            .addAction(android.R.drawable.ic_media_previous, "Previous", prevPendingIntent)
            .addAction(
                if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play,
                if (isPlaying) "Pause" else "Play",
                playPausePendingIntent
            )
            .addAction(android.R.drawable.ic_media_next, "Next", nextPendingIntent)
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setShowActionsInCompactView(0, 1, 2)
            )

        if (largeIcon != null) {
            builder.setLargeIcon(largeIcon)
        }

        return builder.build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Magic Music Playback",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Background playback control and lock screen widget"
                setShowBadge(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        releaseLocks()
        instance = null
        super.onDestroy()
    }

    companion object {
        const val CHANNEL_ID = "magic_music_playback_channel"
        const val NOTIFICATION_ID = 1010

        const val ACTION_PLAY = "com.magic.music.ACTION_PLAY"
        const val ACTION_PAUSE = "com.magic.music.ACTION_PAUSE"
        const val ACTION_NEXT = "com.magic.music.ACTION_NEXT"
        const val ACTION_PREV = "com.magic.music.ACTION_PREV"
        const val ACTION_STOP = "com.magic.music.ACTION_STOP"
        const val ACTION_UPDATE = "com.magic.music.ACTION_UPDATE"

        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_ARTIST = "extra_artist"
        const val EXTRA_IS_PLAYING = "extra_is_playing"
        const val EXTRA_ARTWORK = "extra_artwork"

        var instance: PlaybackService? = null
        var playerControllerCallback: StreamPlayerController? = null

        fun startOrUpdate(context: Context, track: Track, isPlaying: Boolean) {
            val intent = Intent(context, PlaybackService::class.java).apply {
                action = ACTION_UPDATE
                putExtra(EXTRA_TITLE, track.title)
                putExtra(EXTRA_ARTIST, track.artist)
                putExtra(EXTRA_IS_PLAYING, isPlaying)
                putExtra(EXTRA_ARTWORK, track.thumbnailUrl)
            }
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, PlaybackService::class.java).apply {
                action = ACTION_STOP
            }
            try {
                context.startService(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
