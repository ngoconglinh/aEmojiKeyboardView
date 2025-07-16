[![](https://jitpack.io/v/ngoconglinh/aEmojiKeyboardView.svg)](https://jitpack.io/#ngoconglinh/aEmojiKeyboardView)

## Quick Start

**aEmojiKeyboardView** is available on jitpack.

Add dependency:

```groovy
implementation "com.github.ngoconglinh:aEmojiKeyboardView:last-release"
```

## Usage

to use **aEmojiKeyboardView**:

in **Setting.gradle**
```groovy
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}
```

in **xml**
```xml

    <com.ice.emoji.EmojiView
        android:id="@+id/emojiView"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        app:evColumCount="7"
        app:evSize="@dimen/_15sdp"
        app:evTabColor="@color/blue"
        app:evTabMarginEnd="@dimen/_7sdp"
        app:evTabSelectedColor="@color/purple_200"
        app:evTabSize="@dimen/_13ssp"
        app:layout_constraintBottom_toBottomOf="parent" />

```

Add PianoBar Listener to **PianoView**
```kotlin


class MainActivity : BaseViewBinding<ActivityMainBinding>(ActivityMainBinding::inflate),
    EmojiListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initData()
    }

    private fun initData() {
        val tabIcon = listOf(
            com.ice.emoji.R.drawable.baseline_access_time_24,
            com.ice.emoji.R.drawable.baseline_access_time_24,
            com.ice.emoji.R.drawable.baseline_access_time_24,
            com.ice.emoji.R.drawable.baseline_access_time_24,
            com.ice.emoji.R.drawable.baseline_access_time_24,
            com.ice.emoji.R.drawable.baseline_access_time_24,
            com.ice.emoji.R.drawable.baseline_access_time_24,
            com.ice.emoji.R.drawable.baseline_access_time_24
        )
        EmojiView.EmojiViewBuilder(bd.emojiView)
            .setTabIcon(tabIcon)
            .setTabBackground(com.ice.emoji.R.drawable.circle_bg)
            .emojiViewListener(this)
            .setupWithLifecycle(this)
    }

    override fun onEmojiClick(s: String) {

    }

    override fun onShare() {

    }
}
```
Demo:

<img width="426" height="976" alt="image" src="https://github.com/user-attachments/assets/91e0ae0d-3144-4df2-b4b1-78a449cc22a2" />

<img width="426" height="976" alt="image" src="https://github.com/user-attachments/assets/53d02343-81f2-4d8f-bb2f-d62878b073f8" />


<h2 id="creators">Special Thanks :heart:</h2>
