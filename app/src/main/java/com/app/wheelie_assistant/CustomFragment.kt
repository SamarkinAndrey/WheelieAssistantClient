package com.app.wheelie_assistant

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

abstract class CustomFragment : Fragment() {
  abstract fun getFragmentID(): Int

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(getFragmentID(), container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    onInit(view)
  }

  abstract fun onInit(view: View)
}