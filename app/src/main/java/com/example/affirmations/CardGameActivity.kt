package com.example.affirmations

import android.content.Intent
import android.os.Bundle
import android.view.ViewTreeObserver
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class CardGameActivity : AppCompatActivity(), CardAdapter.OnCardClickedListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var scoreView: TextView
    private lateinit var countView: TextView
    private lateinit var game: CardMatchingGame
    private lateinit var adapter: CardAdapter
    private var totalCards: Int = 0

    companion object {
        const val EXTRA_FINAL_SCORE = "extra_final_score"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_card_game)
        supportActionBar?.setTitle(R.string.title_card_game)

        totalCards = resources.getInteger(R.integer.card_count)
        val spanCount = resources.getInteger(R.integer.column_count)

        game = CardMatchingGame(totalCards)

        recyclerView = findViewById(R.id.cardRecyclerView)
        scoreView = findViewById(R.id.scoreView)
        countView = findViewById(R.id.countView)

        recyclerView.layoutManager = GridLayoutManager(this, spanCount)
        adapter = CardAdapter(game, this)
        recyclerView.adapter = adapter

        findViewById<Button>(R.id.btnReset).setOnClickListener { startNewGame() }
        findViewById<Button>(R.id.btnExit).setOnClickListener { exitGame() }

        recyclerView.viewTreeObserver.addOnGlobalLayoutListener(
            object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    recyclerView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    val padding = recyclerView.paddingLeft + recyclerView.paddingRight
                    adapter.cellSize = (recyclerView.width - padding) / spanCount
                    adapter.notifyDataSetChanged()
                }
            }
        )

        updateView()
    }

    private fun startNewGame() {
        game.reset(totalCards)
        adapter.notifyDataSetChanged()
        updateView()
    }

    override fun onCardClicked(position: Int) {
        game.chooseCardAtIndex(position)
        adapter.notifyDataSetChanged()
        updateView()
        if (game.matchedCount == totalCards) {
            Toast.makeText(
                this,
                getString(R.string.game_win, game.score),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun updateView() {
        scoreView.text = getString(R.string.game_score, game.score)
        val remaining = game.cardCount - game.matchedCount
        countView.text = getString(R.string.game_remaining, remaining)
    }

    private fun exitGame() {
        val data = Intent().putExtra(EXTRA_FINAL_SCORE, game.score)
        setResult(RESULT_OK, data)
        finish()
    }
}
