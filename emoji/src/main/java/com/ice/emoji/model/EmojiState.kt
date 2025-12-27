package com.ice.emoji.model

sealed class EmojiState<out T>  {
    object Idle : EmojiState<Nothing>()
    object Loading : EmojiState<Nothing>()
    data class Success<T>(val data: T) : EmojiState<T>()
    data class Error(val message: String?) : EmojiState<Nothing>()
}
