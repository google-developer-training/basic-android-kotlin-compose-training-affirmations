package com.example.affirmations

class Card(
    private val suit: String,
    private val rank: String,
    var isChosen: Boolean = false,
    var isMatched: Boolean = false
) {
    companion object {
        val rankStrings = arrayOf("A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K")
        val validSuits = arrayOf("♥", "♦", "♠", "♣")
    }

    override fun toString(): String = suit + rank

    fun match(otherCards: Array<Card>): Int {
        var score = 0
        if (otherCards.size == 1) {
            val other = otherCards[0]
            if (other.rank == rank) score = 4
            else if (other.suit == suit) score = 1
        }
        return score
    }
}
