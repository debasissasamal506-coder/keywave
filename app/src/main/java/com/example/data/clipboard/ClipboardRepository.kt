package com.example.data.clipboard

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ClipboardRepository(
    private val context: Context,
    private val clipboardDao: ClipboardDao
) {
    private val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
    private val scope = CoroutineScope(Dispatchers.IO)

    // In-memory fallback for when Save Clipboard History is turned OFF
    private val _inMemoryItems = MutableStateFlow<List<ClipboardEntity>>(emptyList())
    val inMemoryItems: Flow<List<ClipboardEntity>> = _inMemoryItems.asStateFlow()

    fun getAllItems(saveHistoryEnabled: Boolean): Flow<List<ClipboardEntity>> {
        return if (saveHistoryEnabled) {
            clipboardDao.getAllItems()
        } else {
            inMemoryItems
        }
    }

    fun copyToClipboard(text: String) {
        try {
            val clip = ClipData.newPlainText("KeyWave", text)
            clipboardManager?.setPrimaryClip(clip)
        } catch (_: Exception) {}
    }

    fun syncFromSystemClipboard(saveHistoryEnabled: Boolean) {
        scope.launch {
            try {
                val clip = clipboardManager?.primaryClip
                if (clip != null && clip.itemCount > 0) {
                    val text = clip.getItemAt(0)?.text?.toString()?.trim()
                    if (!text.isNullOrEmpty() && text.length < 2000) {
                        if (saveHistoryEnabled) {
                            val existing = clipboardDao.countWithText(text)
                            if (existing == 0) {
                                clipboardDao.insertItem(
                                    ClipboardEntity(
                                        text = text,
                                        timestamp = System.currentTimeMillis()
                                    )
                                )
                            }
                        } else {
                            val current = _inMemoryItems.value
                            if (current.none { it.text == text }) {
                                _inMemoryItems.value = listOf(
                                    ClipboardEntity(
                                        id = System.currentTimeMillis(),
                                        text = text,
                                        timestamp = System.currentTimeMillis()
                                    )
                                ) + current.take(15)
                            }
                        }
                    }
                }
            } catch (_: Exception) {}
        }
    }

    suspend fun insertItem(text: String, saveHistoryEnabled: Boolean) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return
        if (saveHistoryEnabled) {
            clipboardDao.insertItem(
                ClipboardEntity(
                    text = trimmed,
                    timestamp = System.currentTimeMillis()
                )
            )
        } else {
            val current = _inMemoryItems.value
            _inMemoryItems.value = listOf(
                ClipboardEntity(
                    id = System.currentTimeMillis(),
                    text = trimmed,
                    timestamp = System.currentTimeMillis()
                )
            ) + current.take(15)
        }
    }

    suspend fun togglePin(item: ClipboardEntity, saveHistoryEnabled: Boolean) {
        if (saveHistoryEnabled) {
            clipboardDao.updatePin(item.id, !item.isPinned)
        } else {
            _inMemoryItems.value = _inMemoryItems.value.map {
                if (it.id == item.id) it.copy(isPinned = !it.isPinned) else it
            }.sortedWith(compareByDescending<ClipboardEntity> { it.isPinned }.thenByDescending { it.timestamp })
        }
    }

    suspend fun deleteItem(item: ClipboardEntity, saveHistoryEnabled: Boolean) {
        if (saveHistoryEnabled) {
            clipboardDao.deleteItem(item.id)
        } else {
            _inMemoryItems.value = _inMemoryItems.value.filter { it.id != item.id }
        }
    }

    suspend fun clearUnpinned(saveHistoryEnabled: Boolean) {
        if (saveHistoryEnabled) {
            clipboardDao.clearUnpinned()
        } else {
            _inMemoryItems.value = _inMemoryItems.value.filter { it.isPinned }
        }
    }

    suspend fun clearAll(saveHistoryEnabled: Boolean) {
        if (saveHistoryEnabled) {
            clipboardDao.clearAll()
        }
        _inMemoryItems.value = emptyList()
    }

    companion object {
        @Volatile
        private var INSTANCE: ClipboardRepository? = null

        fun getInstance(context: Context): ClipboardRepository {
            return INSTANCE ?: synchronized(this) {
                val db = KeyWaveDatabase.getDatabase(context)
                INSTANCE ?: ClipboardRepository(context.applicationContext, db.clipboardDao()).also { INSTANCE = it }
            }
        }
    }
}
