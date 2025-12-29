package com.ice.emoji.adapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

    inner class ViewHolder(
        private val binding: LayoutEmojiPageBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private val rcvAdapter = EmojiRcvAdapter(emojiItemSize, listener, onEmojiClicked)

        init {
            binding.rcvEmoji.apply {
                adapter = rcvAdapter
                itemAnimator = null
                setHasFixedSize(true)
            }
        }

        fun bind(
            group: EmojiGroup,
            pagePos: Int
        ) {
            if (pagePos == 0) {
                bindRecent()
            } else {
                bindNormalPage(group, pagePos)
            }
        }

        private fun bindRecent() {
            scope.launch {
                recentEmojiFlow.collect { list ->
                    binding.loadingView.visibility = View.GONE
                    rcvAdapter.submitList(list)
                }
            }
        }

        private fun bindNormalPage(
            group: EmojiGroup,
            pagePos: Int
        ) {
            val state = pageStates.getOrPut(pagePos) {
                EmojiPageState(group.listEmoji).apply {
                    loadMore(42)
                }
            }

            addLoadMoreListener(state)

            scope.launch {
                state.data.collect { list ->
                    binding.loadingView.visibility =
                        if (list.isEmpty()) View.VISIBLE else View.GONE
                    rcvAdapter.submitList(list)
                }
            }
        }

        private fun addLoadMoreListener(state: EmojiPageState) {
            binding.rcvEmoji.clearOnScrollListeners()
            binding.rcvEmoji.addOnScrollListener(
                object : RecyclerView.OnScrollListener() {
                    override fun onScrolled(
                        recyclerView: RecyclerView,
                        dx: Int,
                        dy: Int
                    ) {
                        if (dy <= 0) return
                        val lm = recyclerView.layoutManager as? GridLayoutManager ?: return
                        val last = lm.findLastVisibleItemPosition()
                        val total = rcvAdapter.itemCount
                        if (last >= total - 1) {
                            state.loadMore()
                        }
                    }
                }
            )
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

    object DiffCallback : DiffUtil.ItemCallback<EmojiGroup>() {
        override fun areItemsTheSame(old: EmojiGroup, new: EmojiGroup): Boolean {
            return old.group == new.group
        }

        override fun areContentsTheSame(old: EmojiGroup, new: EmojiGroup): Boolean {
            return old == new
        }
    }
}
