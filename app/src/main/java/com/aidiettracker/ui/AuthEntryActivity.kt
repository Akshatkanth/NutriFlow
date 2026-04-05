package com.aidiettracker.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.aidiettracker.R
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth

class AuthEntryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth_entry)

        findViewById<MaterialButton>(R.id.button_create_account).setOnClickListener {
            startActivitySmooth(RegisterActivity::class.java)
        }
        findViewById<MaterialButton>(R.id.button_login).setOnClickListener {
            startActivitySmooth(LoginActivity::class.java)
        }

        findViewById<MaterialButton>(R.id.button_create_account).attachTapFeedback()
        findViewById<MaterialButton>(R.id.button_login).attachTapFeedback()
    }

    override fun onStart() {
        super.onStart()
        if (FirebaseAuth.getInstance().currentUser != null) {
            startActivitySmooth(DashboardActivity::class.java)
            finish()
        }
    }
}

