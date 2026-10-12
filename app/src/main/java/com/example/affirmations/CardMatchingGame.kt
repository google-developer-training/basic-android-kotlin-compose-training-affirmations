package com.example.affirmations

class CardMatchingGame(count: Int) {
    var score: Int = 0
        private set

    private val cards: MutableList<Card> = mutableListOf()

    init {
        val deck = Deck()
        repeat(count) {
            deck.drawRandomCard()?.let { cards.add(it) }
        }
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
        for (other in cards) {
            if (other.isChosen && !other.isMatched) {
                val matchScore = card.match(arrayOf(other))
                if (matchScore > 0) {
                    score += matchScore * MATCH_BONUS
                    other.isMatched = true
                    card.isMatched = true
                } else {
                    score -= MISMATCH_PENALTY
                    other.isChosen = false
                }
                break
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
