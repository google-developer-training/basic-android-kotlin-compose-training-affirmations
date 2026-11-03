package com.example.affirmations

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CardMatchingGameTest {

    @Test
    fun initialScoreIsZero() {
        val game = CardMatchingGame(16)
        assertEquals(0, game.score)
        assertEquals(16, game.cardCount)
    }

    @Test
    fun choosingCardCostsOnePoint() {
        val game = CardMatchingGame(16)
        game.chooseCardAtIndex(0)
        assertEquals(-CardMatchingGame.COST_TO_CHOOSE, game.score)
        assertTrue(game.cardAtIndex(0).isChosen)
    }

    @Test
    fun choosingSameCardAgainFlipsItBack() {
        val game = CardMatchingGame(16)
        game.chooseCardAtIndex(0)
        game.chooseCardAtIndex(0)
        assertFalse(game.cardAtIndex(0).isChosen)
    }

    @Test
    fun rankMatchScoresFour() {
        val a = Card("♥", "A")
        val b = Card("♦", "A")
        assertEquals(4, a.match(arrayOf(b)))
    }

    @Test
    fun suitMatchScoresOne() {
        val a = Card("♥", "A")
        val b = Card("♥", "K")
        assertEquals(1, a.match(arrayOf(b)))
    }

    @Test
    fun noMatchScoresZero() {
        val a = Card("♥", "A")
        val b = Card("♦", "K")
        assertEquals(0, a.match(arrayOf(b)))
    }

    @Test
    fun deckDrawsAllUniqueCards() {
        val deck = Deck()
        val seen = mutableSetOf<String>()
        repeat(52) {
            val c = deck.drawRandomCard()
            assertNotNull(c)
            assertTrue(seen.add(c.toString()))
        }
        assertNull(deck.drawRandomCard())
    }

    @Test
    fun tripleRankMatchScoresNine() {
        val a = Card("♥", "A")
        val b = Card("♦", "A")
        val c = Card("♠", "A")
        assertEquals(9, a.match(arrayOf(b, c)))
    }

    @Test
    fun tripleSuitMatchScoresThree() {
        val a = Card("♥", "A")
        val b = Card("♥", "K")
        val c = Card("♥", "2")
        assertEquals(3, a.match(arrayOf(b, c)))
    }

    @Test
    fun tripleNoMatchScoresZero() {
        val a = Card("♥", "A")
        val b = Card("♦", "K")
        val c = Card("♠", "2")
        assertEquals(0, a.match(arrayOf(b, c)))
    }

    @Test
    fun resetClearsScoreAndDealsNewDeck() {
        val game = CardMatchingGame(24)
        game.chooseCardAtIndex(0)
        game.reset(12)
        assertEquals(0, game.score)
        assertEquals(12, game.cardCount)
        assertFalse(game.cardAtIndex(0).isChosen)
    }
}
