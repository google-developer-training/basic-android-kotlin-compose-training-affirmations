package com.example.affirmations

import java.util.Random

class Deck {
    private val cards = mutableListOf<Card>()
    private val r = Random()

    init {
        for (suit in Card.validSuits) {
            for (rank in Card.rankStrings) {
                cards.add(Card(suit = suit, rank = rank))
            }
        }
    }

    fun drawRandomCard(): Card? {
        if (cards.isEmpty()) return null
        return cards.removeAt(r.nextInt(cards.size))
    }
}
