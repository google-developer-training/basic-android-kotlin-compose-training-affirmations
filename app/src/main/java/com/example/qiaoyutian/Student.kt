package com.example.qiaoyutian

import java.io.Serializable

data class Student(
    val id: String,
    val name: String,
    val gender: String,
    val className: String
) : Serializable
