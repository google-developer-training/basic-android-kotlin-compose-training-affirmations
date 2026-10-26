package com.example.affirmations

import android.content.Context
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

/**
 * 对象序列化存储工具（实验六）
 *
 * 写文件：openFileOutput + ObjectOutputStream
 * 读文件：openFileInput  + ObjectInputStream
 * 存储对象必须实现 java.io.Serializable（见 PlayerRecord / GameHistory）。
 */
object Store {

    /** 个人信息序列化文件 */
    const val FILE_PLAYER = "player.dat"

    /** 游戏历史序列化文件 */
    const val FILE_GAME_HISTORY = "game_history.dat"

    /**
     * 将可序列化对象写入内部存储文件。
     * @param data 必须实现 java.io.Serializable
     */
    fun save(context: Context, fileName: String, data: java.io.Serializable) {
        context.openFileOutput(fileName, Context.MODE_PRIVATE).use { fos ->
            ObjectOutputStream(fos).use { oos ->
                oos.writeObject(data)
            }
        }
    }

    /**
     * 从内部存储文件读回对象；文件不存在或读取失败返回 null。
     */
    @Suppress("UNCHECKED_CAST")
    fun <T> load(context: Context, fileName: String): T? {
        if (!context.getFileStreamPath(fileName).exists()) return null
        return try {
            context.openFileInput(fileName).use { fis ->
                ObjectInputStream(fis).use { ois ->
                    ois.readObject() as T
                }
            }
        } catch (e: Exception) {
            null
        }
    }
}
