package com.ice.emoji.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.ice.emoji.EmojiViewListener
import com.ice.emoji.fragment.EmojiFragment
import com.ice.emoji.model.Emoji
import com.ice.emoji.model.EmojiGroup
import com.ice.emoji.repository.EmojiRepository.Companion.RECENT_EMOJI
import kotlinx.coroutines.flow.StateFlow

class PageAdapter(
    private val listener: EmojiViewListener?,
    private val emojiItemSize: Float = 0f,
    private val recentEmoji: StateFlow<List<Emoji>>?,
    private val onAddRecent: (Emoji) -> Unit,
    fragmentActivity: FragmentActivity
) : FragmentStateAdapter(fragmentActivity) {
    private var pageData: List<EmojiGroup> = listOf()

    override fun getItemCount(): Int = pageData.size

    override fun createFragment(position: Int): Fragment {
        val recent = if (pageData[position].group == RECENT_EMOJI) recentEmoji else null
        return EmojiFragment().newInstance(pageData[position], listener, emojiItemSize, recent, onAddRecent)
    }

    fun submitPage(emojiGroups: List<EmojiGroup>) {
        this.pageData = emojiGroups
        notifyDataSetChanged()
    }
}