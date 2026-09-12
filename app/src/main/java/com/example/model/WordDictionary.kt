package com.example.model

/**
 * Common English dictionary for local offline word suggestion and auto-correction.
 * Prioritizes privacy: 100% offline, zero network requests, zero logging.
 */
object WordDictionary {
    val WORDS = listOf(
        "the", "be", "to", "of", "and", "a", "in", "that", "have", "i",
        "it", "for", "not", "on", "with", "he", "as", "you", "do", "at",
        "this", "but", "his", "by", "from", "they", "we", "say", "her", "she",
        "or", "an", "will", "my", "one", "all", "would", "there", "their", "what",
        "so", "up", "out", "if", "about", "who", "get", "which", "go", "me",
        "when", "make", "can", "like", "time", "no", "just", "him", "know", "take",
        "people", "into", "year", "your", "good", "some", "could", "them", "see", "other",
        "than", "then", "now", "look", "only", "come", "its", "over", "think", "also",
        "back", "after", "use", "two", "how", "our", "work", "first", "well", "way",
        "even", "new", "want", "because", "any", "these", "give", "day", "most", "us",
        "hello", "how", "what", "where", "why", "when", "which", "who", "whose", "here",
        "there", "today", "tomorrow", "tonight", "yesterday", "morning", "night", "thanks", "thank", "please",
        "great", "awesome", "perfect", "amazing", "wonderful", "excellent", "beautiful", "happy", "love", "friend",
        "help", "hope", "need", "feel", "try", "call", "send", "receive", "meet", "talk",
        "message", "phone", "email", "home", "work", "school", "office", "place", "world", "life",
        "thing", "idea", "problem", "question", "answer", "reason", "system", "program", "music", "game",
        "water", "food", "coffee", "tea", "money", "power", "change", "start", "stop", "open",
        "close", "read", "write", "listen", "watch", "play", "run", "walk", "sleep", "eat",
        "drink", "buy", "sell", "pay", "wait", "stay", "leave", "bring", "keep", "hold",
        "turn", "show", "hear", "let", "begin", "seem", "live", "believe", "happen", "include",
        "continue", "set", "learn", "change", "lead", "understand", "follow", "stop", "create", "speak",
        "allow", "add", "spend", "grow", "open", "walk", "win", "offer", "remember", "consider",
        "appear", "buy", "serve", "die", "send", "build", "stay", "fall", "cut", "reach",
        "kill", "remain", "suggest", "raise", "pass", "sell", "require", "report", "decide", "pull"
    )

    /**
     * Returns 3-5 completions matching the current input prefix.
     */
    fun getSuggestions(prefix: String, maxCount: Int = 4): List<String> {
        val clean = prefix.trim().lowercase()
        if (clean.isEmpty()) return listOf("the", "to", "and", "you")
        
        val matches = mutableListOf<String>()
        // Exact prefix matches
        for (w in WORDS) {
            if (w.startsWith(clean) && w != clean) {
                matches.add(w)
                if (matches.size >= maxCount) return matches
            }
        }
        
        // If not enough matches, try fuzzy distance 1
        if (matches.size < maxCount && clean.length >= 3) {
            for (w in WORDS) {
                if (w !in matches && w != clean && kotlin.math.abs(w.length - clean.length) <= 1) {
                    if (levenshteinDistance(clean, w) <= 1) {
                        matches.add(w)
                        if (matches.size >= maxCount) return matches
                    }
                }
            }
        }
        
        return matches
    }

    /**
     * Returns the best auto-correction for a completed token, or null if no correction is warranted.
     */
    fun getAutoCorrection(word: String): String? {
        val clean = word.trim().lowercase()
        if (clean.length < 3) return null
        if (clean in WORDS) return null // Already correctly spelled
        
        var bestMatch: String? = null
        var bestDistance = 2 // max threshold
        for (w in WORDS) {
            val dist = levenshteinDistance(clean, w)
            if (dist < bestDistance) {
                bestDistance = dist
                bestMatch = w
            }
        }
        return bestMatch
    }

    private fun levenshteinDistance(a: String, b: String): Int {
        val dp = Array(a.length + 1) { IntArray(b.length + 1) }
        for (i in 0..a.length) dp[i][0] = i
        for (j in 0..b.length) dp[0][j] = j
        for (i in 1..a.length) {
            for (j in 1..b.length) {
                val cost = if (a[i - 1] == b[j - 1]) 0 else 1
                dp[i][j] = minOf(
                    dp[i - 1][j] + 1,
                    dp[i][j - 1] + 1,
                    dp[i - 1][j - 1] + cost
                )
            }
        }
        return dp[a.length][b.length]
    }
}
