package com.example.affirmations

class Card(
    private val suit: String,
    private val rank: String,
    var isChosen: Boolean = false,
    var isMatched: Boolean = false
) {
    override fun toString(): String = suit + rank

    fun match(otherCards: Array<Card>): Int {
        when (otherCards.size) {
            2 -> {
                val first = otherCards[0]
                val second = otherCards[1]
                val allSameRank = rank == first.rank && rank == second.rank
                val allSameSuit = suit == first.suit && suit == second.suit
                return when {
                    allSameRank -> TRIPLE_RANK_SCORE
                    allSameSuit -> TRIPLE_SUIT_SCORE
                    else -> 0
                }
            }
            1 -> {
                val other = otherCards[0]
                if (other.rank == rank) return PAIR_RANK_SCORE
                if (other.suit == suit) return PAIR_SUIT_SCORE
            }
        }
        return 0
    }

    companion object {
        val rankStrings = arrayOf("A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K")
        val validSuits = arrayOf("♥", "♦", "♠", "♣")
        const val TRIPLE_RANK_SCORE = 9
        const val TRIPLE_SUIT_SCORE = 3
        const val PAIR_RANK_SCORE = 4
        const val PAIR_SUIT_SCORE = 1
    }
}
