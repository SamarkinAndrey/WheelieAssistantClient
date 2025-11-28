package com.app.wheelie_assistant

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout

interface ICanLoad {
  fun onLoadSettings(settings: Settings)

  fun loadSettings(settings: Settings? = null) {
    onLoadSettings(settings ?: SettingsManager.settings)
    onUpdateTextValues()
  }

  fun onUpdateTextValues() {
    // TODO
  }

  fun updateTextValues() {
    onUpdateTextValues()
  }
}

interface ICanSave {
  fun onSaveSettings(settings: Settings)

  fun saveSettings(settings: Settings? = null) {
    onSaveSettings(settings ?: SettingsManager.settings)
  }
}

abstract class InfoFragment : CustomFragment(), ICanLoad {
  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    loadSettings()
  }
}

abstract class SettingsFragment : InfoFragment(), ICanSave {
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
  }
}