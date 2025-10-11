package com.ice.app

import android.os.Bundle
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.ice.emoji.EmojiView
import com.ice.emoji.EmojiViewListener

class MainActivity : AppCompatActivity(), EmojiViewListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
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
        EmojiView.EmojiViewBuilder(this, findViewById(R.id.emojiView))
            .setTabIcon(tabIcon)
            .setTabBackground(R.drawable.ic_launcher_background)
            .emojiViewListener(this@MainActivity)
            .setupWithLifecycle(this@MainActivity)
    }

    override fun onEmojiClick(s: String) {

        findViewById<EditText>(R.id.edt).setText(s)
    }

    override fun onShare() {

    }

    private fun codeToEmoji(codes: String): String {
        return codes
            .split(" ") // tách theo dấu cách
            .filter { it.isNotEmpty() } // loại bỏ khoảng trắng thừa
            .map { Integer.parseInt(it, 16) } // chuyển từ hex sang int
            .toIntArray()
            .let { String(it, 0, it.size) } // tạo chuỗi từ mảng codepoint
    }

}