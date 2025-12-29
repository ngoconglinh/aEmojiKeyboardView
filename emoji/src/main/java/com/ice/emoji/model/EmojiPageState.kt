package com.ice.emoji.model

import kotlinx.coroutines.flow.MutableStateFlow

private const val PAGE_SIZE = 21

class EmojiPageState(
    val source: List<Emoji>
) {
    val data = MutableStateFlow<List<Emoji>>(emptyList())
    var isLoading = false

    fun loadMore(size: Int = PAGE_SIZE) {
        if (isLoading) return
        val current = data.value
        if (current.size >= source.size) return

        isLoading = true

        val next = source
            .drop(current.size)
            .take(size)

        data.value = current + next
        isLoading = false
    }
}
