package com.magic.music.stream.player

import android.content.Context
import android.net.Uri
import android.os.CountDownTimer
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.magic.music.stream.data.model.RadioStation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class RadioPlayerController(private val context: Context) {

    private val _currentStation = MutableStateFlow<RadioStation?>(null)
    val currentStation: StateFlow<RadioStation?> = _currentStation.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _isBuffering = MutableStateFlow(false)
    val isBuffering: StateFlow<Boolean> = _isBuffering.asStateFlow()

    private val _liveTitle = MutableStateFlow("")
    val liveTitle: StateFlow<String> = _liveTitle.asStateFlow()

    private val _stationQueue = MutableStateFlow<List<RadioStation>>(emptyList())
    val stationQueue: StateFlow<List<RadioStation>> = _stationQueue.asStateFlow()

    private val _currentIndex = MutableStateFlow(-1)
    val currentIndex: StateFlow<Int> = _currentIndex.asStateFlow()

    private val _sleepTimerMinutes = MutableStateFlow(0)
    val sleepTimerMinutes: StateFlow<Int> = _sleepTimerMinutes.asStateFlow()

    private val _playbackError = MutableStateFlow<String?>(null)
    val playbackError: StateFlow<String?> = _playbackError.asStateFlow()

    private var countDownTimer: CountDownTimer? = null
    private var retryCount = 0

    private val exoPlayer: ExoPlayer = ExoPlayer.Builder(context)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                .setUsage(C.USAGE_MEDIA)
                .build(),
            true // handleAudioFocus
        )
        .setWakeMode(C.WAKE_MODE_NETWORK)
        .build()

    init {
        PlaybackService.radioControllerCallback = this

        exoPlayer.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_BUFFERING -> _isBuffering.value = true
                    Player.STATE_READY -> {
                        _isBuffering.value = false
                        _isPlaying.value = exoPlayer.playWhenReady
                        retryCount = 0
                    }
                    Player.STATE_ENDED, Player.STATE_IDLE -> {
                        _isBuffering.value = false
                        _isPlaying.value = false
                    }
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.value = isPlaying
                _currentStation.value?.let { station ->
                    PlaybackService.startOrUpdate(context, station, isPlaying, _liveTitle.value)
                }
            }

            override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
                val title = mediaMetadata.title?.toString()
                val artist = mediaMetadata.artist?.toString()
                val live = if (!title.isNullOrBlank() && !artist.isNullOrBlank()) {
                    "$artist - $title"
                } else if (!title.isNullOrBlank()) {
                    title
                } else {
                    ""
                }
                _liveTitle.value = live
                _currentStation.value?.let { station ->
                    PlaybackService.startOrUpdate(context, station, _isPlaying.value, live)
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                _isBuffering.value = false
                _isPlaying.value = false
                error.printStackTrace()

                // Prevent infinite prepare loop on dead/offline streams
                if (retryCount < 1 && error.errorCode == PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED) {
                    retryCount++
                    exoPlayer.prepare()
                } else {
                    retryCount = 0
                    _playbackError.value = "Radio station is offline or stream cannot be loaded."
                }
            }
        })
    }

    fun playStation(station: RadioStation, newQueue: List<RadioStation>? = null) {
        retryCount = 0
        _playbackError.value = null

        if (newQueue != null) {
            _stationQueue.value = newQueue
            _currentIndex.value = newQueue.indexOfFirst { it.id == station.id }.coerceAtLeast(0)
        }

        _currentStation.value = station
        _liveTitle.value = ""
        _isBuffering.value = true
        _isPlaying.value = true

        try {
            val artworkUri = if (station.logoUrl.isNotBlank()) {
                try { Uri.parse(station.logoUrl) } catch (e: Exception) { null }
            } else null

            val mediaMetadata = MediaMetadata.Builder()
                .setTitle(station.name)
                .setArtist(station.cleanTags.ifBlank { station.country })
                .setArtworkUri(artworkUri)
                .build()

            val mediaItem = MediaItem.Builder()
                .setUri(station.streamUrl)
                .setMediaMetadata(mediaMetadata)
                .build()

            exoPlayer.stop()
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            exoPlayer.playWhenReady = true

            PlaybackService.startOrUpdate(context, station, isPlaying = true, "")
        } catch (e: Exception) {
            e.printStackTrace()
            _isBuffering.value = false
            _isPlaying.value = false
            _playbackError.value = "Failed to play station: ${e.message}"
        }
    }

    fun clearError() {
        _playbackError.value = null
    }

    fun togglePlayPause() {
        if (_isPlaying.value) {
            exoPlayer.pause()
            _isPlaying.value = false
            _currentStation.value?.let { station ->
                PlaybackService.startOrUpdate(context, station, isPlaying = false, _liveTitle.value)
            }
        } else {
            if (_currentStation.value != null) {
                exoPlayer.play()
                _isPlaying.value = true
                _currentStation.value?.let { station ->
                    PlaybackService.startOrUpdate(context, station, isPlaying = true, _liveTitle.value)
                }
            } else if (_stationQueue.value.isNotEmpty()) {
                playStation(_stationQueue.value[0])
            }
        }
    }

    fun playNext() {
        val q = _stationQueue.value
        if (q.isEmpty()) return
        val nextIdx = (_currentIndex.value + 1) % q.size
        _currentIndex.value = nextIdx
        playStation(q[nextIdx])
    }

    fun playPrevious() {
        val q = _stationQueue.value
        if (q.isEmpty()) return
        val prevIdx = if (_currentIndex.value - 1 < 0) q.size - 1 else _currentIndex.value - 1
        _currentIndex.value = prevIdx
        playStation(q[prevIdx])
    }

    fun setSleepTimer(minutes: Int) {
        countDownTimer?.cancel()
        _sleepTimerMinutes.value = minutes
        if (minutes > 0) {
            countDownTimer = object : CountDownTimer(minutes * 60 * 1000L, 1000L) {
                override fun onTick(millisUntilFinished: Long) {
                    _sleepTimerMinutes.value = (millisUntilFinished / 60000).toInt() + 1
                }
                override fun onFinish() {
                    _sleepTimerMinutes.value = 0
                    if (_isPlaying.value) {
                        togglePlayPause()
                    }
                }
            }.start()
        }
    }

    fun release() {
        countDownTimer?.cancel()
        PlaybackService.stop(context)
        if (PlaybackService.radioControllerCallback == this) {
            PlaybackService.radioControllerCallback = null
        }
        exoPlayer.release()
    }
}
