package com.app.wheelie_assistant

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.fragment.app.Fragment

abstract class BaseSettingsFragment : Fragment() {
  abstract fun setSettings(settings: Settings)
  abstract fun getSettings(settings: Settings)
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
  }
}