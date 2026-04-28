package com.ice.emoji

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.content.withStyledAttributes
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.ice.emoji.adapter.EmojiVpAdapter
import com.ice.emoji.databinding.LayoutEmojiViewBinding
import com.ice.emoji.databinding.LayoutTabItemBinding
import com.ice.emoji.repository.EmojiDataProvider
import com.ice.emoji.repository.EmojiRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class EmojiView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), LifecycleOwner {

    private var lifecycleRegistry = LifecycleRegistry(this)
    override val lifecycle: Lifecycle get() = lifecycleRegistry
    
    private val emojiBinding = LayoutEmojiViewBinding.inflate(LayoutInflater.from(context), this, true)
    private var emojiViewListener: EmojiViewListener? = null
    private var listTabIcon = listOf<Int>()
    private var tabBg: Int? = null
    private var initTabIndex = 0

    private var colCount = 7
    private var emojiItemSize = 23f
    private var tabSize = 23f
    private var tabItemPadding = 20f
    
    private var tabColor: ColorStateList? = null
    private var tabBgColor: ColorStateList? = null

    private var pageAdapter: EmojiVpAdapter? = null
    private val dataProvider: EmojiDataProvider by lazy { EmojiRepository(context) }
    private var tabLayoutMediator: TabLayoutMediator? = null

    init {
        lifecycleRegistry.currentState = Lifecycle.State.CREATED
        
        var selectedColor = Color.WHITE
        var unselectedColor = Color.BLACK
        var selectedBg = Color.TRANSPARENT

        context.withStyledAttributes(attrs, R.styleable.EmojiView) {
            colCount = getInteger(R.styleable.EmojiView_evColumCount, 7)
            emojiItemSize = getDimensionPixelSize(R.styleable.EmojiView_evSize, 23).toFloat()
            tabSize = getDimensionPixelSize(R.styleable.EmojiView_evTabSize, 23).toFloat()
            tabItemPadding = getDimensionPixelSize(R.styleable.EmojiView_evTabMarginEnd, 23).toFloat()
            selectedColor = getColor(R.styleable.EmojiView_evTabSelectedColor, selectedColor)
            unselectedColor = getColor(R.styleable.EmojiView_evTabColor, unselectedColor)
            selectedBg = getColor(R.styleable.EmojiView_evTabBgColor, selectedBg)
        }

        tabColor = buildColorStateList(selectedColor, unselectedColor)
        tabBgColor = buildColorStateList(selectedBg, Color.TRANSPARENT)

        configureViewPager()
    }

    private fun configureViewPager() {
        emojiBinding.vpEmoji.apply {
            offscreenPageLimit = 1
            (getChildAt(0) as? RecyclerView)?.apply {
                setItemViewCacheSize(5)
                setHasFixedSize(true)
            }
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (lifecycleRegistry.currentState == Lifecycle.State.DESTROYED) {
            lifecycleRegistry = LifecycleRegistry(this)
            lifecycleRegistry.currentState = Lifecycle.State.CREATED
        }
        lifecycleRegistry.currentState = Lifecycle.State.STARTED
        lifecycleRegistry.currentState = Lifecycle.State.RESUMED
        setupInternal()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
        tabLayoutMediator?.detach()
        tabLayoutMediator = null
        pageAdapter = null
    }

    private fun setupInternal() {
        if (pageAdapter != null) return

        pageAdapter = EmojiVpAdapter(
            scope = lifecycleScope,
            listener = emojiViewListener,
            recentEmojiFlow = dataProvider.emojiRecentStateFlow,
            emojiItemSize = emojiItemSize,
            onEmojiClicked = { emoji ->
                lifecycleScope.launch(Dispatchers.IO) {
                    dataProvider.setEmojiRecent(emoji)
                }
            }
        ).also { emojiBinding.vpEmoji.adapter = it }

        lifecycleScope.launch {
            val data = dataProvider.getEmojiGroupData()
            withContext(Dispatchers.Main) {
                pageAdapter?.submitList(data) {
                    if (tabLayoutMediator == null) initTab()
                }
            }
        }
    }

    fun setTabColorTint(selectedColor: Int, unselectedColor: Int, selectedBgColor: Int) {
        tabColor = buildColorStateList(selectedColor, unselectedColor)
        tabBgColor = buildColorStateList(selectedBgColor, Color.TRANSPARENT)
        refreshTabStyles()
    }

    fun setSelectedTab(index: Int, smoothScroll: Boolean) {
        if (emojiBinding.vpEmoji.currentItem == index) return
        try {
            emojiBinding.vpEmoji.post {
                emojiBinding.vpEmoji.setCurrentItem(index, smoothScroll)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun buildColorStateList(selected: Int, unselected: Int): ColorStateList {
        val states = arrayOf(
            intArrayOf(android.R.attr.state_selected),
            intArrayOf(-android.R.attr.state_selected)
        )
        val colors = intArrayOf(selected, unselected)
        return ColorStateList(states, colors)
    }

    private fun refreshTabStyles() {
        with(emojiBinding) {
            viewLine.backgroundTintList = tabColor
            ivShare.imageTintList = tabColor
            
            repeat(tabEmojiCategory.tabCount) { index ->
                tabEmojiCategory.getTabAt(index)?.customView?.let { view ->
                    view.findViewById<ImageView>(R.id.ivTabIcon)?.apply {
                        imageTintList = tabColor
                        background?.setTintList(tabBgColor)
                    }
                }
            }
        }
    }

    private fun initTab() {
        val params = LinearLayout.LayoutParams(tabSize.toInt() + tabItemPadding.toInt(), tabSize.toInt())
        val inflater = LayoutInflater.from(context)

        with(emojiBinding) {
            ivShare.layoutParams = params
            flLine.layoutParams = LinearLayout.LayoutParams((tabSize * 0.7).toInt(), tabSize.toInt())
            viewLine.backgroundTintList = tabColor
            
            tabLayoutMediator?.detach()
            tabLayoutMediator = TabLayoutMediator(
                tabEmojiCategory,
                vpEmoji,
                true,
                false
            ) { tab, position ->
                val tabBinding = LayoutTabItemBinding.inflate(inflater, null, false)
                listTabIcon.getOrNull(position)?.let { tabBinding.ivTabIcon.setImageResource(it) }
                
                tabBg?.let {
                    tabBinding.ivTabIcon.background = ContextCompat.getDrawable(context, it)
                    tabBinding.ivTabIcon.background?.setTintList(tabBgColor)
                }
                
                tabBinding.ivTabIcon.imageTintList = tabColor
                tab.customView = tabBinding.root.apply { layoutParams = params }
            }.apply { attach() }

            tabEmojiCategory.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    tab?.customView?.findViewById<ImageView>(R.id.ivTabIcon)?.isSelected = true
                }
                override fun onTabUnselected(tab: TabLayout.Tab?) {
                    tab?.customView?.findViewById<ImageView>(R.id.ivTabIcon)?.isSelected = false
                }
                override fun onTabReselected(tab: TabLayout.Tab?) {}
            })

            ivShare.setOnClickListener { emojiViewListener?.onShare() }

            setSelectedTab(initTabIndex, false)
        }
    }

    class EmojiViewBuilder(private val view: EmojiView) {
        fun setTabIcon(icons: List<Int>) = apply { view.listTabIcon = icons }
        fun setTabBackground(bg: Int) = apply { view.tabBg = bg }
        fun setListener(listener: EmojiViewListener) = apply { view.emojiViewListener = listener }
        fun setInitTabIndex(index: Int) = apply { view.initTabIndex = index }
        fun setup() { view.setupInternal() }
    }
}