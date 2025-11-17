package com.app.wheelie_assistant

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout

abstract class SettingsFragment : CustomFragment() {
  protected var readOnly = false

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    App.settingsActivity?.let {
      val scrollLayout = view.findViewById<LinearLayout>(R.id.scroll_layout)
      val saveLayout = it.getSaveLayout()

      saveLayout.post {
        scrollLayout.setPadding(
          scrollLayout.paddingStart,
          scrollLayout.paddingTop,
          scrollLayout.paddingEnd,
          saveLayout.height
        )
      }
    }

    super.onViewCreated(view, savedInstanceState)

    loadSettings()
  }

  protected abstract fun onLoadSettings(settings: Settings)
  protected abstract fun onSaveSettings(settings: Settings)
  protected abstract fun onUpdateTextValues()

  fun loadSettings(settings: Settings? = null) {
    onLoadSettings(settings?: SettingsManager.currentSettings)
    onUpdateTextValues()
  }

  fun saveSettings(settings: Settings? = null) {
    onSaveSettings(settings?: SettingsManager.currentSettings)
  }

  fun readOnly(): Boolean = readOnly

  fun updateTextValues() {
    onUpdateTextValues()
  }
}