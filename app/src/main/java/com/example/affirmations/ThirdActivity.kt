package com.example.affirmations

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts

class ThirdActivity : ComponentActivity() {

    private lateinit var imageView: ImageView

    private val takePhotoLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val bitmap = result.data?.extras?.getParcelable<Bitmap>("data")
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap)
            }
        }
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
            text = getString(R.string.act3_title)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
            setTextColor(0xFF0D47A1.toInt())
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, dp(16))
        })

        imageView = ImageView(this).apply {
            setBackgroundColor(android.graphics.Color.WHITE)
            scaleType = ImageView.ScaleType.CENTER_INSIDE
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f
            ).apply { bottomMargin = dp(12) }
        }
        root.addView(imageView)

        root.addView(Button(this).apply {
            text = getString(R.string.act3_take_photo)
            setOnClickListener { dispatchTakePicture() }
        })

        setContentView(root)
    }

    private fun dispatchTakePicture() {
        try {
            takePhotoLauncher.launch(Intent(MediaStore.ACTION_IMAGE_CAPTURE))
        } catch (e: Exception) {
            Toast.makeText(this, R.string.act3_no_camera, Toast.LENGTH_SHORT).show()
        }
    }
}
