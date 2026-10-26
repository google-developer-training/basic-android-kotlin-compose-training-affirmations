package com.example.affirmations

import java.util.Random

/**
 * 翻牌匹配游戏逻辑（实验六：支持两张牌 / 三张牌两种模式）
 *
 * - 两张牌模式：每次翻开2张，点数相同得4分，仅花色相同得1分，均不匹配扣2分翻回；
 * - 三张牌模式：每次翻开3张，点数全相同得9分，花色全相同得3分，否则扣2分翻回；
 * - 每翻开一张牌固定消耗1分（COST_TO_CHOOSE）。
 */
enum class MatchMode { TWO_CARDS, THREE_CARDS }

class CardMatchingGame(count: Int, private val mode: MatchMode) {

    var score: Int = 0
        private set

    private val cards: MutableList<Card> = mutableListOf()
    private val random = Random()

    init {
        setup(count)
    }

    private fun setup(count: Int) {
        score = 0
        cards.clear()

        // 按模式分组发牌：两张牌模式两两一组，三张牌模式三三一组（同点数不同花色）
        val groupSize = if (mode == MatchMode.TWO_CARDS) 2 else 3
        val ranks = Card.rankStrings.toMutableList().apply { shuffle(random) }
        var dealt = 0
        var group = 0
        while (dealt + groupSize <= count && group < ranks.size) {
            val rank = ranks[group++]
            val suits = Card.validSuits.toMutableList().apply { shuffle(random) }
            repeat(groupSize) { k -> cards.add(Card(suit = suits[k], rank = rank)) }
            dealt += groupSize
        }

        // 剩余位置从完整牌堆补足
        val deck = Deck()
        while (dealt < count) {
            deck.drawRandomCard()?.let { cards.add(it) }
            dealt++
        }

        cards.shuffle(random)
    }

    fun reset(count: Int) {
        setup(count)
    }

    fun cardAtIndex(index: Int): Card = cards[index]

    val cardCount: Int get() = cards.size

    val matchedCount: Int get() = cards.count { it.isMatched }

    /** 本轮已翻开、需要参与匹配判定的未匹配牌数：两张牌模式为1，三张牌模式为2 */
    private val requiredChosen: Int
        get() = if (mode == MatchMode.TWO_CARDS) 1 else 2

    fun chooseCardAtIndex(index: Int) {
        val card = cards[index]
        if (card.isMatched) return
        // 再次点击已翻开的牌：翻回
        if (card.isChosen) {
            card.isChosen = false
            return
        }

        val chosen = cards.filter { it.isChosen && !it.isMatched }
        // 已翻开达到本轮要求的数量，先进行匹配判定
        if (chosen.size == requiredChosen) {
            val matchScore = card.match(chosen.toTypedArray())
            if (matchScore > 0) {
                score += matchScore * MATCH_BONUS
                card.isMatched = true
                chosen.forEach { it.isMatched = true }
            } else {
                score -= MISMATCH_PENALTY
                chosen.forEach { it.isChosen = false }
            }
        }

        score -= COST_TO_CHOOSE
        card.isChosen = true
    }

    companion object {
        const val MISMATCH_PENALTY = 2
        const val MATCH_BONUS = 4
        const val COST_TO_CHOOSE = 1
    }
}
