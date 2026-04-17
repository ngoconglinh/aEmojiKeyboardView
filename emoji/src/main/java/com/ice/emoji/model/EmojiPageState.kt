package com.ice.emoji.model

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext

private const val PAGE_SIZE = 21

class EmojiPageState(
    private val source: List<Emoji>
) {
    val data = MutableStateFlow<List<Emoji>>(emptyList())
    private var isLoading = false

    suspend fun loadMore(size: Int = PAGE_SIZE) = withContext(Dispatchers.Default) {
        if (isLoading) return@withContext
        val current = data.value
        if (current.size >= source.size) return@withContext

        isLoading = true
        try {
            val next = source
                .drop(current.size)
                .take(size)

            data.value = current + next
        } finally {
            isLoading = false
        }
    }
}
