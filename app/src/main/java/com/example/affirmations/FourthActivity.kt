package com.example.affirmations

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import java.util.Locale

class FourthActivity : ComponentActivity() {

    private lateinit var timeView: TextView
    private lateinit var toggleButton: Button

    private val handler = Handler(Looper.getMainLooper())
    private var elapsedTime = 0L
    private var running = false
    private var baseTime = 0L

    companion object {
        private const val KEY_ELAPSED = "elapsed"
        private const val KEY_RUNNING = "running"
    }

    private val ticker = object : Runnable {
        override fun run() {
            if (running) {
                timeView.text = formatElapsed(elapsedTime + SystemClock.elapsedRealtime() - baseTime)
                handler.postDelayed(this, 10)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState != null) {
            elapsedTime = savedInstanceState.getLong(KEY_ELAPSED)
            running = savedInstanceState.getBoolean(KEY_RUNNING)
        }

        val density = resources.displayMetrics.density
        fun dp(value: Int): Int = (value * density).toInt()

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(0xFFF2F5FB.toInt())
            setPadding(dp(20), dp(24), dp(20), dp(24))
        }

        root.addView(TextView(this).apply {
            text = getString(R.string.act4_title)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
            setTextColor(0xFF0D47A1.toInt())
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, dp(24))
        })

        timeView = TextView(this).apply {
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 48f)
            setTextColor(0xFF202936.toInt())
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, dp(24))
        }
        root.addView(timeView)

        toggleButton = Button(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = dp(8) }
            setOnClickListener { toggle() }
        }
        root.addView(toggleButton)

        root.addView(Button(this).apply {
            text = getString(R.string.act4_reset)
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = dp(8) }
            setOnClickListener { reset() }
        })

        root.addView(Button(this).apply {
            text = getString(R.string.btn_back)
            setOnClickListener { finish() }
        })

        setContentView(root)

        if (running) {
            baseTime = SystemClock.elapsedRealtime()
            toggleButton.text = getString(R.string.act4_pause)
            handler.post(ticker)
        } else {
            timeView.text = formatElapsed(elapsedTime)
            toggleButton.text = getString(R.string.act4_start)
        }
    }

    private fun toggle() {
        if (running) {
            elapsedTime += SystemClock.elapsedRealtime() - baseTime
            running = false
            handler.removeCallbacks(ticker)
            toggleButton.text = getString(R.string.act4_start)
            timeView.text = formatElapsed(elapsedTime)
        } else {
            baseTime = SystemClock.elapsedRealtime()
            running = true
            toggleButton.text = getString(R.string.act4_pause)
            handler.post(ticker)
        }
    }

    private fun reset() {
        handler.removeCallbacks(ticker)
        running = false
        elapsedTime = 0L
        timeView.text = formatElapsed(0L)
        toggleButton.text = getString(R.string.act4_start)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (running) {
            elapsedTime += SystemClock.elapsedRealtime() - baseTime
        }
        outState.putLong(KEY_ELAPSED, elapsedTime)
        outState.putBoolean(KEY_RUNNING, running)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(ticker)
    }

    private fun formatElapsed(ms: Long): String {
        val totalSeconds = ms / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        val centi = (ms % 1000) / 10
        return String.format(Locale.US, "%02d:%02d.%02d", minutes, seconds, centi)
    }
}
