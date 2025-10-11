package com.ice.emoji

import com.ice.emoji.model.Emoji

interface EmojiViewListener {
    fun onEmojiClick(s: String)

    fun onShare()
}