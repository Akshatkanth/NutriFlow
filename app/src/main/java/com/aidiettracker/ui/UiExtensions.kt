package com.aidiettracker.ui

import android.app.Activity
import android.content.Intent
import android.view.View

fun <T : Activity> Activity.startActivitySmooth(target: Class<T>) {
    startActivity(Intent(this, target))
    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
}

fun Activity.finishWithSmoothTransition() {
    finish()
    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
}

fun View.attachTapFeedback() {
    setOnTouchListener { v, event ->
        when (event.actionMasked) {
            android.view.MotionEvent.ACTION_DOWN -> v.animate().alpha(0.85f).setDuration(80).start()
            android.view.MotionEvent.ACTION_CANCEL,
            android.view.MotionEvent.ACTION_UP -> v.animate().alpha(1f).setDuration(120).start()
        }
        false
    }
}

