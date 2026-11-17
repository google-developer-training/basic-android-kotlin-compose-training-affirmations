package com.qiaoyutian.musicplayer

import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat

class SongAdapter(private val context: Context) : BaseAdapter() {

    private var songs: List<Song> = emptyList()
    private var currentIndex = -1
    private var playing = false

    fun submit(list: List<Song>) {
        songs = list
        notifyDataSetChanged()
    }

    fun setState(index: Int, isPlaying: Boolean) {
        if (currentIndex == index && playing == isPlaying) return
        currentIndex = index
        playing = isPlaying
        notifyDataSetChanged()
    }

    override fun getCount(): Int = songs.size

    override fun getItem(position: Int): Song = songs[position]

    override fun getItemId(position: Int): Long = songs[position].id

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        val holder: ViewHolder
        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_song, parent, false)
            holder = ViewHolder(
                title = view.findViewById(R.id.item_title),
                artist = view.findViewById(R.id.item_artist),
                duration = view.findViewById(R.id.item_duration),
                playing = view.findViewById(R.id.item_playing),
                icon = view.findViewById(R.id.item_icon)
            )
            view.tag = holder
        } else {
            view = convertView
            holder = view.tag as ViewHolder
        }

        val song = songs[position]
        val isCurrent = position == currentIndex

        holder.title.text = song.title
        holder.artist.text = song.artist
        holder.duration.text = TimeFormat.mmss(song.duration)
        holder.playing.visibility = if (isCurrent) View.VISIBLE else View.GONE
        holder.playing.text = context.getString(
            if (playing) R.string.playing_now_label else R.string.paused_label
        )

        view.setBackgroundResource(if (isCurrent) R.drawable.bg_song_current else 0)

        val color = ContextCompat.getColor(
            context,
            if (isCurrent) R.color.brand_primary else R.color.text_secondary
        )
        ImageViewCompat.setImageTintList(holder.icon, ColorStateList.valueOf(color))
        holder.title.setTextColor(
            ContextCompat.getColor(
                context,
                if (isCurrent) R.color.brand_primary else R.color.text_primary
            )
        )
        return view
    }

    private data class ViewHolder(
        val title: TextView,
        val artist: TextView,
        val duration: TextView,
        val playing: TextView,
        val icon: ImageView
    )
}
