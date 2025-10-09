package com.ice.emoji

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.ice.emoji.databinding.LayoutEmojiPageBinding
import com.ice.emoji.model.Emoji
import com.ice.emoji.model.EmojiGroup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EmojiVpAdapter(
    private val pageInit: MutableLiveData<Int>,
    private val colCount: Int,
    private val emojiItemSize: Float,
    private val owner: LifecycleOwner,
    private val listener: EmojiListener?,
    private val listEmojiGroup: List<EmojiGroup>
): RecyclerView.Adapter<EmojiVpAdapter.ViewHolder>() {
    private var adapterRecent: EmojiRcvAdapter? = null
    private var llEmptyRecent: LinearLayout? = null
    private var rcvEmojiRecent: RecyclerView? = null

    inner class ViewHolder(private val viewBinding: LayoutEmojiPageBinding) : RecyclerView.ViewHolder(viewBinding.root) {
        fun onBind(eg: EmojiGroup, position: Int) {
            viewBinding.loadingView.visibility = View.VISIBLE
            pageInit.observe(owner) { initIndex ->
                if (initIndex != position) return@observe

                owner.lifecycleScope.launch(Dispatchers.IO) {
                    val rcvAdapter = EmojiRcvAdapter(emojiItemSize, listener) { emoji ->
                        Recent.setStrTemplateRecent(viewBinding.root.context, emoji)
                        val newRecent = Recent.getStrTemplateRecent(viewBinding.root.context)
                        adapterRecent?.submitList(newRecent) {
                            llEmptyRecent?.isVisible = newRecent.isEmpty()
                            rcvEmojiRecent?.isVisible = !newRecent.isEmpty()
                        }
                    }

                    if (position == 0) {
                        adapterRecent = rcvAdapter
                        llEmptyRecent = viewBinding.llEmpty
                        rcvEmojiRecent = viewBinding.rcvEmoji
                    }
                    val fullList = eg.listEmoji
                    val pageSize = colCount * 6
                    val displayed = mutableListOf<Emoji>()
                    var currentPage = 0
                    var isLoadingPage = false

                    suspend fun loadNextPage() {
                        if (isLoadingPage) return
                        val start = currentPage * pageSize
                        if (start >= fullList.size) return
                        isLoadingPage = true
                        val end = minOf(start + pageSize, fullList.size)
                        val nextChunk = fullList.subList(start, end)
                        displayed.addAll(nextChunk)
                        withContext(Dispatchers.Main) {
                            rcvAdapter.submitList(displayed.toList())
                        }
                        currentPage++
                        isLoadingPage = false
                    }

                    withContext(Dispatchers.Main) {
                        viewBinding.rcvEmoji.apply {
                            clearOnScrollListeners()

                            (itemAnimator as SimpleItemAnimator?)?.supportsChangeAnimations = false
                            itemAnimator?.changeDuration = 0
                            itemAnimator = null
                            layoutManager = GridLayoutManager(viewBinding.root.context, colCount)
                            adapter = rcvAdapter
                        }

                        owner.lifecycleScope.launch {
                            loadNextPage()
                            viewBinding.loadingView.visibility = View.GONE
                            viewBinding.llEmpty.isVisible = displayed.isEmpty()
                            viewBinding.rcvEmoji.isVisible = displayed.isNotEmpty()
                            viewBinding.rcvEmoji.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                                    super.onScrolled(recyclerView, dx, dy)
                                    val lm = recyclerView.layoutManager as? GridLayoutManager ?: return
                                    val lastVisible = lm.findLastVisibleItemPosition()
                                    val total = rcvAdapter.itemCount
                                    val threshold = colCount * 2
                                    if (!isLoadingPage && displayed.size < fullList.size && lastVisible >= total - threshold) {
                                        owner.lifecycleScope.launch {
                                            loadNextPage()
                                        }
                                    }
                                }
                            })
                            delay(1000)
                            pageInit.postValue(position + 1)
                        }
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val viewBinding = LayoutEmojiPageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(viewBinding)
    }

    override fun getItemCount(): Int = listEmojiGroup.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind(listEmojiGroup[position], position)
    }
}