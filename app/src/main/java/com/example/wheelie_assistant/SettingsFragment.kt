package com.app.wheelie_assistant

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment

abstract class SettingsFragment : Fragment() {
  abstract fun getFragmentID(): Int

  abstract fun loadSettings(settings: Settings)
  abstract fun saveSettings(settings: Settings)

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(getFragmentID(), container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    val scrollLayout = view.findViewById<LinearLayout>(R.id.scroll_layout)
    val activity = App.settingsActivity

    activity?.let {
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

    onInit(view)
    loadSettings(SettingsManager.currentSettings)
    updateTextValues()
  }

  abstract fun onInit(view: View)
  abstract fun updateTextValues()
}