package com.example.affirmations

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.provider.MediaStore
import android.util.Log

data class Song(
    val id: Long,
    val title: String,
    val artist: String,
    val data: String
)

class MusicService : Service() {

    companion object {
        const val CHANNEL_ID = "music_channel"
        const val NOTIFICATION_ID = 1
    }

    private var mediaPlayer: MediaPlayer? = null
    private val songList = mutableListOf<Song>()
    private var currentSongIndex = 0
    private var isPaused = false

    private val binder = MusicBinder()

    inner class MusicBinder : Binder() {
        fun getService(): MusicService = this@MusicService
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        loadSongs()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "音乐播放",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "音乐播放通知栏"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun loadSongs() {
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DATA
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        contentResolver.query(collection, projection, selection, null, sortOrder)?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val dataCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val title = cursor.getString(titleCol) ?: "未知歌曲"
                val artist = cursor.getString(artistCol) ?: "未知艺术家"
                val data = cursor.getString(dataCol) ?: ""
                songList.add(Song(id, title, artist, data))
            }
        }
        Log.d("MusicService", "Loaded ${songList.size} songs")
    }

    fun getSongList(): List<Song> = songList.toList()

    fun playSong(index: Int) {
        if (index < 0 || index >= songList.size) return
        currentSongIndex = index
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer().apply {
            setDataSource(songList[index].data)
            prepare()
            setOnCompletionListener { nextSong() }
            start()
        }
        isPaused = false
        showNotification(songList[index].title)
    }

    fun play() {
        if (mediaPlayer == null && songList.isNotEmpty()) {
            playSong(currentSongIndex)
        } else if (isPaused) {
            mediaPlayer?.start()
            isPaused = false
            showNotification(songList[currentSongIndex].title)
        }
    }

    fun pause() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
                isPaused = true
            }
        }
    }

    fun stop() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        isPaused = false
        cancelNotification()
    }

    fun nextSong() {
        if (songList.isEmpty()) return
        currentSongIndex = (currentSongIndex + 1) % songList.size
        playSong(currentSongIndex)
    }

    fun prevSong() {
        if (songList.isEmpty()) return
        currentSongIndex = if (currentSongIndex - 1 < 0) songList.size - 1 else currentSongIndex - 1
        playSong(currentSongIndex)
    }

    fun isPlaying(): Boolean = mediaPlayer?.isPlaying == true

    fun getCurrentPosition(): Int = mediaPlayer?.currentPosition ?: 0

    fun getDuration(): Int = mediaPlayer?.duration ?: 0

    fun seekTo(pos: Int) {
        mediaPlayer?.seekTo(pos)
    }

    fun getCurrentSong(): Song? = if (songList.isNotEmpty()) songList[currentSongIndex] else null

    fun getCurrentSongIndex(): Int = currentSongIndex

    private fun showNotification(songTitle: String) {
        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MusicActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("正在播放")
            .setContentText(songTitle)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()

        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, notification)
    }

    private fun cancelNotification() {
        val manager = getSystemService(NotificationManager::class.java)
        manager.cancel(NOTIFICATION_ID)
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
