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
    private val listener: EmojiViewListener?,
    private val onEmojiClicked: ((Emoji) -> Unit)?
) : ListAdapter<Emoji, EmojiRcvAdapter.EmojiViewHolder>(DiffCallback) {

    object DiffCallback : DiffUtil.ItemCallback<Emoji>() {
        override fun areItemsTheSame(oldItem: Emoji, newItem: Emoji): Boolean {
            return oldItem.char == newItem.char
        }

        override fun areContentsTheSame(oldItem: Emoji, newItem: Emoji): Boolean {
            return oldItem.char == newItem.char
        }
    }

    inner class EmojiViewHolder(val binding: LayoutEmojiItemBinding) : RecyclerView.ViewHolder(binding.root) {
        private var currentEmoji: Emoji? = null

        init {
            binding.tvEmoji.textSize = emojiItemSize
            binding.root.setOnClickListener {
                currentEmoji?.let { emoji ->
                    listener?.onEmojiClick(emoji.char)
                    onEmojiClicked?.invoke(emoji)
                }
            }
        }

        fun bind(item: Emoji) {
            currentEmoji = item
            binding.tvEmoji.text = item.char
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
