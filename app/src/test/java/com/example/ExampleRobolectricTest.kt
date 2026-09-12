package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.KeyboardPreferences
import com.example.model.KeyboardLayouts
import com.example.model.RGBMode
import com.example.model.ThemePresets
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("KeyWave", appName)
  }

  @Test
  fun `verify theme presets contains distinct themes`() {
    assertTrue(ThemePresets.ALL_THEMES.size >= 12)
    assertNotNull(ThemePresets.getThemeById("minimal_dark"))
    assertNotNull(ThemePresets.getThemeById("neon_night"))
    assertNotNull(ThemePresets.getThemeById("cyberpunk"))
    assertNotNull(ThemePresets.getThemeById("aurora"))
  }

  @Test
  fun `verify default keyboard layout rows`() {
    assertEquals(10, KeyboardLayouts.NUMBER_ROW.size)
    assertEquals(10, KeyboardLayouts.QWERTY_ROW_1.size)
    assertEquals(9, KeyboardLayouts.QWERTY_ROW_2.size)
    assertEquals(9, KeyboardLayouts.getQwertyRow3(false, false).size)
  }

  @Test
  fun `verify preferences default config`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val prefs = KeyboardPreferences.getInstance(context)
    val config = prefs.loadConfig()
    assertEquals("minimal_dark", config.themeId)
    assertEquals(RGBMode.OFF, config.rgbMode)
    assertTrue(config.keyboardSizePercent in 70..120)
    assertTrue(config.showToolbar)
    assertTrue(config.visibleToolbarTools.isNotEmpty())
  }

  @Test
  fun `verify undo redo manager operations`() {
    val manager = com.example.keyboard.tools.UndoRedoManager(maxHistory = 10)
    assertEquals(false, manager.canUndo.value)
    assertEquals(false, manager.canRedo.value)

    manager.pushState("Hello")
    assertEquals(false, manager.canUndo.value)

    manager.pushState("Hello ")
    manager.pushState("Hello World")
    assertEquals(true, manager.canUndo.value)
    assertEquals(false, manager.canRedo.value)

    val undone1 = manager.undo()
    assertEquals("Hello ", undone1)
    assertEquals(true, manager.canUndo.value)
    assertEquals(true, manager.canRedo.value)

    val redone = manager.redo()
    assertEquals("Hello World", redone)
    assertEquals(true, manager.canUndo.value)
    assertEquals(false, manager.canRedo.value)
  }

  @Test
  fun `verify toolbar tools list and defaults`() {
    val defaultTools = com.example.model.ToolbarTool.values()
    assertTrue(defaultTools.size >= 10)
    assertNotNull(com.example.model.ToolbarTool.valueOf("EMOJI"))
    assertNotNull(com.example.model.ToolbarTool.valueOf("CLIPBOARD"))
    assertNotNull(com.example.model.ToolbarTool.valueOf("TEXT_EDITING"))
    assertNotNull(com.example.model.ToolbarTool.valueOf("ONE_HANDED"))
    assertNotNull(com.example.model.ToolbarTool.valueOf("RESIZE"))
  }

  @Test
  fun `verify haptic manager cancel stops vibration safely`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val hapticManager = com.example.haptics.KeyWaveHapticManager(context)
    // Testing vibration trigger and immediate cancellation
    hapticManager.vibrateKeypress(com.example.model.HapticStrength.STRONG)
    hapticManager.cancel()
    // Verify no exception thrown and cancel executes cleanly
  }
}

