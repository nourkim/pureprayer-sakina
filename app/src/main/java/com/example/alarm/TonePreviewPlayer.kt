package com.example.alarm

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import com.example.data.model.SakinaToneId

class TonePreviewPlayer {
    private var mediaPlayer: MediaPlayer? = null
    private var isStopped = false

    fun playOnce(context: Context, tone: SakinaToneId) {
        playRepeated(context, tone, 1)
    }

    fun playRepeated(context: Context, tone: SakinaToneId, count: Int) {
        stop()
        isStopped = false
        val total = count.coerceIn(1, 3)
        var remaining = total

        fun playNext() {
            if (isStopped || remaining <= 0) {
                stop()
                return
            }
            remaining--
            try {
                val uri = Uri.parse("android.resource://${context.packageName}/${tone.rawResId}")
                val mp = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    setDataSource(context, uri)
                    isLooping = false
                    setOnCompletionListener { player ->
                        player.release()
                        if (mediaPlayer === player) {
                            mediaPlayer = null
                            if (!isStopped && remaining > 0) {
                                playNext()
                            }
                        }
                    }
                    setOnErrorListener { player, _, _ ->
                        player.release()
                        if (mediaPlayer === player) {
                            mediaPlayer = null
                        }
                        true
                    }
                    prepare()
                    start()
                }
                mediaPlayer = mp
            } catch (_: Exception) {
                stop()
            }
        }

        playNext()
    }

    fun stop() {
        isStopped = true
        runCatching {
            mediaPlayer?.let { mp ->
                if (mp.isPlaying) mp.stop()
                mp.release()
            }
        }
        mediaPlayer = null
    }
}
