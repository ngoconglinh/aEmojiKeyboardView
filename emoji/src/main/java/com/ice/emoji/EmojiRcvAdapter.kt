package com.ice.emoji

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ice.emoji.databinding.LayoutEmojiItemBinding
import com.ice.emoji.model.Emoji

class EmojiRcvAdapter(
    private val emojiItemSize: Float,
    private val listener: EmojiListener?
) : ListAdapter<Emoji, EmojiRcvAdapter.EmojiViewHolder>(DiffCallback) {

    object DiffCallback : DiffUtil.ItemCallback<Emoji>() {
        override fun areItemsTheSame(oldItem: Emoji, newItem: Emoji): Boolean {
            return oldItem.codes == newItem.codes
        }

        override fun areContentsTheSame(oldItem: Emoji, newItem: Emoji): Boolean {
            return oldItem == newItem
        }
    }

    inner class EmojiViewHolder(val binding: LayoutEmojiItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Emoji) {
            binding.tvEmoji.apply {
                text = item.char
                textSize = emojiItemSize
                setOnClickListener {
                    listener?.onEmojiClick(item.char)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmojiViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = LayoutEmojiItemBinding.inflate(inflater, parent, false)
        return EmojiViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EmojiViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
