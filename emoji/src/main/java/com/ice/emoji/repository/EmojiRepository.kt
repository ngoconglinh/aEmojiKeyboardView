package com.ice.emoji.repository

import android.content.Context
import android.content.Context.MODE_PRIVATE
import androidx.core.content.edit
import com.google.gson.Gson
import com.ice.emoji.model.Emoji
import com.ice.emoji.model.EmojiGroup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

internal class EmojiRepository(
    private val context: Context
) : EmojiDataProvider {

    private val gson by lazy { Gson() }
    private val mutex = Mutex()

    private var cachedEmojiList: List<Emoji>? = null

    private val _emojiRecentStateFlow = MutableStateFlow<List<Emoji>>(emptyList())
    override val emojiRecentStateFlow: StateFlow<List<Emoji>> = _emojiRecentStateFlow.asStateFlow()

    init {
        _emojiRecentStateFlow.value = getEmojiRecent()
    }

    override suspend fun getEmojiGroupData(): List<EmojiGroup> =
        withContext(Dispatchers.Default) {
            try {
                val allEmoji = cachedEmojiList ?: context.assets
                    .open(EMOJI_FILE_NAME)
                    .bufferedReader()
                    .use { it.readText() }
                    .let { json ->
                        gson.fromJson(json, Array<Emoji>::class.java).toList()
                    }.also { 
                        cachedEmojiList = it 
                    }

                val recent = getEmojiRecent()

                val groups = allEmoji
                    .groupBy { it.group }
                    .map { (name, list) ->
                        EmojiGroup(name, list)
                    }
                    .toMutableList()

                groups.add(0, EmojiGroup(RECENT_EMOJI, recent))
                groups
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
        }

    override fun getEmojiRecent(): List<Emoji> {
        val sharePreference = context.getSharedPreferences(STR_EMOJI_RECENT_KEY, MODE_PRIVATE)
        val str = sharePreference.getString(STR_EMOJI_RECENT, null)
        if (str.isNullOrEmpty()) {
            return emptyList()
        }
        return try {
            gson.fromJson(str, Array<Emoji>::class.java)?.toList() ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    override suspend fun setEmojiRecent(emoji: Emoji) = withContext(Dispatchers.IO) {
        mutex.withLock {
            val currentRecent = getEmojiRecent().toMutableList()
            
            currentRecent.removeAll { it.char == emoji.char }
            currentRecent.add(0, emoji)
            
            val limitedRecent = if (currentRecent.size > 50) {
                currentRecent.take(50)
            } else {
                currentRecent
            }

            val jsonStr = gson.toJson(limitedRecent)
            context.getSharedPreferences(STR_EMOJI_RECENT_KEY, MODE_PRIVATE).edit {
                putString(STR_EMOJI_RECENT, jsonStr)
            }
            
            _emojiRecentStateFlow.value = limitedRecent
        }
    }

    companion object {
        const val EMOJI_FILE_NAME = "emoji_data_2.json"
        const val STR_EMOJI_RECENT_KEY = "STR_EMOJI_RECENT_KEY"
        const val STR_EMOJI_RECENT = "STR_EMOJI_RECENT"
        const val RECENT_EMOJI = "Recent emoji"
    }
}
