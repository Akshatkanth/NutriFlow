package com.aidiettracker.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.aidiettracker.R
import com.google.firebase.auth.FirebaseAuth

class OnboardingSplashActivity : AppCompatActivity() {

    private val handler = Handler(Looper.getMainLooper())
    private val proceedRunnable = Runnable { openAuthEntry() }
    private var navigated = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding_splash)

        findViewById<View>(R.id.tap_ring).setOnClickListener {
            openAuthEntry()
        }

        handler.postDelayed(proceedRunnable, AUTO_FORWARD_DELAY_MS)
    }

    override fun onDestroy() {
        handler.removeCallbacks(proceedRunnable)
        super.onDestroy()
    }

    override fun onStart() {
        super.onStart()
        if (FirebaseAuth.getInstance().currentUser != null) {
            routeAfterAuth()
        }
    }

    private fun openAuthEntry() {
        if (navigated) return
        navigated = true
        startActivitySmooth(AuthEntryActivity::class.java)
        finish()
    }

    companion object {
        private const val AUTO_FORWARD_DELAY_MS = 1800L
    }
}

