package com.example.affirmations

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * 翻牌游戏 Fragment 基类（实验六）
 *
 * 两张牌（TwoCardGameFragment）与三张牌（ThreeCardGameFragment）
 * 共用本基类的界面与逻辑，仅模式与历史记录不同。
 * 每完成一局，将成绩历史以对象序列化方式写入 game_history.dat。
 */
abstract class CardGameFragment : Fragment(), CardAdapter.OnCardClickedListener {

    /** 游戏匹配模式：两张牌 / 三张牌 */
    protected abstract val mode: MatchMode

    /** 从历史中取出本玩法对应的成绩记录 */
    protected abstract fun recordOf(history: GameHistory): GameRecord

    private lateinit var recyclerView: RecyclerView
    private lateinit var scoreView: TextView
    private lateinit var countView: TextView
    private lateinit var historyView: TextView
    private lateinit var game: CardMatchingGame
    private lateinit var adapter: CardAdapter
    private var totalCards: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_card_game, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        totalCards = when (mode) {
            MatchMode.TWO_CARDS -> resources.getInteger(R.integer.card_count_two)
            MatchMode.THREE_CARDS -> resources.getInteger(R.integer.card_count_three)
        }
        val spanCount = resources.getInteger(R.integer.column_count)

        game = CardMatchingGame(totalCards, mode)

        recyclerView = view.findViewById(R.id.cardRecyclerView)
        scoreView = view.findViewById(R.id.scoreView)
        countView = view.findViewById(R.id.countView)
        historyView = view.findViewById(R.id.historyView)

        recyclerView.layoutManager = GridLayoutManager(requireContext(), spanCount)
        adapter = CardAdapter(game, this)
        recyclerView.adapter = adapter

        view.findViewById<Button>(R.id.btnReset).setOnClickListener { startNewGame() }

        // 等布局完成后再按列宽计算每格卡片尺寸
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

        refreshHistory()
        updateView()
    }

    override fun onResume() {
        super.onResume()
        (activity as? AppCompatActivity)?.supportActionBar?.setTitle(titleRes)
        refreshHistory()
    }

    private val titleRes: Int
        get() = when (mode) {
            MatchMode.TWO_CARDS -> R.string.title_two_card
            MatchMode.THREE_CARDS -> R.string.title_three_card
        }

    private fun startNewGame() {
        game.reset(totalCards)
        adapter.notifyDataSetChanged()
        updateView()
        refreshHistory()
    }

    override fun onCardClicked(position: Int) {
        game.chooseCardAtIndex(position)
        adapter.notifyDataSetChanged()
        updateView()
        if (game.matchedCount == game.cardCount) {
            onGameFinished()
        }
    }

    /** 一局完成：更新历史记录并序列化保存 */
    private fun onGameFinished() {
        val history = Store.load<GameHistory>(requireContext(), Store.FILE_GAME_HISTORY)
            ?: GameHistory()
        val record = recordOf(history)
        record.totalGames += 1
        record.lastScore = game.score
        record.lastTime = System.currentTimeMillis()
        if (game.score > record.bestScore) {
            record.bestScore = game.score
        }
        Store.save(requireContext(), Store.FILE_GAME_HISTORY, history)
        refreshHistory()
        Toast.makeText(
            requireContext(),
            getString(R.string.game_win, game.score),
            Toast.LENGTH_LONG
        ).show()
    }

    /** 从序列化文件恢复并显示本玩法的历史成绩 */
    private fun refreshHistory() {
        val history = Store.load<GameHistory>(requireContext(), Store.FILE_GAME_HISTORY)
            ?: GameHistory()
        val record = recordOf(history)
        historyView.text = if (record.totalGames == 0) {
            getString(R.string.game_history_empty)
        } else {
            getString(
                R.string.game_history_text,
                record.bestScore,
                record.totalGames,
                record.lastScore
            )
        }
    }

    private fun updateView() {
        scoreView.text = getString(R.string.game_score, game.score)
        val remaining = game.cardCount - game.matchedCount
        countView.text = getString(R.string.game_remaining, remaining)
    }
}
