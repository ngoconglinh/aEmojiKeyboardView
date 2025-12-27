package com.ice.emoji.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ice.emoji.EmojiRcvAdapter
import com.ice.emoji.EmojiViewListener
import com.ice.emoji.databinding.FragmentEmojiBinding
import com.ice.emoji.model.Emoji
import com.ice.emoji.model.EmojiGroup
import com.ice.emoji.model.EmojiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EmojiFragment : Fragment() {
    private var binding: FragmentEmojiBinding? = null

    private var emojiGroup: EmojiGroup? = null
    private var emojiItemSize: Float = 0f
    private var listener: EmojiViewListener? = null
    private val _emojiData = MutableStateFlow<EmojiState<List<Emoji>>>(EmojiState.Idle)
    private val emojiData = _emojiData.asStateFlow()
    private var emojiRecentStateFlow: StateFlow<List<Emoji>>? = null
    private var onEmojiClicked: ((Emoji) -> Unit)? = null
    private var rcvAdapter: EmojiRcvAdapter? = null
    private var isLoadingMore = false
    fun newInstance(
        data: EmojiGroup,
        listener: EmojiViewListener?,
        emojiItemSize: Float = 0f,
        emojiRecentStateFlow: StateFlow<List<Emoji>>?,
        onEmojiClicked: ((Emoji) -> Unit)?
    ): EmojiFragment {
        this.emojiGroup = data
        this.listener = listener
        this.emojiItemSize = emojiItemSize
        this.emojiRecentStateFlow = emojiRecentStateFlow
        this.onEmojiClicked = onEmojiClicked
        return this@EmojiFragment
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEmojiBinding.inflate(LayoutInflater.from(context), container, false)
        initView()
        initData()
        handleEvent()
        return binding?.root
    }

    private fun initData() {
        emojiRecentStateFlow?.let {
            viewLifecycleOwner.lifecycleScope.launch {
                it.collect { value ->
                    binding?.loadingView?.visibility = View.GONE
                    submitList(value)
                }
            }
        }?: run {
            viewLifecycleOwner.lifecycleScope.launch {
                emojiData.collect { state ->
                    when(state) {
                        is EmojiState.Idle -> {
                            binding?.loadingView?.visibility = View.VISIBLE
                        }
                        is EmojiState.Loading -> Unit
                        is EmojiState.Error -> {
                            binding?.loadingView?.visibility = View.GONE
                        }
                        is EmojiState.Success -> {
                            binding?.loadingView?.visibility = View.GONE
                            submitList(state.data)
                        }
                    }
                }
            }
        }
    }

    private var isOpenedFragment = false

    override fun onResume() {
        super.onResume()
        isOpenedFragment = true
        loadData(42)
    }

    private fun submitList(data: List<Emoji>) {
        binding?.llEmpty?.isVisible = data.isEmpty()
        rcvAdapter?.submitList(data)
    }

    private fun initView() = with(binding!!) {
        rcvAdapter = EmojiRcvAdapter(emojiItemSize, listener, onEmojiClicked)
        rcvEmoji.adapter = rcvAdapter
        rcvEmoji.itemAnimator = null
        rcvEmoji.setHasFixedSize(true)
    }

    private fun handleEvent() = with(binding!!) {
        if (emojiRecentStateFlow != null) return@with
        rcvEmoji.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (isLoadingMore || dy <= 0) return
                val lm = recyclerView.layoutManager as? GridLayoutManager ?: return
                val totalItemCount = rcvAdapter?.itemCount ?: 0
                val lastVisible = lm.findLastVisibleItemPosition()
                if (totalItemCount > 0 && lastVisible >= totalItemCount - 1) {
                    isLoadingMore = true
                    loadData()
                }
            }
        })
    }

    private fun loadData(loadSize: Int = 21) {
        if (!isOpenedFragment && emojiRecentStateFlow == null) return
        viewLifecycleOwner.lifecycleScope.launch {
            loadMore(loadSize)
        }
    }

    private suspend fun loadMore(loadSize: Int) {
        withContext(Dispatchers.Main) {
            _emojiData.value = EmojiState.Loading
        }
        try {
            val data = emojiGroup?.listEmoji ?: emptyList()
            val currentList = rcvAdapter?.currentList ?: emptyList()
            val currentIndex = currentList.size
            if (currentIndex >= data.size) {
                isLoadingMore = false
                _emojiData.value = EmojiState.Success(currentList)
                return
            }
            val loadIndex = (currentIndex + loadSize).coerceAtMost(data.size)
            val nextItems = data.subList(currentIndex, loadIndex)

            val newList = ArrayList(currentList)
            newList.addAll(nextItems)
            withContext(Dispatchers.Main) {
                _emojiData.value = EmojiState.Success(newList)
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                _emojiData.value = EmojiState.Error(e.message)
            }
        } finally {
            isLoadingMore = false
        }
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }

    override fun onDestroy() {
        lifecycleScope.cancel()
        super.onDestroy()
    }
}
