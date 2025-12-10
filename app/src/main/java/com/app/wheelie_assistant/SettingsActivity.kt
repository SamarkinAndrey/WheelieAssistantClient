package com.app.wheelie_assistant

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.button.MaterialButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.view.isVisible

class SettingsActivity : AppCompatActivity() {

  private lateinit var viewPager: ViewPager2
  private lateinit var tabLayout: TabLayout
  private lateinit var saveButton: MaterialButton
  private lateinit var backButton: AppCompatImageButton
  private lateinit var saveLayout: LinearLayout

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.settings_activity)

    App.settingsActivity = this

    initViews()
    setupViewPager()
    setupTabs()
    setupClickListeners()
  }

  override fun onDestroy() {
    super.onDestroy()

    if (App.settingsActivity === this)
      App.settingsActivity = null
  }

  private fun initViews() {
    viewPager = findViewById(R.id.view_pager)
    tabLayout = findViewById(R.id.tab_layout)
    saveButton = findViewById(R.id.save_button)
    backButton = findViewById(R.id.back_button)

    saveLayout = findViewById(R.id.save_layout)
  }

  private fun setupViewPager() {
    val adapter = SettingsPagerAdapter(this)
    viewPager.adapter = adapter

    viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
      override fun onPageSelected(position: Int) {
        val fragment = adapter.getFragment(position)

        saveButton.isVisible = fragment is ICanSave
      }
    })
  }

  private fun setupTabs() {
    TabLayoutMediator(tabLayout, viewPager) { tab, position ->
      tab.text = when (position) {
        0 -> "Основные"
        1 -> "Угловые"
        2 -> "Напряжение"
        3 -> "Скорость"
        4 -> "Прошивка"
        else -> "Раздел"
      }
    }.attach()
  }

  private fun setupClickListeners() {
    saveButton.setOnClickListener {
      saveSettings()

//      if (saveSettings())
//        finish()
    }

    backButton.setOnClickListener {
      finish()
    }
  }

  fun loadSettings() {
    supportFragmentManager.fragments.forEach { fragment ->
      if (fragment is ICanLoad)
        fragment.loadSettings()
    }
  }

  fun saveSettings(): Boolean {
    val settings  = SettingsManager.settings

    supportFragmentManager.fragments.forEach { fragment ->
      if (fragment is ICanSave)
        fragment.saveSettings(settings)
    }

    if (!settings.validate()) {
      Toast.makeText(this, "Ошибка валидации настроек", Toast.LENGTH_SHORT).show()
      return false
    }

    SettingsManager.settings = settings

    val parser = JsonParamParser()
    SettingsManager.saveTo(parser)
    saveCallback?.invoke(parser)

    return true
  }

  companion object {
    private var saveCallback: ((JsonParamParser) -> Unit)? = null

    fun setSaveCallback(callback: (JsonParamParser) -> Unit) {
      saveCallback = callback
    }
  }

  fun getSaveLayout(): LinearLayout {
    return saveLayout
  }
}