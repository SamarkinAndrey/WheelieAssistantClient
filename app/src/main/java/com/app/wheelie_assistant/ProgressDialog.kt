package com.app.wheelie_assistant

import androidx.appcompat.app.AlertDialog
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import com.google.android.material.dialog.MaterialAlertDialogBuilder

object ProgressDialogManager {

  private var progressDialog: ProgressDialog? = null
  private var timeoutHandler: Handler? = null
  private var timeoutRunnable: Runnable? = null
  private var onCancelListener: (() -> Unit)? = null

  fun start(
    context: Context,
    message: String = "Загрузка...",
    timeoutSeconds: Long = 0,
    onCancel: (() -> Unit)? = null
  ) {
    progressDialog?.dismiss()
    cancelTimeout()

    progressDialog = ProgressDialog(context, onCancel != null)
    progressDialog?.show(message)

    this.onCancelListener = onCancel

    if (timeoutSeconds > 0) {
      timeoutHandler = Handler(Looper.getMainLooper())
      timeoutRunnable = Runnable {
        if (isShowing()) {
          finish()
        }
      }
      timeoutHandler?.postDelayed(timeoutRunnable!!, timeoutSeconds * 1000)
    }
  }

  fun set(progressValue: Int, message: String? = null) {
    progressDialog?.update(progressValue, message)
  }

  fun finish() {
    cancelTimeout()
    progressDialog?.dismiss()
    progressDialog = null
    onCancelListener = null
  }

  fun isShowing(): Boolean {
    return progressDialog?.isShowing() ?: false
  }

  fun performCancel() {
    onCancelListener?.invoke()
    finish()
  }

  private fun cancelTimeout() {
    timeoutRunnable?.let {
      timeoutHandler?.removeCallbacks(it)
    }
    timeoutHandler = null
    timeoutRunnable = null
  }
}

fun progressStart(
  context: Context,
  message: String = "Загрузка...",
  timeoutSeconds: Long = 0,
  onCancel: (() -> Unit)? = null
) {
  ProgressDialogManager.start(context, message, timeoutSeconds, onCancel)
}

fun progressSet(progressValue: Int, message: String? = null) {
  ProgressDialogManager.set(progressValue, message)
}

fun progressFinish() {
  ProgressDialogManager.finish()
}

fun progressIsShowing(): Boolean {
  return ProgressDialogManager.isShowing()
}

class ProgressDialog(context: Context, private val isCancelable: Boolean) {

  private val dialog: AlertDialog
  private val progressBar: ProgressBar
  private val messageText: TextView
  private val cancelButton: Button

  init {
    val view = LayoutInflater.from(context).inflate(R.layout.dialog_progress, null)
    progressBar = view.findViewById(R.id.progressBar)
    messageText = view.findViewById(R.id.messageTextView)
    cancelButton = view.findViewById(R.id.cancelButton)

    dialog = MaterialAlertDialogBuilder(context)
      .setView(view)
      .setCancelable(false)
      .create()

    setupCancelButton()
  }

  private fun setupCancelButton() {
    if (isCancelable) {
      cancelButton.visibility = android.view.View.VISIBLE
      cancelButton.setOnClickListener {
        ProgressDialogManager.performCancel()
      }
    } else {
      cancelButton.visibility = android.view.View.GONE
    }
  }

  fun show(message: String = "Загрузка...") {
    messageText.text = message
    progressBar.progress = 0
    dialog.show()
  }

  fun update(progress: Int, message: String? = null) {
    progressBar.progress = progress.coerceIn(0, 100)
    message?.let { messageText.text = it }
  }

  fun dismiss() {
    dialog.dismiss()
  }

  fun isShowing() = dialog.isShowing
}