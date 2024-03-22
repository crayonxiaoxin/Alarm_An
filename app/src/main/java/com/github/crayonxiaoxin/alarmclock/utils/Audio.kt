package com.github.crayonxiaoxin.alarmclock.utils

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

object AudioManager {
    private val mediaPlayer by lazy { MediaPlayer() }

    private var job: Job? = null

    fun playMp3FromUri(context: Context, uri: Uri, isLoop: Boolean = false) {
        job?.cancel()
        context.grantUriPermission(context.packageName, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        job = CoroutineScope(Dispatchers.IO).launch {
            mediaPlayer.apply {
                reset()
                setDataSource(context, uri)
                prepareAsync()
                isLooping = isLoop
                setOnPreparedListener {
                    it.start()
                }
            }
        }
    }

    fun stopMp3() {
        job?.cancel()
        if (mediaPlayer.isPlaying) {
            mediaPlayer.stop()
        }
    }

    fun release() {
        stopMp3()
        mediaPlayer?.release()
    }
}

