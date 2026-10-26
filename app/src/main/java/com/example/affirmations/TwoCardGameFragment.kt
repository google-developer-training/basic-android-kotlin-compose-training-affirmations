package com.example.affirmations

/** 两张牌翻牌游戏 Fragment（实验六） */
class TwoCardGameFragment : CardGameFragment() {

    override val mode: MatchMode = MatchMode.TWO_CARDS

    override fun recordOf(history: GameHistory): GameRecord = history.twoCard
}
