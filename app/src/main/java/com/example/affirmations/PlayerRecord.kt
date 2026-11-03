package com.example.affirmations

import java.io.Serializable

/**
 * 个人信息记录（实验六：对象序列化存储）
 *
 * 实现了 [Serializable]，配合 Store 工具使用
 * openFileOutput / ObjectOutputStream 写入文件、
 * openFileInput / ObjectInputStream 读回，退出程序后再次打开可以恢复。
 */
data class PlayerRecord(
    var name: String = "",
    var gender: String = "",
    var studentId: String = "",
    var phone: String = "",
    var email: String = "",
    var hobbies: MutableSet<String> = mutableSetOf(),
    /** 上次保存时间戳（毫秒），用于展示历史信息 */
    var lastSavedTime: Long = 0L
) : Serializable
