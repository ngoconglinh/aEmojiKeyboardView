package com.ice.emoji

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.content.withStyledAttributes
import androidx.core.graphics.toColorInt
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.gson.Gson
import com.ice.emoji.databinding.LayoutEmojiViewBinding
import com.ice.emoji.databinding.LayoutTabItemBinding
import com.ice.emoji.model.Emoji
import com.ice.emoji.model.EmojiGroup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class EmojiView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val emojiBinding = LayoutEmojiViewBinding.inflate(LayoutInflater.from(context), this, true)
    private val pageInitiated = MutableLiveData(0)
    private var emojiListener: EmojiListener? = null
    private var listTabIcon = listOf<Int>()
    private var tabBg: Int? = null

    private var colCount = 7
    private var emojiItemSize = 23f
    private var tabSize = 23f
    private var tabItemPadding = 20f
    private var tabColor: ColorStateList
    private var tabBgColor: ColorStateList

    init {
        var tabSelectedColor = "#ffffff".toColorInt()
        var tabUnSelectedColor = "#000000".toColorInt()
        var tabBgSelectedColor = "#00000000".toColorInt()
        getContext().withStyledAttributes(attrs, R.styleable.EmojiView) {
            colCount = getInteger(R.styleable.EmojiView_evColumCount, 7)
            emojiItemSize = getDimensionPixelSize(R.styleable.EmojiView_evSize, 23).toFloat()
            tabSize = getDimensionPixelSize(R.styleable.EmojiView_evTabSize, 23).toFloat()
            tabItemPadding = getDimensionPixelSize(R.styleable.EmojiView_evTabMarginEnd, 23).toFloat()
            tabSelectedColor = getColor(R.styleable.EmojiView_evTabSelectedColor, tabSelectedColor)
            tabUnSelectedColor = getColor(R.styleable.EmojiView_evTabColor, tabUnSelectedColor)
            tabBgSelectedColor = getColor(R.styleable.EmojiView_evTabBgColor, tabBgSelectedColor)
        }

        val states = arrayOf<IntArray?>(
            intArrayOf(android.R.attr.state_selected),
            intArrayOf(-android.R.attr.state_selected)
        )

        val colors = intArrayOf(tabSelectedColor, tabUnSelectedColor)
        val colorsBg = intArrayOf(tabBgSelectedColor, "#00000000".toColorInt())
        tabColor = ColorStateList(states, colors)
        tabBgColor = ColorStateList(states, colorsBg)
    }

    private fun setTabIcon(listIcon: List<Int>) {
        this.listTabIcon = listIcon
    }

    private fun setTabBackground(bg: Int) {
        this.tabBg = bg
    }

    private fun setupWithLifecycle(owner: LifecycleOwner) {
        owner.lifecycleScope.launch(Dispatchers.IO) {
            val fileInString: String = context.assets.open("emoji_data.json").bufferedReader().use { it.readText() }
            val allEmoji = Gson().fromJson(fileInString, Array<Emoji>::class.java).toList()
            val allEmojiByGroup = groupEmojisByGroup(allEmoji).toMutableList()
            val recentGroup = EmojiGroup("Recent emoji", Recent.getStrTemplateRecent(context))
            allEmojiByGroup.add(0, recentGroup)
            withContext(Dispatchers.Main) {
                val vpAdapter = EmojiVpAdapter(pageInitiated, colCount, emojiItemSize, owner, emojiListener, allEmojiByGroup)
                emojiBinding.vpEmoji.apply {
                    if (allEmojiByGroup.isNotEmpty()) offscreenPageLimit = allEmojiByGroup.size
                    adapter = vpAdapter
                }
                initTab()
            }
        }
    }

    private fun initTab() {
        val params = LinearLayout.LayoutParams(tabSize.toInt() + tabItemPadding.toInt(), tabSize.toInt())

        emojiBinding.ivShare.layoutParams = params
        emojiBinding.flLine.layoutParams = LinearLayout.LayoutParams((tabSize  * 0.7).toInt(), tabSize.toInt())
        emojiBinding.viewLine.backgroundTintList = tabColor
        TabLayoutMediator(
            emojiBinding.tabEmojiCategory,
            emojiBinding.vpEmoji
        ) { tab, position ->
            val tabBinding = LayoutTabItemBinding.inflate(LayoutInflater.from(context), null, false)
            val icon = try {
                listTabIcon[position]
            } catch (_: IndexOutOfBoundsException) {
                null
            }
            icon?.let { tabBinding.ivTabIcon.setImageResource(icon) }
            tabBg?.let {
                tabBinding.ivTabIcon.background = ContextCompat.getDrawable(context, it)
                tabBinding.ivTabIcon.background.setTintList(tabBgColor)
            }
            tabBinding.ivTabIcon.imageTintList = tabColor
            tab.customView = tabBinding.root
            tab.customView?.layoutParams = params
        }.attach()

        emojiBinding.tabEmojiCategory.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener{
            override fun onTabSelected(p0: TabLayout.Tab?) {
                p0?.customView?.findViewById<ImageView>(R.id.ivTabIcon)?.isSelected = true
            }

            override fun onTabUnselected(p0: TabLayout.Tab?) {
                p0?.customView?.findViewById<ImageView>(R.id.ivTabIcon)?.isSelected = false
            }

            override fun onTabReselected(p0: TabLayout.Tab?) {}
        })

        emojiBinding.ivShare.setOnClickListener {
            emojiListener?.onShare()
        }
    }

    fun setTabColorTint(tabSelectedColor: Int, tabUnSelectedColor: Int, bgSelectedColor: Int) {
        val colors = intArrayOf(tabSelectedColor, tabUnSelectedColor)
        val states = arrayOf<IntArray?>(
            intArrayOf(android.R.attr.state_selected),
            intArrayOf(-android.R.attr.state_selected)
        )

        tabColor = ColorStateList(states, colors)
        val colorsBg = intArrayOf(bgSelectedColor, "#00000000".toColorInt())
        tabBgColor = ColorStateList(states, colorsBg)
        for (i in 0..emojiBinding.tabEmojiCategory.tabCount) {
            val customView = emojiBinding.tabEmojiCategory.getTabAt(i)?.customView
            val ivTabIcon = customView?.findViewById<ImageView>(R.id.ivTabIcon)
            ivTabIcon?.imageTintList = tabColor
            ivTabIcon?.background?.setTintList(tabBgColor)
        }
        emojiBinding.viewLine.backgroundTintList = tabColor
        emojiBinding.ivShare.imageTintList = tabColor
    }

    private fun groupEmojisByGroup(emojis: List<Emoji>): List<EmojiGroup> {
        return emojis.groupBy { it.group }
            .map { (groupName, emojiList) ->
                EmojiGroup(group = groupName, listEmoji = emojiList)
            }
    }

    private fun emojiViewListener(emojiListener: EmojiListener) {
        this.emojiListener = emojiListener
    }

    class EmojiViewBuilder(private val emojiView: EmojiView) {

        fun setTabIcon(listIcon: List<Int>): EmojiViewBuilderTabBg {
            emojiView.setTabIcon(listIcon)
            return EmojiViewBuilderTabBg(emojiView)
        }

        class EmojiViewBuilderTabBg(private val emojiView: EmojiView) {
            fun setTabBackground(bg: Int): EmojiViewBuilderListener {
                emojiView.setTabBackground(bg)
                return EmojiViewBuilderListener(emojiView)
            }
        }

        class EmojiViewBuilderListener(private val emojiView: EmojiView) {
            fun emojiViewListener(listener: EmojiListener): EmojiViewBuilderFinal {
                emojiView.emojiViewListener(listener)
                return EmojiViewBuilderFinal(emojiView)
            }
        }

        class EmojiViewBuilderFinal(private val emojiView: EmojiView) {
            fun setupWithLifecycle(owner: LifecycleOwner) {
                emojiView.setupWithLifecycle(owner)
            }
        }
    }

}