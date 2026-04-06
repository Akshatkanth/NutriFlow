package com.aidiettracker.ui

import android.app.Activity
import android.content.Intent
import android.view.View
import com.aidiettracker.data.local.LocalProfileStore
import com.google.firebase.auth.FirebaseAuth

fun <T : Activity> Activity.startActivitySmooth(target: Class<T>) {
    startActivity(Intent(this, target))
    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
}

fun <T : Activity> Activity.startTabActivitySmooth(target: Class<T>) {
    val intent = Intent(this, target).apply {
        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
    }
    startActivity(intent)
    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
}

fun Activity.routeAfterAuth() {
    val currentUser = FirebaseAuth.getInstance().currentUser
    if (currentUser == null) {
        startActivitySmooth(AuthEntryActivity::class.java)
        finish()
        return
    }

    val hasProfile = LocalProfileStore.hasCompletedOnboarding(this, currentUser.uid)
    if (hasProfile) {
        startActivitySmooth(DashboardActivity::class.java)
    } else {
        val intent = Intent(this, ProfileActivity::class.java).apply {
            putExtra(ProfileActivity.EXTRA_FORCE_ONBOARDING, true)
        }
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
    finish()
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

