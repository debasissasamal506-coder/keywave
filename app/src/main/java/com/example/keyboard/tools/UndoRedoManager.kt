package com.example.keyboard.tools

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UndoRedoManager(private val maxHistory: Int = 50) {
    private val history = mutableListOf<String>()
    private var currentIndex = -1
    private var isUndoingOrRedoing = false

    private val _canUndo = MutableStateFlow(false)
    val canUndo: StateFlow<Boolean> = _canUndo.asStateFlow()

    private val _canRedo = MutableStateFlow(false)
    val canRedo: StateFlow<Boolean> = _canRedo.asStateFlow()

    fun pushState(text: String) {
        if (isUndoingOrRedoing) return
        if (currentIndex >= 0 && currentIndex < history.size && history[currentIndex] == text) {
            return
        }
        // Truncate any redo branch ahead of currentIndex
        if (currentIndex < history.size - 1) {
            val toRemove = history.size - 1 - currentIndex
            repeat(toRemove) {
                if (history.isNotEmpty()) history.removeAt(history.size - 1)
            }
        }
        history.add(text)
        if (history.size > maxHistory) {
            history.removeAt(0)
        }
        currentIndex = history.size - 1
        updateFlags()
    }

    fun undo(): String? {
        if (currentIndex > 0) {
            isUndoingOrRedoing = true
            currentIndex--
            val text = history[currentIndex]
            updateFlags()
            isUndoingOrRedoing = false
            return text
        }
        return null
    }

    fun redo(): String? {
        if (currentIndex < history.size - 1) {
            isUndoingOrRedoing = true
            currentIndex++
            val text = history[currentIndex]
            updateFlags()
            isUndoingOrRedoing = false
            return text
        }
        return null
    }

    fun clear() {
        history.clear()
        currentIndex = -1
        updateFlags()
    }

    private fun updateFlags() {
        _canUndo.value = currentIndex > 0
        _canRedo.value = currentIndex < history.size - 1 && history.isNotEmpty()
    }
}
