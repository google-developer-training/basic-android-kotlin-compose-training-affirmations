package com.example.affirmations

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity

class SecondActivity : ComponentActivity() {

    companion object {
        const val EXTRA_RESULT = "extra_result"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val density = resources.displayMetrics.density
        fun dp(value: Int): Int = (value * density).toInt()

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(0xFFF2F5FB.toInt())
            setPadding(dp(20), dp(24), dp(20), dp(24))
        }

        root.addView(TextView(this).apply {
            text = getString(R.string.act2_title)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
            setTextColor(0xFF0D47A1.toInt())
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, dp(16))
        })

        val input = EditText(this).apply {
            hint = getString(R.string.act2_hint)
            setSingleLine(true)
            setPadding(dp(12), dp(12), dp(12), dp(12))
            setBackgroundColor(android.graphics.Color.WHITE)
        }
        root.addView(input)

        root.addView(Button(this).apply {
            text = getString(R.string.act2_confirm)
            setPadding(dp(12), dp(14), dp(12), dp(14))
            setOnClickListener {
                val data = Intent().putExtra(EXTRA_RESULT, input.text.toString())
                setResult(Activity.RESULT_OK, data)
                finish()
            }
        })

        setContentView(root)
    }
}
