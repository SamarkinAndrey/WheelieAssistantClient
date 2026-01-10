package com.app.wheelie_assistant

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class SettingsPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

  private val fragments = mutableListOf<Fragment>()

  override fun getItemCount(): Int = 4

  override fun createFragment(position: Int): Fragment {
    val fragment = when (position) {
      0 -> BasicSettingsFragment()
      1 -> AngleSettingsFragment()
      2 -> TrendSettingsFragment()
      3 -> OtaUpdateFragment()
      else -> BasicSettingsFragment()
    }

    if (fragments.size <= position) {
      fragments.add(position, fragment)
    } else {
      fragments[position] = fragment
    }

    return fragment
  }

  fun getFragment(position: Int): Fragment? {
    return fragments.getOrNull(position)
  }
}