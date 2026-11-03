package com.example.affirmations

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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MusicActivity : AppCompatActivity() {

    private lateinit var tvSongTitle: TextView
    private lateinit var lvSongs: ListView
    private lateinit var seekBar: SeekBar
    private lateinit var btnPlay: Button
    private lateinit var btnStop: Button
    private lateinit var btnPrev: Button
    private lateinit var btnNext: Button

    private var musicService: MusicService? = null
    private var isBound = false

    private val handler = Handler(Looper.getMainLooper())
    private var isTouchSeekBar = false

    private val seekBarRunnable = object : Runnable {
        override fun run() {
            musicService?.let { service ->
                if (!isTouchSeekBar) {
                    val pos = service.getCurrentPosition()
                    val dur = service.getDuration()
                    if (dur > 0) {
                        seekBar.max = dur
                        seekBar.progress = pos
                    }
                    service.getCurrentSong()?.let { song ->
                        tvSongTitle.text = song.title
                    }
                    btnPlay.text = if (service.isPlaying()) "暂停" else "播放"
                }
            }
            handler.postDelayed(this, 500)
        }
    }

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as MusicService.MusicBinder
            musicService = binder.getService()
            isBound = true
            updateSongList()
            handler.post(seekBarRunnable)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isBound = false
            musicService = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_music)

        tvSongTitle = findViewById(R.id.tvSongTitle)
        lvSongs = findViewById(R.id.lvSongs)
        seekBar = findViewById(R.id.seekBar)
        btnPlay = findViewById(R.id.btnPlay)
        btnStop = findViewById(R.id.btnStop)
        btnPrev = findViewById(R.id.btnPrev)
        btnNext = findViewById(R.id.btnNext)

        btnPlay.setOnClickListener {
            musicService?.let { service ->
                if (service.isPlaying()) {
                    service.pause()
                    btnPlay.text = "播放"
                } else {
                    service.play()
                    btnPlay.text = "暂停"
                }
            }
        }

        btnStop.setOnClickListener {
            musicService?.stop()
            btnPlay.text = "播放"
            seekBar.progress = 0
            tvSongTitle.text = "未播放"
        }

        btnPrev.setOnClickListener {
            musicService?.prevSong()
        }

        btnNext.setOnClickListener {
            musicService?.nextSong()
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser && isTouchSeekBar) {
                    musicService?.seekTo(progress)
                }
            }
            override fun onStartTrackingTouch(sb: SeekBar?) {
                isTouchSeekBar = true
            }
            override fun onStopTrackingTouch(sb: SeekBar?) {
                isTouchSeekBar = false
            }
        })

        requestAudioPermission()
    }

    private fun requestAudioPermission() {
        val permissions = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.READ_MEDIA_AUDIO)
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        val needed = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (needed.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, needed.toTypedArray(), 100)
        } else {
            bindMusicService()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100 && grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            bindMusicService()
        } else {
            Toast.makeText(this, "需要音频和通知权限才能正常使用", Toast.LENGTH_LONG).show()
        }
    }

    private fun bindMusicService() {
        val intent = Intent(this, MusicService::class.java)
        bindService(intent, connection, Context.BIND_AUTO_CREATE)
        startService(intent)
    }

    private fun updateSongList() {
        val songs = musicService?.getSongList() ?: emptyList()
        lvSongs.adapter = SongAdapter(songs)
        lvSongs.setOnItemClickListener { _, _, position, _ ->
            musicService?.playSong(position)
            btnPlay.text = "暂停"
        }
    }

    inner class SongAdapter(private val songs: List<Song>) : BaseAdapter() {
        override fun getCount() = songs.size
        override fun getItem(position: Int) = songs[position]
        override fun getItemId(position: Int) = position.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val view = convertView ?: LayoutInflater.from(this@MusicActivity)
                .inflate(android.R.layout.simple_list_item_2, parent, false)
            val text1 = view.findViewById<TextView>(android.R.id.text1)
            val text2 = view.findViewById<TextView>(android.R.id.text2)
            text1.text = songs[position].title
            text2.text = songs[position].artist
            return view
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(seekBarRunnable)
        if (isBound) {
            unbindService(connection)
            isBound = false
        }
    }
}
