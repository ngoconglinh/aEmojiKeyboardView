package com.ice.emoji.repository

import com.ice.emoji.model.Emoji
import com.ice.emoji.model.EmojiGroup
import kotlinx.coroutines.flow.StateFlow

interface EmojiDataProvider {
    suspend fun getEmojiGroupData(): List<EmojiGroup>
    fun getEmojiRecent(): List<Emoji>
    suspend fun setEmojiRecent(emoji: Emoji)

    val emojiRecentStateFlow: StateFlow<List<Emoji>>

}