package com.example.affirmations

import java.util.Random

class CardMatchingGame(count: Int) {
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

        val ranks = Card.rankStrings.toMutableList().apply { shuffle(random) }
        var dealt = 0
        var group = 0
        while (dealt + 3 <= count && group < ranks.size) {
            val rank = ranks[group++]
            val suits = Card.validSuits.toMutableList().apply { shuffle(random) }
            repeat(3) { k -> cards.add(Card(suit = suits[k], rank = rank)) }
            dealt += 3
        }

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

    fun chooseCardAtIndex(index: Int) {
        val card = cards[index]
        if (card.isMatched) return
        if (card.isChosen) {
            card.isChosen = false
            return
        }

        val chosen = cards.filter { it.isChosen && !it.isMatched }
        if (chosen.size == 2) {
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
