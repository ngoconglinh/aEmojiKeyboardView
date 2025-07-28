package com.ice.emoji

import android.content.Context
import android.content.Context.MODE_PRIVATE
import androidx.core.content.edit
import com.google.gson.Gson
import com.ice.emoji.model.Emoji

object Recent {
    const val STR_EMOJI_RECENT_KEY = "STR_EMOJI_RECENT_KEY"
    const val STR_EMOJI_RECENT = "STR_EMOJI_RECENT"

    fun getStrTemplateRecent(context: Context): List<Emoji> {
        val sharePreference = context.getSharedPreferences(STR_EMOJI_RECENT_KEY, MODE_PRIVATE)
        val str = sharePreference.getString(STR_EMOJI_RECENT, "")
        if (str == null || str == "") {
            return listOf()
        }
        return (Gson().fromJson(str, Array<Emoji>::class.java)?: arrayOf()).toList()
    }

    fun setStrTemplateRecent(context: Context, emoji: Emoji) {
        val mRecent = getStrTemplateRecent(context).toMutableList()
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
    }
}