package com.example.qiaoyutian

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class StudentAdapter(private val students: List<Student>) : RecyclerView.Adapter<StudentAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvId: TextView = view.findViewById(R.id.tv_student_id)
        val tvName: TextView = view.findViewById(R.id.tv_student_name)
        val tvGender: TextView = view.findViewById(R.id.tv_student_gender)
        val tvClass: TextView = view.findViewById(R.id.tv_student_class)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_student, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val student = students[position]
        holder.tvId.text = student.id
        holder.tvName.text = student.name
        holder.tvGender.text = student.gender
        holder.tvClass.text = student.className
    }

    override fun getItemCount() = students.size
}
