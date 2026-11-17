package com.qiaoyutian.musicplayer

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.widget.ImageButton
import android.widget.ListView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var tvCurrentTitle: TextView
    private lateinit var tvCurrentArtist: TextView
    private lateinit var tvPlayState: TextView
    private lateinit var tvCurrentTime: TextView
    private lateinit var tvTotalTime: TextView
    private lateinit var tvSongCount: TextView
    private lateinit var tvEmpty: TextView
    private lateinit var seekBar: SeekBar
    private lateinit var listSongs: ListView
    private lateinit var btnPrevious: ImageButton
    private lateinit var btnPlayPause: ImageButton
    private lateinit var btnStop: ImageButton

    private lateinit var adapter: SongAdapter

    private var musicService: MusicService? = null
    private var isBound = false
    private var isDragging = false
    private var lastSongId: Long? = null

    private val mainHandler = Handler(Looper.getMainLooper())
    private var progressThread: Thread? = null

    @Volatile
    private var progressRunning = false

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val audioGranted = results[audioPermission()] == true || hasAudioPermission()
        if (audioGranted) {
            musicService?.reloadSongs()
            refreshSongList()
            updateProgress()
        } else {
            refreshSongList()
            Toast.makeText(this, R.string.empty_songs_permission, Toast.LENGTH_LONG).show()
        }
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as? MusicService.MusicBinder ?: return
            musicService = binder.getService()
            isBound = true
            refreshSongList()
            updateSongInfo()
            updateProgress()
            startProgressLoop()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isBound = false
            musicService = null
            stopProgressLoop()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        applyWindowInsets()
        bindViews()
        setupToolbar()
        setupList()
        setupControls()
        requestRuntimePermissions()

        val intent = Intent(this, MusicService::class.java)
        startService(intent)
    }

    override fun onStart() {
        super.onStart()
        val intent = Intent(this, MusicService::class.java)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onStop() {
        super.onStop()
        stopProgressLoop()
        if (isBound) {
            unbindService(serviceConnection)
            isBound = false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopProgressLoop()
    }

    private fun applyWindowInsets() {
        val root = findViewById<android.view.View>(R.id.root_layout)
        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(0, bars.top, 0, bars.bottom)
            insets
        }
        ViewCompat.requestApplyInsets(root)
    }

    private fun bindViews() {
        toolbar = findViewById(R.id.toolbar)
        tvCurrentTitle = findViewById(R.id.tv_current_title)
        tvCurrentArtist = findViewById(R.id.tv_current_artist)
        tvPlayState = findViewById(R.id.tv_play_state)
        tvCurrentTime = findViewById(R.id.tv_current_time)
        tvTotalTime = findViewById(R.id.tv_total_time)
        tvSongCount = findViewById(R.id.tv_song_count)
        tvEmpty = findViewById(R.id.tv_empty)
        seekBar = findViewById(R.id.seek_bar)
        listSongs = findViewById(R.id.list_songs)
        btnPrevious = findViewById(R.id.btn_previous)
        btnPlayPause = findViewById(R.id.btn_play_pause)
        btnStop = findViewById(R.id.btn_stop)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.title = getString(R.string.app_name)
    }

    private fun setupList() {
        adapter = SongAdapter(this)
        listSongs.adapter = adapter
        listSongs.setOnItemClickListener { _, _, position, _ ->
            if (!hasAudioPermission()) {
                requestRuntimePermissions()
                return@setOnItemClickListener
            }
            val service = musicService ?: return@setOnItemClickListener
            service.playSong(position)
            adapter.setState(position, true)
            updateSongInfo()
            updateProgress()
        }
    }

    private fun setupControls() {
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seek: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    tvCurrentTime.text = TimeFormat.mmss(progress.toLong())
                }
            }

            override fun onStartTrackingTouch(seek: SeekBar) {
                isDragging = true
            }

            override fun onStopTrackingTouch(seek: SeekBar) {
                isDragging = false
                musicService?.seekTo(seek.progress)
                updateProgress()
            }
        })

        btnPlayPause.setOnClickListener {
            val service = musicService ?: return@setOnClickListener
            if (service.getSongs().isEmpty()) {
                Toast.makeText(this, R.string.no_song_selected, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (service.isPlaying()) service.pause() else service.play()
            updateProgress()
        }

        btnPrevious.setOnClickListener {
            musicService?.previousSong()
            updateSongInfo()
            updateProgress()
        }

        btnStop.setOnClickListener {
            musicService?.stop()
            updateProgress()
        }

        findViewById<ImageButton>(R.id.btn_next).setOnClickListener {
            musicService?.nextSong()
            updateSongInfo()
            updateProgress()
        }

        findViewById<MaterialButton>(R.id.btn_refresh).setOnClickListener {
            if (!hasAudioPermission()) {
                requestRuntimePermissions()
            } else {
                musicService?.reloadSongs()
                refreshSongList()
                Toast.makeText(
                    this,
                    String.format(Locale.CHINA, "已刷新，共 %d 首", adapter.count),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun refreshSongList() {
        val songs = musicService?.getSongs().orEmpty()
        adapter.submit(songs)
        tvSongCount.text = String.format(Locale.CHINA, "%d 首", songs.size)
        tvEmpty.visibility = if (songs.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
        tvEmpty.setText(
            if (hasAudioPermission()) R.string.empty_songs else R.string.empty_songs_permission
        )
    }

    private fun updateSongInfo() {
        val service = musicService ?: return
        val song = service.getCurrentSong()
        lastSongId = song?.id
        adapter.setState(service.getCurrentIndex(), service.isPlaying())
        if (song == null) {
            tvCurrentTitle.setText(R.string.unknown_song)
            tvCurrentArtist.setText(R.string.unknown_artist)
            tvTotalTime.text = "00:00"
        } else {
            tvCurrentTitle.text = song.title
            tvCurrentArtist.text = song.artist
            tvTotalTime.text = TimeFormat.mmss(song.duration)
        }
    }

    private fun updateProgress() {
        if (!isBound) return
        val service = musicService ?: return

        val song = service.getCurrentSong()
        if (song?.id != lastSongId) {
            updateSongInfo()
        }

        val duration = service.getDuration().coerceAtLeast(0)
        val position = service.getPosition()

        if (duration > 0) {
            seekBar.max = duration
        } else {
            seekBar.max = 100
        }
        if (!isDragging) {
            seekBar.progress = position.coerceIn(0, seekBar.max)
        }
        tvCurrentTime.text = TimeFormat.mmss(if (isDragging) seekBar.progress.toLong() else position.toLong())
        tvTotalTime.text = TimeFormat.mmss(duration.toLong())
        updatePlayState(service)
    }

    private fun updatePlayState(service: MusicService) {
        val playing = service.isPlaying()
        val hasSong = service.getCurrentSong() != null
        btnPlayPause.setImageResource(if (playing) R.drawable.ic_pause else R.drawable.ic_play)
        btnPlayPause.contentDescription =
            getString(if (playing) R.string.pause else R.string.play)

        tvPlayState.text = when {
            !hasSong -> getString(R.string.stopped)
            playing -> getString(R.string.now_playing)
            service.isStopped() -> getString(R.string.stopped)
            else -> getString(R.string.paused)
        }
        adapter.setState(service.getCurrentIndex(), playing && hasSong)
    }

    
    private fun startProgressLoop() {
        if (progressRunning) return
        progressRunning = true
        progressThread = Thread {
            while (progressRunning) {
                try {
                    Thread.sleep(500)
                } catch (e: InterruptedException) {
                    break
                }
                mainHandler.post { if (isBound) updateProgress() }
            }
        }.apply {
            name = "MusicProgressThread"
            isDaemon = true
            start()
        }
    }

    private fun stopProgressLoop() {
        progressRunning = false
        progressThread?.interrupt()
        progressThread = null
        mainHandler.removeCallbacksAndMessages(null)
    }

    private fun audioPermission(): String =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

    private fun hasAudioPermission(): Boolean =
        ContextCompat.checkSelfPermission(this, audioPermission()) ==
            PackageManager.PERMISSION_GRANTED

    private fun requestRuntimePermissions() {
        val permissions = mutableListOf<String>()
        if (!hasAudioPermission()) permissions.add(audioPermission())
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        if (permissions.isNotEmpty()) {
            permissionLauncher.launch(permissions.toTypedArray())
        }
    }
}
