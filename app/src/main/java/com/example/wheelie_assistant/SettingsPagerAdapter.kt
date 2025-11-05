package com.app.wheelie_assistant

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class SettingsPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

  override fun getItemCount(): Int = 4

  override fun createFragment(position: Int): Fragment {
    return when (position) {
      0 -> BasicSettingsFragment()
      1 -> AngularSettingsFragment()
      2 -> VoltageSettingsFragment()
      3 -> SpeedSettingsFragment()
      else -> BasicSettingsFragment()
    }
  }
}