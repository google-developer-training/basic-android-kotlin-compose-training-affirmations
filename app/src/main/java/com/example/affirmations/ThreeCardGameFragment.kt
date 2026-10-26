package com.example.affirmations

/** 三张牌翻牌游戏 Fragment（实验六） */
class ThreeCardGameFragment : CardGameFragment() {

    override val mode: MatchMode = MatchMode.THREE_CARDS

    override fun recordOf(history: GameHistory): GameRecord = history.threeCard
}
