package com.example.model

object LanguageLayouts {
    val HINDI_ROW_1 = listOf(
        KeyItem("क", "क़", "1"),
        KeyItem("ख", "ख़", "2"),
        KeyItem("ग", "ग़", "3"),
        KeyItem("घ", "घ", "4"),
        KeyItem("ङ", "ङ", "5"),
        KeyItem("च", "च", "6"),
        KeyItem("छ", "छ", "7"),
        KeyItem("ज", "ज़", "8"),
        KeyItem("झ", "झ", "9"),
        KeyItem("ञ", "ञ", "0")
    )

    val HINDI_ROW_2 = listOf(
        KeyItem("ट", "ट", "@"),
        KeyItem("ठ", "ठ", "#"),
        KeyItem("ड", "ड़", "$"),
        KeyItem("ढ", "ढ़", "_"),
        KeyItem("ण", "ण", "&"),
        KeyItem("त", "त", "-"),
        KeyItem("थ", "थ", "+"),
        KeyItem("द", "द", "("),
        KeyItem("ध", "ध", ")"),
        KeyItem("न", "न", "/")
    )

    fun getHindiRow3(isShifted: Boolean): List<KeyItem> {
        val vowels = if (isShifted) {
            listOf("आ", "ई", "ऊ", "ए", "ऐ", "ओ", "औ")
        } else {
            listOf("प", "फ", "ब", "भ", "म", "य", "र")
        }
        return listOf(
            KeyItem("", type = KeyType.SHIFT, weight = 1.4f, displayLabel = if (isShifted) "अ/आ" else "स्वर"),
            KeyItem(vowels[0]),
            KeyItem(vowels[1]),
            KeyItem(vowels[2]),
            KeyItem(vowels[3]),
            KeyItem(vowels[4]),
            KeyItem(vowels[5]),
            KeyItem(vowels[6]),
            KeyItem("", type = KeyType.BACKSPACE, weight = 1.4f, displayLabel = "⌫")
        )
    }

    val HINDI_BOTTOM_ROW = listOf(
        KeyItem("?123", type = KeyType.SWITCH_SYMBOLS, weight = 1.3f, displayLabel = "?123"),
        KeyItem("😊", type = KeyType.EMOJI, weight = 1.1f, displayLabel = "😊"),
        KeyItem("ल", type = KeyType.CHARACTER, weight = 1.0f),
        KeyItem(" ", type = KeyType.SPACE, weight = 3.8f, displayLabel = "हिंदी"),
        KeyItem("स", type = KeyType.CHARACTER, weight = 1.0f),
        KeyItem("\n", type = KeyType.ENTER, weight = 1.4f, displayLabel = "⏎")
    )

    // Bengali (Bangla) Layout
    val BENGALI_ROW_1 = listOf(
        KeyItem("ক", "ক", "১"),
        KeyItem("খ", "খ", "২"),
        KeyItem("গ", "গ", "৩"),
        KeyItem("ঘ", "ঘ", "৪"),
        KeyItem("ঙ", "ঙ", "৫"),
        KeyItem("চ", "চ", "৬"),
        KeyItem("ছ", "ছ", "৭"),
        KeyItem("জ", "জ", "৮"),
        KeyItem("ঝ", "ঝ", "৯"),
        KeyItem("ঞ", "ঞ", "০")
    )

    val BENGALI_ROW_2 = listOf(
        KeyItem("ট", "ট", "@"),
        KeyItem("ঠ", "ঠ", "#"),
        KeyItem("ড", "ড়", "$"),
        KeyItem("ঢ", "ঢ়", "_"),
        KeyItem("ণ", "ণ", "&"),
        KeyItem("ত", "ত", "-"),
        KeyItem("থ", "থ", "+"),
        KeyItem("দ", "দ", "("),
        KeyItem("ধ", "ধ", ")"),
        KeyItem("ন", "ন", "/")
    )

    fun getBengaliRow3(isShifted: Boolean): List<KeyItem> {
        val vowels = if (isShifted) {
            listOf("আ", "ই", "ঈ", "উ", "ঊ", "এ", "ঐ")
        } else {
            listOf("প", "ফ", "ব", "ভ", "ম", "য", "র")
        }
        return listOf(
            KeyItem("", type = KeyType.SHIFT, weight = 1.4f, displayLabel = if (isShifted) "অ/আ" else "স্বর"),
            KeyItem(vowels[0]),
            KeyItem(vowels[1]),
            KeyItem(vowels[2]),
            KeyItem(vowels[3]),
            KeyItem(vowels[4]),
            KeyItem(vowels[5]),
            KeyItem(vowels[6]),
            KeyItem("", type = KeyType.BACKSPACE, weight = 1.4f, displayLabel = "⌫")
        )
    }

    val BENGALI_BOTTOM_ROW = listOf(
        KeyItem("?123", type = KeyType.SWITCH_SYMBOLS, weight = 1.3f, displayLabel = "?123"),
        KeyItem("😊", type = KeyType.EMOJI, weight = 1.1f, displayLabel = "😊"),
        KeyItem("ল", type = KeyType.CHARACTER, weight = 1.0f),
        KeyItem(" ", type = KeyType.SPACE, weight = 3.8f, displayLabel = "বাংলা"),
        KeyItem("স", type = KeyType.CHARACTER, weight = 1.0f),
        KeyItem("\n", type = KeyType.ENTER, weight = 1.4f, displayLabel = "⏎")
    )
}
