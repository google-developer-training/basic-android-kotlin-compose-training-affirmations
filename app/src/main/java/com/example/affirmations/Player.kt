package com.example.affirmations

object Player {
    var name: String = ""
    var gender: String = ""
    var studentId: String = ""
    var phone: String = ""
    var email: String = ""
    var hobbies: MutableSet<String> = mutableSetOf()
    var highScore: Int = 0

    fun resetDefaults(
        name: String,
        gender: String,
        studentId: String,
        phone: String,
        email: String,
        hobbies: Set<String>
    ) {
        this.name = name
        this.gender = gender
        this.studentId = studentId
        this.phone = phone
        this.email = email
        this.hobbies = hobbies.toMutableSet()
    }
}
