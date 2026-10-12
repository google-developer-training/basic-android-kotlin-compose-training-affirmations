package com.example.affirmations

import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity

class FirstActivity : ComponentActivity() {

    companion object {
        const val EXTRA_PAYLOAD = "extra_payload"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val density = resources.displayMetrics.density
        fun dp(value: Int): Int = (value * density).toInt()

        val payload = intent.getStringExtra(EXTRA_PAYLOAD).orEmpty()

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(0xFFF2F5FB.toInt())
            setPadding(dp(20), dp(24), dp(20), dp(24))
        }

        root.addView(TextView(this).apply {
            text = getString(R.string.act1_title)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
            setTextColor(0xFF0D47A1.toInt())
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, dp(16))
        })

        root.addView(TextView(this).apply {
            text = getString(R.string.act1_received_label, payload)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            setTextColor(0xFF202936.toInt())
            setLineSpacing(0f, 1.3f)
            setPadding(dp(8), dp(12), dp(8), dp(20))
        })

        root.addView(Button(this).apply {
            text = getString(R.string.btn_back)
            setOnClickListener { finish() }
        })

        setContentView(root)
    }
}
