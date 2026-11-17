package com.qiaoyutian.musicplayer

import android.net.Uri
import java.util.Locale

data class Song(
    val id: Long,
    val title: String,
    val artist: String,
    val duration: Long,
    val uri: Uri
)

object TimeFormat {

    fun mmss(millis: Long): String {
        if (millis <= 0) return "00:00"
        val totalSeconds = millis / 1000
        val hour = totalSeconds / 3600
        val minute = (totalSeconds % 3600) / 60
        val second = totalSeconds % 60
        return if (hour > 0) {
            String.format(Locale.US, "%d:%02d:%02d", hour, minute, second)
        } else {
            String.format(Locale.US, "%02d:%02d", minute, second)
        }
    }
}
