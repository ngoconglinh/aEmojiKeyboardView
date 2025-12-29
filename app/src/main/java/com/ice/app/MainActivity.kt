package com.ice.app

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.ice.app.databinding.ActivityMainBinding
import com.ice.emoji.EmojiView
import com.ice.emoji.EmojiViewListener

class MainActivity : AppCompatActivity() {
    private lateinit var bd: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        bd = ActivityMainBinding.inflate(layoutInflater)
        setContentView(bd.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initData()
    }

    private fun initData() {

        val tabIcon = listOf(
           R.drawable.ic_launcher_foreground,
           R.drawable.ic_launcher_foreground,
           R.drawable.ic_launcher_foreground,
           R.drawable.ic_launcher_foreground,
           R.drawable.ic_launcher_foreground,
           R.drawable.ic_launcher_foreground,
           R.drawable.ic_launcher_foreground,
           R.drawable.ic_launcher_foreground,
           R.drawable.ic_launcher_foreground,
           R.drawable.ic_launcher_foreground
        )

        EmojiView.EmojiViewBuilder(bd.emoji)
            .setTabIcon(tabIcon)
            .setTabBackground(R.drawable.ic_launcher_background)
            .emojiViewListener(object: EmojiViewListener {
                override fun onEmojiClick(emoji: String) {

                }

                override fun onShare() {

                }
            })
            .setupWithLifecycle(this.lifecycleScope)
    }
}