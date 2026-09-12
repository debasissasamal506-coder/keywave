package com.example.data.clipboard

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "clipboard_items")
data class ClipboardEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isPinned: Boolean = false
)
