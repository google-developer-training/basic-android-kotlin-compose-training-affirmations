package com.example.affirmations

import java.io.Serializable

/**
 * 翻牌游戏历史记录（实验六：对象序列化存储）
 *
 * 两张牌、三张牌两种玩法各自维护一份 [GameRecord]，
 * 完成一局后由对应 Fragment 更新并写入文件，
 * 退出程序后再打开可以恢复显示历史最佳成绩。
 */
data class GameHistory(
    val twoCard: GameRecord = GameRecord(),
    val threeCard: GameRecord = GameRecord()
) : Serializable

/** 单个游戏的历史成绩 */
data class GameRecord(
    /** 已完成的局数 */
    var totalGames: Int = 0,
    /** 历史最高分 */
    var bestScore: Int = 0,
    /** 最近一局得分 */
    var lastScore: Int = 0,
    /** 最近一局完成时间戳（毫秒） */
    var lastTime: Long = 0L
) : Serializable
