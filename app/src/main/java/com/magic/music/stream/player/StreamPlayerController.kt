package com.magic.music.stream.player

import android.content.Context
import com.magic.music.stream.data.model.Track
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class StreamPlayerController(private val context: Context) {

    private val _currentTrack = MutableStateFlow<Track?>(null)
    val currentTrack: StateFlow<Track?> = _currentTrack.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()

    private val _queue = MutableStateFlow<List<Track>>(emptyList())
    val queue: StateFlow<List<Track>> = _queue.asStateFlow()

    private val _currentIndex = MutableStateFlow(-1)
    val currentIndex: StateFlow<Int> = _currentIndex.asStateFlow()

    private val _isRepeat = MutableStateFlow(true)
    val isRepeat: StateFlow<Boolean> = _isRepeat.asStateFlow()

    private val _isShuffle = MutableStateFlow(false)
    val isShuffle: StateFlow<Boolean> = _isShuffle.asStateFlow()

    init {
        PlaybackService.playerControllerCallback = this
    }

    val engine = StreamPlayerEngine(
        context = context,
        onStateChanged = { playing ->
            _isPlaying.value = playing
            _currentTrack.value?.let { track ->
                PlaybackService.startOrUpdate(context, track, playing)
            }
        },
        onProgress = { curr, total ->
            _currentPosition.value = curr
            if (total > 0) {
                _duration.value = total
            }
        },
        onEnded = {
            handleTrackEnded()
        },
        onError = { err ->
            _isPlaying.value = false
            _currentTrack.value?.let { track ->
                PlaybackService.startOrUpdate(context, track, false)
            }
        }
    )

    fun playTrack(track: Track, newQueue: List<Track>? = null) {
        if (newQueue != null) {
            _queue.value = newQueue
            _currentIndex.value = newQueue.indexOfFirst { it.id == track.id }.coerceAtLeast(0)
        } else if (_queue.value.isEmpty() || !_queue.value.any { it.id == track.id }) {
            _queue.value = listOf(track)
            _currentIndex.value = 0
        } else {
            _currentIndex.value = _queue.value.indexOfFirst { it.id == track.id }
        }

        _currentTrack.value = track
        _currentPosition.value = 0L
        _duration.value = track.durationMs
        _isPlaying.value = true

        engine.play(track.videoId)
        PlaybackService.startOrUpdate(context, track, isPlaying = true)
    }

    fun togglePlayPause() {
        if (_isPlaying.value) {
            engine.pause()
            _isPlaying.value = false
            _currentTrack.value?.let { track ->
                PlaybackService.startOrUpdate(context, track, isPlaying = false)
            }
        } else {
            if (_currentTrack.value != null) {
                engine.resume()
                _isPlaying.value = true
                _currentTrack.value?.let { track ->
                    PlaybackService.startOrUpdate(context, track, isPlaying = true)
                }
            } else if (_queue.value.isNotEmpty()) {
                playTrack(_queue.value[0])
            }
        }
    }

    fun playNext() {
        val q = _queue.value
        if (q.isEmpty()) return
        val nextIdx = if (_isShuffle.value) {
            (0 until q.size).random()
        } else {
            (_currentIndex.value + 1) % q.size
        }
        _currentIndex.value = nextIdx
        playTrack(q[nextIdx])
    }

    fun playPrevious() {
        val q = _queue.value
        if (q.isEmpty()) return
        val prevIdx = if (_currentIndex.value - 1 < 0) q.size - 1 else _currentIndex.value - 1
        _currentIndex.value = prevIdx
        playTrack(q[prevIdx])
    }

    fun seekTo(positionMs: Long) {
        _currentPosition.value = positionMs
        engine.seekTo(positionMs)
    }

    fun toggleRepeat() {
        _isRepeat.value = !_isRepeat.value
    }

    fun toggleShuffle() {
        _isShuffle.value = !_isShuffle.value
    }

    private fun handleTrackEnded() {
        if (_isRepeat.value && _queue.value.size <= 1) {
            // Replay single track
            _currentTrack.value?.let { track ->
                engine.play(track.videoId)
                PlaybackService.startOrUpdate(context, track, isPlaying = true)
            }
        } else {
            playNext()
        }
    }

    fun release() {
        PlaybackService.stop(context)
        if (PlaybackService.playerControllerCallback == this) {
            PlaybackService.playerControllerCallback = null
        }
        engine.release()
    }
}
