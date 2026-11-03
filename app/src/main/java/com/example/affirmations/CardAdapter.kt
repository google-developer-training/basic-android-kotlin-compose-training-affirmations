package com.example.affirmations

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CardAdapter(
    private val game: CardMatchingGame,
    private val listener: OnCardClickedListener
) : RecyclerView.Adapter<CardAdapter.CardViewHolder>() {

    interface OnCardClickedListener {
        fun onCardClicked(position: Int)
    }

    var cellSize: Int = 0

    inner class CardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardView: TextView = itemView.findViewById(R.id.cardButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_card, parent, false)
        return CardViewHolder(view)
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        val card = game.cardAtIndex(position)

        if (cellSize > 0) {
            holder.itemView.layoutParams = holder.itemView.layoutParams.apply {
                width = cellSize
                height = cellSize
            }
        }

        when {
            card.isMatched -> {
                holder.cardView.text = card.toString()
                holder.cardView.setBackgroundResource(R.drawable.card_matched_bg)
                holder.cardView.setTextColor(Color.parseColor("#FF2E7D32"))
                holder.cardView.isEnabled = false
            }
            card.isChosen -> {
                holder.cardView.text = card.toString()
                holder.cardView.setBackgroundResource(R.drawable.card_chosen_bg)
                holder.cardView.setTextColor(Color.BLACK)
                holder.cardView.isEnabled = true
            }
            else -> {
                holder.cardView.text = holder.cardView.context.getString(R.string.card_back)
                holder.cardView.setBackgroundResource(R.drawable.card_poker)
                holder.cardView.isEnabled = true
            }
        }

        holder.itemView.setOnClickListener { listener.onCardClicked(position) }
    }

    override fun getItemCount(): Int = game.cardCount
}
