package com.example.qiaoyutian

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class AddStudentActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_student)

        val etId: EditText = findViewById(R.id.et_id)
        val etName: EditText = findViewById(R.id.et_name)
        val spinnerGender: Spinner = findViewById(R.id.spinner_gender)
        val spinnerClass: Spinner = findViewById(R.id.spinner_class)
        val btnConfirm: Button = findViewById(R.id.btn_confirm)
        val btnCancel: Button = findViewById(R.id.btn_cancel)

        val genders = arrayOf("男", "女")
        val genderAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, genders)
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerGender.adapter = genderAdapter

        val classes = arrayOf(
            "2024级1班", "2024级2班", "2024级3班", "2024级4班",
            "2024级5班", "2024级6班", "2024级7班", "2024级8班"
        )
        val classAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, classes)
        classAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerClass.adapter = classAdapter

        btnConfirm.setOnClickListener {
            val id = etId.text.toString().trim()
            val name = etName.text.toString().trim()
            val gender = spinnerGender.selectedItem.toString()
            val className = spinnerClass.selectedItem.toString()

            if (id.isEmpty() || name.isEmpty()) {
                Toast.makeText(this, "请填写完整信息", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val student = Student(id, name, gender, className)
            val intent = Intent()
            intent.putExtra("new_student", student)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }

        btnCancel.setOnClickListener {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
    }
}
