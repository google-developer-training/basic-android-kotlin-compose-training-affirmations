package com.example.affirmations

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 个人信息 Fragment（实验六）
 *
 * 进入页面时通过对象序列化（openFileInput + ObjectInputStream）
 * 从 player.dat 恢复历史信息；点击保存时以 openFileOutput +
 * ObjectOutputStream 写回文件，退出程序后再打开仍可恢复。
 */
class PersonInfoFragment : Fragment() {

    private lateinit var infoView: TextView
    private lateinit var inputName: EditText
    private lateinit var inputStudentId: EditText
    private lateinit var inputPhone: EditText
    private lateinit var inputEmail: EditText
    private lateinit var radioMale: RadioButton
    private lateinit var radioFemale: RadioButton
    private lateinit var chkTravel: CheckBox
    private lateinit var chkReading: CheckBox
    private lateinit var chkGame: CheckBox
    private lateinit var chkShopping: CheckBox

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_person_info, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        infoView = view.findViewById(R.id.infoView)
        inputName = view.findViewById(R.id.inputName)
        inputStudentId = view.findViewById(R.id.inputStudentId)
        inputPhone = view.findViewById(R.id.inputPhone)
        inputEmail = view.findViewById(R.id.inputEmail)
        radioMale = view.findViewById(R.id.radioMale)
        radioFemale = view.findViewById(R.id.radioFemale)
        chkTravel = view.findViewById(R.id.chkTravel)
        chkReading = view.findViewById(R.id.chkReading)
        chkGame = view.findViewById(R.id.chkGame)
        chkShopping = view.findViewById(R.id.chkShopping)

        // 从序列化文件恢复表单；无文件时使用默认值
        val record = loadRecord() ?: defaultRecord()
        fillForm(record)

        view.findViewById<Button>(R.id.btnSave).setOnClickListener { saveToFile() }
        refreshInfo()
    }

    override fun onResume() {
        super.onResume()
        (activity as? AppCompatActivity)?.supportActionBar?.setTitle(R.string.title_person_fragment)
        refreshInfo()
    }

    private fun defaultRecord(): PlayerRecord = PlayerRecord(
        name = getString(R.string.default_name),
        gender = getString(R.string.default_gender),
        studentId = getString(R.string.default_student_id),
        phone = getString(R.string.default_phone),
        email = getString(R.string.default_email),
        hobbies = mutableSetOf(getString(R.string.hobby_reading))
    )

    private fun loadRecord(): PlayerRecord? =
        Store.load(requireContext(), Store.FILE_PLAYER)

    private fun fillForm(record: PlayerRecord) {
        inputName.setText(record.name)
        inputStudentId.setText(record.studentId)
        inputPhone.setText(record.phone)
        inputEmail.setText(record.email)

        val maleStr = getString(R.string.gender_male)
        radioMale.isChecked = record.gender == maleStr
        radioFemale.isChecked = !radioMale.isChecked

        chkTravel.isChecked = record.hobbies.contains(getString(R.string.hobby_travel))
        chkReading.isChecked = record.hobbies.contains(getString(R.string.hobby_reading))
        chkGame.isChecked = record.hobbies.contains(getString(R.string.hobby_game))
        chkShopping.isChecked = record.hobbies.contains(getString(R.string.hobby_shopping))
    }

    /** 展示从序列化文件恢复的个人信息与游戏历史 */
    private fun refreshInfo() {
        val record = loadRecord()
        if (record == null) {
            infoView.text = getString(R.string.no_history)
            return
        }
        val time = if (record.lastSavedTime > 0) {
            formatTime(record.lastSavedTime)
        } else {
            getString(R.string.time_unknown)
        }
        val history = Store.load<GameHistory>(requireContext(), Store.FILE_GAME_HISTORY)
            ?: GameHistory()
        infoView.text = getString(
            R.string.info_view,
            record.name,
            record.gender,
            record.studentId,
            record.phone,
            record.email,
            record.hobbies.joinToString("、"),
            time,
            history.twoCard.bestScore,
            history.threeCard.bestScore
        )
    }

    /** 收集表单输入并序列化保存到 player.dat */
    private fun saveToFile() {
        val hobbies = mutableSetOf<String>()
        if (chkTravel.isChecked) hobbies.add(getString(R.string.hobby_travel))
        if (chkReading.isChecked) hobbies.add(getString(R.string.hobby_reading))
        if (chkGame.isChecked) hobbies.add(getString(R.string.hobby_game))
        if (chkShopping.isChecked) hobbies.add(getString(R.string.hobby_shopping))

        val record = PlayerRecord(
            name = inputName.text.toString().trim(),
            gender = if (radioMale.isChecked) {
                getString(R.string.gender_male)
            } else {
                getString(R.string.gender_female)
            },
            studentId = inputStudentId.text.toString().trim(),
            phone = inputPhone.text.toString().trim(),
            email = inputEmail.text.toString().trim(),
            hobbies = hobbies,
            lastSavedTime = System.currentTimeMillis()
        )
        Store.save(requireContext(), Store.FILE_PLAYER, record)
        Toast.makeText(requireContext(), R.string.saved_ok, Toast.LENGTH_SHORT).show()
        refreshInfo()
    }

    private fun formatTime(timestamp: Long): String =
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(timestamp))
}
