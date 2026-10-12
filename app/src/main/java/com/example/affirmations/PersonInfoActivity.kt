package com.example.affirmations

import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity

class PersonInfoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_person_info)
        supportActionBar?.setTitle(R.string.title_person_info)

        val inputName = findViewById<EditText>(R.id.inputName)
        val inputStudentId = findViewById<EditText>(R.id.inputStudentId)
        val inputPhone = findViewById<EditText>(R.id.inputPhone)
        val inputEmail = findViewById<EditText>(R.id.inputEmail)
        val genderGroup = findViewById<RadioGroup>(R.id.genderGroup)
        val radioMale = findViewById<RadioButton>(R.id.radioMale)
        val radioFemale = findViewById<RadioButton>(R.id.radioFemale)
        val chkTravel = findViewById<CheckBox>(R.id.chkTravel)
        val chkReading = findViewById<CheckBox>(R.id.chkReading)
        val chkGame = findViewById<CheckBox>(R.id.chkGame)
        val chkShopping = findViewById<CheckBox>(R.id.chkShopping)

        inputName.setText(Player.name)
        inputStudentId.setText(Player.studentId)
        inputPhone.setText(Player.phone)
        inputEmail.setText(Player.email)

        val maleStr = getString(R.string.gender_male)
        radioMale.isChecked = Player.gender == maleStr
        radioFemale.isChecked = !radioMale.isChecked

        val travel = getString(R.string.hobby_travel)
        val reading = getString(R.string.hobby_reading)
        val game = getString(R.string.hobby_game)
        val shopping = getString(R.string.hobby_shopping)
        chkTravel.isChecked = Player.hobbies.contains(travel)
        chkReading.isChecked = Player.hobbies.contains(reading)
        chkGame.isChecked = Player.hobbies.contains(game)
        chkShopping.isChecked = Player.hobbies.contains(shopping)

        findViewById<Button>(R.id.btnSave).setOnClickListener {
            val hobbies = mutableSetOf<String>()
            if (chkTravel.isChecked) hobbies.add(travel)
            if (chkReading.isChecked) hobbies.add(reading)
            if (chkGame.isChecked) hobbies.add(game)
            if (chkShopping.isChecked) hobbies.add(shopping)
            Player.name = inputName.text.toString().trim()
            Player.studentId = inputStudentId.text.toString().trim()
            Player.phone = inputPhone.text.toString().trim()
            Player.email = inputEmail.text.toString().trim()
            Player.gender = if (radioMale.isChecked) maleStr else getString(R.string.gender_female)
            Player.hobbies = hobbies
            finish()
        }

        findViewById<Button>(R.id.btnCancel).setOnClickListener { finish() }
    }
}
