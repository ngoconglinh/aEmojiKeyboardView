package com.ice.emoji

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.ice.emoji.databinding.LayoutEmojiPageBinding
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
    inner class ViewHolder(private val viewBinding: LayoutEmojiPageBinding) : RecyclerView.ViewHolder(viewBinding.root) {
        fun onBind(eg: EmojiGroup, position: Int) {
            viewBinding.loadingView.visibility = View.VISIBLE
            pageInit.observe(owner) {
                if (it != position) return@observe
                owner.lifecycleScope.launch(Dispatchers.IO) {
                    val rcvAdapter = EmojiRcvAdapter(emojiItemSize, listener) { emoji ->
                        Recent.setStrTemplateRecent(viewBinding.root.context, emoji)
                        val newRecent = Recent.getStrTemplateRecent(viewBinding.root.context)
                        adapterRecent?.submitList(newRecent)
                    }
                    if (position == 0) adapterRecent = rcvAdapter
                    val allEmoji = eg.listEmoji
                    withContext(Dispatchers.Main) {
                        viewBinding.rcvEmoji.apply {
                            (itemAnimator as SimpleItemAnimator?)?.supportsChangeAnimations = false
                            itemAnimator?.changeDuration = 0
                            itemAnimator = null
                            layoutManager = GridLayoutManager(viewBinding.root.context, colCount)
                            adapter = rcvAdapter
                        }
                        rcvAdapter.submitList(allEmoji)
                        viewBinding.loadingView.visibility = View.GONE
                        viewBinding.llEmpty.isVisible = allEmoji.isEmpty()
                        viewBinding.rcvEmoji.isVisible = !allEmoji.isEmpty()
                        delay(500)
                        pageInit.postValue(position + 1)
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