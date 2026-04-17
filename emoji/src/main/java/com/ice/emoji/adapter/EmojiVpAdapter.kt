package com.ice.emoji.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ice.emoji.model.EmojiPageState
import com.ice.emoji.EmojiRcvAdapter
import com.ice.emoji.EmojiViewListener
import com.ice.emoji.databinding.LayoutEmojiPageBinding
import com.ice.emoji.model.Emoji
import com.ice.emoji.model.EmojiGroup
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class EmojiVpAdapter(
    private val scope: CoroutineScope,
    private val listener: EmojiViewListener?,
    private val recentEmojiFlow: StateFlow<List<Emoji>>,
    private val emojiItemSize: Float = 0f,
    private val onEmojiClicked: ((Emoji) -> Unit)?
) : ListAdapter<EmojiGroup, EmojiVpAdapter.ViewHolder>(DiffCallback) {

    private val pageStates = mutableMapOf<Int, EmojiPageState>()
    private val sharedPool = RecyclerView.RecycledViewPool().apply {
        // Tối ưu: Emoji thường có số lượng lớn trên màn hình, tăng pool size giúp scroll mượt hơn
        setMaxRecycledViews(0, 60)
    }

    inner class ViewHolder(
        private val binding: LayoutEmojiPageBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private val rcvAdapter = EmojiRcvAdapter(emojiItemSize, listener, onEmojiClicked)
        private var collectJob: Job? = null
        private var currentState: EmojiPageState? = null

        init {
            binding.rcvEmoji.apply {
                adapter = rcvAdapter
                itemAnimator = null
                setHasFixedSize(true)
                setRecycledViewPool(sharedPool)
                setItemViewCacheSize(10)
                
                // Tối ưu: Chỉ thêm listener một lần duy nhất
                addOnScrollListener(object : RecyclerView.OnScrollListener() {
                    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                        if (dy <= 0) return
                        val lm = recyclerView.layoutManager as? GridLayoutManager ?: return
                        val last = lm.findLastVisibleItemPosition()
                        if (last >= rcvAdapter.itemCount - 5) { // Load trước khi chạm đáy
                            currentState?.let { state ->
                                scope.launch { state.loadMore() }
                            }
                        }
                    }
                })
            }
        }

        fun bind(
            group: EmojiGroup,
            pagePos: Int
        ) {
            collectJob?.cancel()
            if (pagePos == 0) {
                currentState = null
                bindRecent()
            } else {
                bindNormalPage(group, pagePos)
            }
        }

        private fun bindRecent() {
            collectJob = scope.launch {
                recentEmojiFlow.collect { list ->
                    binding.loadingView.visibility = View.GONE
                    binding.llEmpty.isVisible = list.isEmpty()
                    rcvAdapter.submitList(list)
                }
            }
        }

        private fun bindNormalPage(
            group: EmojiGroup,
            pagePos: Int
        ) {
            val state = pageStates.getOrPut(pagePos) {
                EmojiPageState(group.listEmoji).also {
                    scope.launch { it.loadMore(42) }
                }
            }
            currentState = state

            collectJob = scope.launch {
                state.data.collect { list ->
                    binding.loadingView.visibility = View.GONE
                    binding.llEmpty.isVisible = list.isEmpty()
                    rcvAdapter.submitList(list)
                }
            }
        }

        fun unbind() {
            collectJob?.cancel()
            currentState = null
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = LayoutEmojiPageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), position)
    }

    override fun onViewRecycled(holder: ViewHolder) {
        super.onViewRecycled(holder)
        holder.unbind()
    }

    object DiffCallback : DiffUtil.ItemCallback<EmojiGroup>() {
        override fun areItemsTheSame(old: EmojiGroup, new: EmojiGroup): Boolean {
            return old.group == new.group
        }

        override fun areContentsTheSame(old: EmojiGroup, new: EmojiGroup): Boolean {
            return old == new
        }
    }
}
