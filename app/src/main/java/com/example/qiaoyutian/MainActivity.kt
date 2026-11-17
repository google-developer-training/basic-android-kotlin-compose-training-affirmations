package com.example.qiaoyutian

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private val studentList = mutableListOf<Student>()
    private lateinit var adapter: StudentAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        title = "乔于恬2024050133"

        val recyclerView: RecyclerView = findViewById(R.id.recycler_view)
        val btnAdd: Button = findViewById(R.id.btn_add)

        adapter = StudentAdapter(studentList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        studentList.add(Student("2024050133", "乔于恬", "女", "2024级2班"))
        studentList.add(Student("2024050101", "张三", "男", "2024级2班"))
        studentList.add(Student("2024050102", "李四", "男", "2024级1班"))
        adapter.notifyDataSetChanged()

        btnAdd.setOnClickListener {
            val intent = Intent(this, AddStudentActivity::class.java)
            startActivityForResult(intent, 100)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
            val student = data?.getSerializableExtra("new_student") as? Student
            student?.let {
                studentList.add(it)
                adapter.notifyDataSetChanged()
                Toast.makeText(this, "添加成功", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
