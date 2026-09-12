package com.example.model

enum class KeyType {
    CHARACTER,
    SHIFT,
    BACKSPACE,
    ENTER,
    SPACE,
    SWITCH_SYMBOLS,
    SWITCH_LETTERS,
    SWITCH_EXTRA_SYMBOLS,
    EMOJI,
    LANGUAGE
}

data class KeyItem(
    val primaryChar: String,
    val shiftChar: String = primaryChar.uppercase(),
    val secondaryChar: String? = null,
    val type: KeyType = KeyType.CHARACTER,
    val weight: Float = 1.0f,
    val displayLabel: String? = null,
    val row: Int = 0,
    val col: Int = 0
)

enum class KeyboardLayoutMode {
    LETTERS,
    SYMBOLS,
    EXTRA_SYMBOLS,
    EMOJIS
}

object KeyboardLayouts {
    val NUMBER_ROW = listOf(
        KeyItem("1"), KeyItem("2"), KeyItem("3"), KeyItem("4"), KeyItem("5"),
        KeyItem("6"), KeyItem("7"), KeyItem("8"), KeyItem("9"), KeyItem("0")
    )

    val QWERTY_ROW_1 = listOf(
        KeyItem("q", "Q", "1"),
        KeyItem("w", "W", "2"),
        KeyItem("e", "E", "3"),
        KeyItem("r", "R", "4"),
        KeyItem("t", "T", "5"),
        KeyItem("y", "Y", "6"),
        KeyItem("u", "U", "7"),
        KeyItem("i", "I", "8"),
        KeyItem("o", "O", "9"),
        KeyItem("p", "P", "0")
    )

    // Standard 9-key row with 0.5f spacer on each side to ensure exact alignment with 10-key rows
    val QWERTY_ROW_2 = listOf(
        KeyItem("a", "A", "@"),
        KeyItem("s", "S", "#"),
        KeyItem("d", "D", "$"),
        KeyItem("f", "F", "_"),
        KeyItem("g", "G", "&"),
        KeyItem("h", "H", "-"),
        KeyItem("j", "J", "+"),
        KeyItem("k", "K", "("),
        KeyItem("l", "L", ")")
    )

    fun getQwertyRow3(isShifted: Boolean, isCapsLock: Boolean): List<KeyItem> {
        val shiftLabel = if (isCapsLock) "⇪" else if (isShifted) "⬆" else "⇧"
        return listOf(
            KeyItem("", type = KeyType.SHIFT, weight = 1.5f, displayLabel = shiftLabel),
            KeyItem("z", "Z", "*"),
            KeyItem("x", "X", "\""),
            KeyItem("c", "C", "'"),
            KeyItem("v", "V", ":"),
            KeyItem("b", "B", ";"),
            KeyItem("n", "N", "!"),
            KeyItem("m", "M", "?"),
            KeyItem("", type = KeyType.BACKSPACE, weight = 1.5f, displayLabel = "⌫")
        )
    }

    val QWERTY_BOTTOM_ROW = listOf(
        KeyItem("?123", type = KeyType.SWITCH_SYMBOLS, weight = 1.4f, displayLabel = "?123"),
        KeyItem("😊", type = KeyType.EMOJI, weight = 1.1f, displayLabel = "😊"),
        KeyItem(",", type = KeyType.CHARACTER, weight = 1.0f),
        KeyItem(" ", type = KeyType.SPACE, weight = 4.0f, displayLabel = "KeyWave"),
        KeyItem(".", type = KeyType.CHARACTER, weight = 1.0f),
        KeyItem("\n", type = KeyType.ENTER, weight = 1.5f, displayLabel = "⏎")
    )

    // Complete symbols layouts with all requested symbols:
    // ! @ # $ % & * ( ) - _ + = / \ | [ ] { } < > : ; " ' ? , . ` ~ ^
    val SYMBOLS_ROW_1 = listOf(
        KeyItem("1"), KeyItem("2"), KeyItem("3"), KeyItem("4"), KeyItem("5"),
        KeyItem("6"), KeyItem("7"), KeyItem("8"), KeyItem("9"), KeyItem("0")
    )

    val SYMBOLS_ROW_2 = listOf(
        KeyItem("@"), KeyItem("#"), KeyItem("$"), KeyItem("%"), KeyItem("&"),
        KeyItem("-"), KeyItem("+"), KeyItem("("), KeyItem(")"), KeyItem("/")
    )

    val SYMBOLS_ROW_3 = listOf(
        KeyItem("=\\<", type = KeyType.SWITCH_EXTRA_SYMBOLS, weight = 1.5f, displayLabel = "=\\<"),
        KeyItem("*"), KeyItem("\""), KeyItem("'"), KeyItem(":"), KeyItem(";"),
        KeyItem("!"), KeyItem("?"),
        KeyItem("", type = KeyType.BACKSPACE, weight = 1.5f, displayLabel = "⌫")
    )

    val SYMBOLS_BOTTOM_ROW = listOf(
        KeyItem("ABC", type = KeyType.SWITCH_LETTERS, weight = 1.4f, displayLabel = "ABC"),
        KeyItem("😊", type = KeyType.EMOJI, weight = 1.1f, displayLabel = "😊"),
        KeyItem("_", type = KeyType.CHARACTER, weight = 1.0f),
        KeyItem(" ", type = KeyType.SPACE, weight = 4.0f, displayLabel = "KeyWave"),
        KeyItem(".", type = KeyType.CHARACTER, weight = 1.0f),
        KeyItem("\n", type = KeyType.ENTER, weight = 1.5f, displayLabel = "⏎")
    )

    // Extra Symbols Layout
    val EXTRA_SYMBOLS_ROW_1 = listOf(
        KeyItem("~"), KeyItem("`"), KeyItem("|"), KeyItem("•"), KeyItem("√"),
        KeyItem("π"), KeyItem("÷"), KeyItem("×"), KeyItem("¶"), KeyItem("∆")
    )

    val EXTRA_SYMBOLS_ROW_2 = listOf(
        KeyItem("£"), KeyItem("€"), KeyItem("¥"), KeyItem("¢"), KeyItem("^"),
        KeyItem("°"), KeyItem("="), KeyItem("{"), KeyItem("}"), KeyItem("\\")
    )

    val EXTRA_SYMBOLS_ROW_3 = listOf(
        KeyItem("?123", type = KeyType.SWITCH_SYMBOLS, weight = 1.5f, displayLabel = "?123"),
        KeyItem("<"), KeyItem(">"), KeyItem("["), KeyItem("]"), KeyItem("©"),
        KeyItem("®"), KeyItem("™"),
        KeyItem("", type = KeyType.BACKSPACE, weight = 1.5f, displayLabel = "⌫")
    )

    // Categorized Emojis for the dedicated Emoji Panel
    data class EmojiCategory(val name: String, val icon: String, val emojis: List<String>)

    val EMOJI_CATEGORIES = listOf(
        EmojiCategory(
            name = "Smileys",
            icon = "😀",
            emojis = listOf(
                "😀", "😃", "😄", "😁", "😆", "😅", "😂", "🤣", "😊", "😇",
                "🙂", "🙃", "😉", "😌", "😍", "🥰", "😘", "😗", "😙", "😚",
                "😋", "😛", "😝", "😜", "🤪", "🤨", "🧐", "🤓", "😎", "🤩",
                "🥳", "😏", "😒", "😞", "😔", "😟", "😕", "🙁", "☹️", "😣",
                "😖", "😫", "😩", "🥺", "😢", "😭", "😤", "😠", "😡", "🤬",
                "🤯", "😳", "🥵", "🥶", "😱", "😨", "😰", "😥", "😓", "🤗"
            )
        ),
        EmojiCategory(
            name = "People",
            icon = "👋",
            emojis = listOf(
                "👋", "🤚", "🖐️", "✋", "🖖", "👌", "🤌", "🤏", "✌️", "🤞",
                "🤟", "🤘", "🤙", "👈", "👉", "👆", "🖕", "👇", "☝️", "👍",
                "👎", "✊", "👊", "🤛", "🤜", "👏", "🙌", "👐", "🤲", "🤝",
                "🙏", "✍️", "💅", "🤳", "💪", "🦾", "🦿", "🦵", "🦶", "👂",
                "🦻", "👃", "🧠", "🫀", "🫁", "🦷", "🦴", "👀", "👁️", "👅"
            )
        ),
        EmojiCategory(
            name = "Animals",
            icon = "🐱",
            emojis = listOf(
                "🐶", "🐱", "🐭", "🐹", "🐰", "🦊", "🐻", "🐼", "🐻‍❄️", "🐨",
                "🐯", "🦁", "🐮", "🐷", "🐸", "🐵", "🐔", "🐧", "🐦", "🐤",
                "🦆", "🦅", "🦉", "🦇", "🐺", "🐗", "🐴", "🦄", "🐝", "🪱",
                "🐛", "🦋", "🐌", "🐞", "🐜", "🪰", "🪲", "🪳", "🦟", "🦗",
                "🕷️", "🦂", "🐢", "🐍", "🦎", "🦖", "🦕", "🐙", "🦑", "🦐"
            )
        ),
        EmojiCategory(
            name = "Food",
            icon = "🍕",
            emojis = listOf(
                "🍏", "🍎", "🍐", "🍊", "🍋", "🍌", "🍉", "🍇", "🍓", "🫐",
                "🍈", "🍒", "🍑", "🥭", "🍍", "🥥", "🥝", "🍅", "🍆", "🥑",
                "🥦", "🥬", "🥒", "🌶️", "🫑", "🌽", "🥕", "🫒", "🧄", "🧅",
                "🥔", "🍠", "🥐", "🥯", "🍞", "🥖", "🥨", "🧀", "🥚", "🍳",
                "🧈", "🥞", "🧇", "🥓", "🥩", "🍗", "🍖", "🦴", "🌭", "🍔",
                "🍟", "🍕", "🫓", "🥪", "🥙", "🧆", "🌮", "🌯", "🫔", "🥗",
                "☕", "🫖", "🍵", "🧃", "🥤", "🧋", "🍺", "🍻", "🥂", "🍷"
            )
        ),
        EmojiCategory(
            name = "Travel",
            icon = "🚀",
            emojis = listOf(
                "🚗", "🚕", "🚙", "🚌", "🚎", "🏎️", "🚓", "🚑", "🚒", "🚐",
                "🛻", "🚚", "🚛", "🚜", "🦯", "🦽", "🦼", "🛴", "🚲", "🛵",
                "🏍️", "🛺", "🚨", "🚔", "🚍", "🚘", "🚖", "🚡", "🚠", "🚟",
                "🚀", "🛸", "🚁", "🛶", "⛵", "🚤", "🛥️", "🛳️", "⛴️", "🚢",
                "✈️", "🛫", "🛬", "🪂", "💺", "🛰️", "🌍", "🌎", "🌏", "🗺️"
            )
        ),
        EmojiCategory(
            name = "Activities",
            icon = "⚽",
            emojis = listOf(
                "⚽", "🏀", "🏈", "⚾", "🥎", "🎾", "🏐", "🏉", "🥏", "🎱",
                "🪀", "🏓", "🏸", "🏒", "🏑", "🥍", "🏏", "🪃", "🥅", "⛳",
                "🪁", "🏹", "🎣", "🤿", "🥊", "🥋", "🎽", "🛹", "🛼", "🛷",
                "🎯", "🎮", "🎲", "🧩", "♟️", "🎭", "🎨", "🎬", "🎤", "🎧"
            )
        ),
        EmojiCategory(
            name = "Objects",
            icon = "💡",
            emojis = listOf(
                "💡", "🔦", "🏮", "🪔", "🧱", "🪨", "🪵", "🛖", "📱", "💻",
                "🖥️", "🖨️", "⌨️", "🖱️", "💾", "💿", "📀", "📷", "📸", "📹",
                "🎥", "📽️", "🎞️", "📞", "☎️", "📟", "📠", "📺", "📻", "🎙️",
                "💎", "💍", "👑", "🧢", "👒", "🎒", "💼", "📦", "🏷️", "🔒"
            )
        ),
        EmojiCategory(
            name = "Symbols",
            icon = "❤️",
            emojis = listOf(
                "❤️", "🧡", "💛", "💚", "💙", "💜", "🖤", "🤍", "🤎", "💔",
                "❣️", "💕", "💞", "💓", "💗", "💖", "💘", "💝", "💟", "☮️",
                "✝️", "☪️", "🕉️", "☸️", "✡️", "🔯", "🕎", "☯️", "☦️", "🛐",
                "💯", "🔥", "✨", "🌟", "💫", "⚡", "💥", "🎉", "🎊", "⚠️"
            )
        ),
        EmojiCategory(
            name = "Flags",
            icon = "🏁",
            emojis = listOf(
                "🏁", "🚩", "🎌", "🏴", "🏳️", "🏳️‍🌈", "🏳️‍⚧️", "🏴‍☠️",
                "🇺🇸", "🇬🇧", "🇨🇦", "🇦🇺", "🇩🇪", "🇫🇷", "🇮🇹", "🇪🇸",
                "🇯🇵", "🇰🇷", "🇨🇳", "🇮🇳", "🇧🇷", "🇲🇽", "🇿🇦", "🇷🇺"
            )
        )
    )

    // Flat list for backwards compatibility
    val COMMON_EMOJIS = EMOJI_CATEGORIES.flatMap { it.emojis }
}
