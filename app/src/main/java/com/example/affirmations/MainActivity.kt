package com.example.affirmations

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var nameRow: TextView
    private lateinit var genderRow: TextView
    private lateinit var studentIdRow: TextView
    private lateinit var phoneRow: TextView
    private lateinit var emailRow: TextView
    private lateinit var hobbyRow: TextView
    private lateinit var highScoreRow: TextView

    private val gameLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val finalScore = result.data?.getIntExtra(CardGameActivity.EXTRA_FINAL_SCORE, 0) ?: 0
            if (finalScore > Player.highScore) {
                Player.highScore = finalScore
            }
        }
        refresh()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (Player.name.isEmpty()) {
            Player.resetDefaults(
                name = getString(R.string.default_name),
                gender = getString(R.string.default_gender),
                studentId = getString(R.string.default_student_id),
                phone = getString(R.string.default_phone),
                email = getString(R.string.default_email),
                hobbies = setOf(getString(R.string.hobby_reading))
            )
        }

        nameRow = findViewById(R.id.nameRow)
        genderRow = findViewById(R.id.genderRow)
        studentIdRow = findViewById(R.id.studentIdRow)
        phoneRow = findViewById(R.id.phoneRow)
        emailRow = findViewById(R.id.emailRow)
        hobbyRow = findViewById(R.id.hobbyRow)
        highScoreRow = findViewById(R.id.highScoreRow)

        findViewById<android.widget.Button>(R.id.btnEditInfo).setOnClickListener {
            startActivity(Intent(this, PersonInfoActivity::class.java))
        }
        findViewById<android.widget.Button>(R.id.btnPlayGame).setOnClickListener {
            gameLauncher.launch(Intent(this, CardGameActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    private fun refresh() {
        nameRow.text = getString(R.string.row_name, Player.name)
        genderRow.text = getString(R.string.row_gender, Player.gender)
        studentIdRow.text = getString(R.string.row_student_id, Player.studentId)
        phoneRow.text = getString(R.string.row_phone, Player.phone)
        emailRow.text = getString(R.string.row_email, Player.email)
        hobbyRow.text = getString(R.string.row_hobby, Player.hobbies.joinToString("、"))
        highScoreRow.text = getString(R.string.row_high_score, Player.highScore)
    }
}
