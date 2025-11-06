package com.app.wheelie_assistant

import androidx.fragment.app.Fragment

abstract class BaseSettingsFragment : Fragment() {
  abstract fun setSettings(settings: Settings)
  abstract fun getSettings(settings: Settings)
}