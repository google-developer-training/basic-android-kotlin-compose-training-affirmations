package com.example.affirmations

import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts

class MainActivity : ComponentActivity() {

    private lateinit var backResultView: TextView

    private val secondLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data?.getStringExtra(SecondActivity.EXTRA_RESULT).orEmpty()
            backResultView.text = getString(R.string.back_result_label, data)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(buildUI())
    }

    private fun buildUI(): View {
        val density = resources.displayMetrics.density
        fun dp(value: Int): Int = (value * density).toInt()

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(0xFFF2F5FB.toInt())
            setPadding(dp(20), dp(24), dp(20), dp(24))
        }

        root.addView(TextView(this).apply {
            text = getString(R.string.main_title)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 22f)
            setTextColor(0xFF0D47A1.toInt())
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, dp(20))
        })

        root.addView(makeButton(R.string.btn_open_act1) {
            val intent = Intent(this, FirstActivity::class.java)
            intent.putExtra(FirstActivity.EXTRA_PAYLOAD, getString(R.string.forward_payload))
            startActivity(intent)
        })

        root.addView(makeButton(R.string.btn_open_act2) {
            secondLauncher.launch(Intent(this, SecondActivity::class.java))
        })

        root.addView(makeButton(R.string.btn_open_act3) {
            startActivity(Intent(this, ThirdActivity::class.java))
        })

        root.addView(makeButton(R.string.btn_open_act4) {
            startActivity(Intent(this, FourthActivity::class.java))
        })

        backResultView = TextView(this).apply {
            text = getString(R.string.back_result_empty)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
            setTextColor(0xFF202936.toInt())
            setLineSpacing(0f, 1.3f)
            setPadding(dp(4), dp(20), dp(4), 0)
        }
        root.addView(backResultView)

        return root
    }

    private fun makeButton(textRes: Int, onClick: () -> Unit): Button {
        val density = resources.displayMetrics.density
        fun dp(value: Int): Int = (value * density).toInt()
        return Button(this).apply {
            text = getString(textRes)
            setPadding(dp(12), dp(14), dp(12), dp(14))
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = dp(12) }
            setOnClickListener { onClick() }
        }
    }
}
