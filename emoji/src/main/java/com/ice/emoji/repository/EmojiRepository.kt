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
import kotlinx.coroutines.withContext

internal class EmojiRepository(
    private val context: Context
) : EmojiDataProvider {

    private val _emojiRecentStateFlow = MutableStateFlow(getEmojiRecent())
    override val emojiRecentStateFlow: StateFlow<List<Emoji>> = _emojiRecentStateFlow.asStateFlow()

    override suspend fun getEmojiGroupData(): List<EmojiGroup> =
        withContext(Dispatchers.IO) {

            val json = context.assets
                .open(EMOJI_FILE_NAME)
                .bufferedReader()
                .use { it.readText() }

            val allEmoji = Gson()
                .fromJson(json, Array<Emoji>::class.java)
                .toList()

            val groups = allEmoji
                .groupBy { it.group }
                .map { (name, list) ->
                    EmojiGroup(name, list)
                }
                .toMutableList()

            groups.add(
                0,
                EmojiGroup(RECENT_EMOJI, getEmojiRecent())
            )

            groups
        }

    override fun getEmojiRecent(): List<Emoji> {
        val sharePreference = context.getSharedPreferences(STR_EMOJI_RECENT_KEY, MODE_PRIVATE)
        val str = sharePreference.getString(STR_EMOJI_RECENT, "")
        if (str == null || str == "") {
            return listOf()
        }
        return (Gson().fromJson(str, Array<Emoji>::class.java)?: arrayOf()).toList()
    }

    override suspend fun setEmojiRecent(emoji: Emoji) = withContext(Dispatchers.IO) {
        val mRecent = getEmojiRecent().toMutableList()
        if (mRecent.contains(emoji)) {
            mRecent.remove(emoji)
        } else {
            if (mRecent.size > 50) {
                mRecent.removeAt(mRecent.size - 1)
            }
        }
        mRecent.add(0, emoji)
        val jsonStr = Gson().toJson(mRecent)
        val sharedPreferences = context.getSharedPreferences(STR_EMOJI_RECENT_KEY, MODE_PRIVATE)
        sharedPreferences.edit {
            putString(STR_EMOJI_RECENT, jsonStr)
        }
        _emojiRecentStateFlow.value = mRecent
    }

    companion object {
        const val EMOJI_FILE_NAME = "emoji_data_2.json"

        const val STR_EMOJI_RECENT_KEY = "STR_EMOJI_RECENT_KEY"
        const val STR_EMOJI_RECENT = "STR_EMOJI_RECENT"
        const val RECENT_EMOJI = "Recent emoji"
    }
}
