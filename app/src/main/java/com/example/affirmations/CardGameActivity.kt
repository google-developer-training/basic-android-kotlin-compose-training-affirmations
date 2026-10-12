package com.example.affirmations

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.GridLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class CardGameActivity : AppCompatActivity() {

    private lateinit var grid: GridLayout
    private lateinit var scoreView: TextView
    private lateinit var game: CardMatchingGame
    private val buttons = mutableListOf<Button>()

    companion object {
        const val EXTRA_FINAL_SCORE = "extra_final_score"
        private const val CARD_COUNT = 16
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_card_game)
        supportActionBar?.setTitle(R.string.title_card_game)

        grid = findViewById(R.id.cardGrid)
        scoreView = findViewById(R.id.scoreView)

        findViewById<Button>(R.id.btnReset).setOnClickListener { startNewGame() }
        findViewById<Button>(R.id.btnExit).setOnClickListener { exitGame() }

        startNewGame()
    }

    private fun startNewGame() {
        game = CardMatchingGame(CARD_COUNT)
        buttons.clear()
        grid.removeAllViews()

        val dm = resources.displayMetrics
        val horizontalPad = dp(24)
        val byWidth = (dm.widthPixels - horizontalPad) / 4
        val bottomBar = dp(64)
        val byHeight = (dm.heightPixels - bottomBar - dp(24)) / 4
        val size = minOf(byWidth, byHeight)

        for (i in 0 until CARD_COUNT) {
            val btn = Button(this).apply {
                setAllCaps(false)
                textSize = 16f
                setTextColor(Color.BLACK)
                setOnClickListener { onCardClicked(i) }
            }
            val params = GridLayout.LayoutParams().apply {
                width = size
                height = size
                columnSpec = GridLayout.spec(i % 4)
                rowSpec = GridLayout.spec(i / 4)
                setMargins(dp(4), dp(4), dp(4), dp(4))
            }
            grid.addView(btn, params)
            buttons.add(btn)
        }
        updateView()
    }

    private fun onCardClicked(index: Int) {
        game.chooseCardAtIndex(index)
        updateView()
        if (game.matchedCount == CARD_COUNT) {
            Toast.makeText(
                this,
                getString(R.string.game_win, game.score),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun updateView() {
        scoreView.text = getString(R.string.game_score, game.score)
        for (i in 0 until CARD_COUNT) {
            val card = game.cardAtIndex(i)
            val btn = buttons[i]
            when {
                card.isMatched -> {
                    btn.setTextColor(Color.BLACK)
                    btn.text = card.toString()
                    btn.setBackgroundColor(Color.parseColor("#FFC8E6C9"))
                    btn.isEnabled = false
                }
                card.isChosen -> {
                    btn.setTextColor(Color.BLACK)
                    btn.text = card.toString()
                    btn.setBackgroundColor(Color.WHITE)
                }
                else -> {
                    btn.text = ""
                    btn.setBackgroundResource(R.drawable.card_poker)
                }
            }
        }
    }

    private fun exitGame() {
        val data = Intent().putExtra(EXTRA_FINAL_SCORE, game.score)
        setResult(RESULT_OK, data)
        finish()
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()
}
