package com.example.data.clipboard

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ClipboardDao {
    @Query("SELECT * FROM clipboard_items ORDER BY isPinned DESC, timestamp DESC")
    fun getAllItems(): Flow<List<ClipboardEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ClipboardEntity): Long

    @Query("DELETE FROM clipboard_items WHERE id = :id")
    suspend fun deleteItem(id: Long)

    @Query("UPDATE clipboard_items SET isPinned = :isPinned WHERE id = :id")
    suspend fun updatePin(id: Long, isPinned: Boolean)

    @Query("DELETE FROM clipboard_items WHERE isPinned = 0")
    suspend fun clearUnpinned()

    @Query("DELETE FROM clipboard_items")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM clipboard_items WHERE text = :text")
    suspend fun countWithText(text: String): Int
}
