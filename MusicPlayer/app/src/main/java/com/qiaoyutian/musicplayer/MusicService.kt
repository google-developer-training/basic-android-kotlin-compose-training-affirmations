package com.qiaoyutian.musicplayer

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.provider.MediaStore
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat

class MusicService : Service() {

    companion object {
        private const val TAG = "MusicService"
        const val CHANNEL_ID = "music_channel"
        const val NOTIFICATION_ID = 1001

        const val ACTION_TOGGLE = "com.qiaoyutian.musicplayer.action.TOGGLE"
        const val ACTION_PREVIOUS = "com.qiaoyutian.musicplayer.action.PREVIOUS"
        const val ACTION_NEXT = "com.qiaoyutian.musicplayer.action.NEXT"
        const val ACTION_STOP = "com.qiaoyutian.musicplayer.action.STOP"
    }

    private lateinit var mediaPlayer: MediaPlayer
    private val songs = mutableListOf<Song>()
    private var currentIndex = -1
    private var prepared = false
    private var stopped = true
    private var foregroundStarted = false

    private val binder = MusicBinder()

    inner class MusicBinder : Binder() {
        fun getService(): MusicService = this@MusicService
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        initMediaPlayer()
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_TOGGLE -> if (isPlaying()) pause() else play()
            ACTION_PREVIOUS -> previousSong()
            ACTION_NEXT -> nextSong()
            ACTION_STOP -> stop()
            else -> reloadSongs()
        }
        return START_STICKY
    }

    private fun initMediaPlayer() {
        mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            setOnPreparedListener { mp ->
                prepared = true
                stopped = false
                mp.start()
                showNotification()
                startForegroundSafely()
            }
            setOnCompletionListener {
                nextSong()
            }
            setOnErrorListener { _, what, extra ->
                Log.e(TAG, "MediaPlayer error: what=$what, extra=$extra")
                true
            }
        }
    }

    
    fun reloadSongs(): List<Song> {
        val playingSongId = getCurrentSong()?.id
        songs.clear()
        if (!hasAudioPermission()) return emptyList()

        songs.addAll(querySongs("${MediaStore.Audio.Media.IS_MUSIC} != 0"))
        if (songs.isEmpty()) {
            songs.addAll(querySongs("${MediaStore.Audio.Media.DURATION} IS NULL"))
        }

        if (playingSongId != null) {
            val newIndex = songs.indexOfFirst { it.id == playingSongId }
            if (newIndex >= 0) currentIndex = newIndex
        }
        return songs.toList()
    }

    private fun querySongs(selection: String?): List<Song> {
        val result = mutableListOf<Song>()
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION
        )
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"
        try {
            contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                null,
                sortOrder
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val title = cursor.getString(titleColumn) ?: "未知歌曲"
                    val artist = cursor.getString(artistColumn)
                        ?.takeIf { it.isNotBlank() && it != "<unknown>" }
                        ?: "未知艺术家"
                    val duration = cursor.getLong(durationColumn)
                    val uri = ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        id
                    )
                    result.add(Song(id, title, artist, duration, uri))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "查询音乐列表失败", e)
        }
        return result
    }

    fun getSongs(): List<Song> = songs.toList()

    fun getCurrentSong(): Song? = songs.getOrNull(currentIndex)

    fun getCurrentIndex(): Int = currentIndex

    fun isStopped(): Boolean = stopped

    fun playSong(index: Int) {
        if (index !in songs.indices) return
        currentIndex = index
        prepared = false
        stopped = false
        try {
            mediaPlayer.reset()
            mediaPlayer.setDataSource(this, songs[index].uri)
            mediaPlayer.prepareAsync()
        } catch (e: Exception) {
            Log.e(TAG, "播放失败: ${songs[index].title}", e)
        }
    }

    fun play() {
        when {
            currentIndex < 0 -> if (songs.isNotEmpty()) playSong(0)
            prepared -> {
                mediaPlayer.start()
                stopped = false
                showNotification()
                startForegroundSafely()
            }
            else -> playSong(currentIndex)
        }
    }

    fun pause() {
        if (prepared && mediaPlayer.isPlaying) {
            mediaPlayer.pause()
            showNotification()
        }
    }

    fun stop() {
        if (prepared) {
            try {
                mediaPlayer.pause()
                mediaPlayer.seekTo(0)
            } catch (e: Exception) {
                Log.e(TAG, "停止播放失败", e)
            }
        }
        stopped = true
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        foregroundStarted = false
        NotificationManagerCompat.from(this).cancel(NOTIFICATION_ID)
    }

    fun nextSong() {
        if (songs.isEmpty()) return
        val next = if (currentIndex < 0) 0 else (currentIndex + 1) % songs.size
        playSong(next)
    }

    fun previousSong() {
        if (songs.isEmpty()) return
        val previous = if (currentIndex <= 0) songs.size - 1 else currentIndex - 1
        playSong(previous)
    }

    fun seekTo(position: Int) {
        if (prepared) {
            mediaPlayer.seekTo(position.coerceAtLeast(0))
        }
    }

    fun getPosition(): Int = try {
        if (prepared) mediaPlayer.currentPosition else 0
    } catch (e: Exception) {
        0
    }

    fun getDuration(): Int = try {
        if (prepared) mediaPlayer.duration else (getCurrentSong()?.duration?.toInt() ?: 0)
    } catch (e: Exception) {
        getCurrentSong()?.duration?.toInt() ?: 0
    }

    fun isPlaying(): Boolean = try {
        prepared && mediaPlayer.isPlaying
    } catch (e: Exception) {
        false
    }

    private fun hasAudioPermission(): Boolean {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            android.Manifest.permission.READ_MEDIA_AUDIO
        } else {
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        }
        return ContextCompat.checkSelfPermission(this, permission) ==
            PackageManager.PERMISSION_GRANTED
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.notification_channel_description)
                setShowBadge(false)
            }
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    
    private fun buildNotification(song: Song): android.app.Notification {
        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        fun actionIntent(action: String, requestCode: Int): PendingIntent =
            PendingIntent.getService(
                this,
                requestCode,
                Intent(this, MusicService::class.java).setAction(action),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_music_note)
            .setContentTitle(song.title)
            .setContentText(
                if (isPlaying()) {
                    "${song.artist} · ${getString(R.string.now_playing)}"
                } else {
                    "${song.artist} · ${getString(R.string.paused)}"
                }
            )
            .setContentIntent(contentIntent)
            .setOnlyAlertOnce(true)
            .setOngoing(isPlaying())
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .addAction(R.drawable.ic_skip_previous, getString(R.string.previous), actionIntent(ACTION_PREVIOUS, 11))
            .addAction(
                if (isPlaying()) R.drawable.ic_pause else R.drawable.ic_play,
                getString(if (isPlaying()) R.string.pause else R.string.play),
                actionIntent(ACTION_TOGGLE, 12)
            )
            .addAction(R.drawable.ic_skip_next, getString(R.string.next), actionIntent(ACTION_NEXT, 13))
            .build()
    }

    private fun showNotification() {
        val song = getCurrentSong() ?: return
        try {
            NotificationManagerCompat.from(this).notify(NOTIFICATION_ID, buildNotification(song))
        } catch (e: SecurityException) {
            Log.w(TAG, "没有通知权限，无法显示通知", e)
        }
    }

    private fun startForegroundSafely() {
        val song = getCurrentSong() ?: return
        if (foregroundStarted) return
        try {
            ServiceCompat.startForeground(
                this,
                NOTIFICATION_ID,
                buildNotification(song),
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
                } else {
                    0
                }
            )
            foregroundStarted = true
        } catch (e: Exception) {
            Log.w(TAG, "启动前台服务失败", e)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            mediaPlayer.release()
        } catch (e: Exception) {
            Log.e(TAG, "释放 MediaPlayer 失败", e)
        }
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        NotificationManagerCompat.from(this).cancel(NOTIFICATION_ID)
    }
}
