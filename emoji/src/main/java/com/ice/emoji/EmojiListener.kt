package com.ice.emoji

import com.ice.emoji.model.Emoji

interface EmojiListener {
    fun onEmojiClick(s: String)

    fun onShare()
}