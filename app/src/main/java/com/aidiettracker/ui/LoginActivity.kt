package com.aidiettracker.ui

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AppCompatActivity
import com.aidiettracker.R
import com.google.android.material.button.MaterialButton
import android.widget.EditText
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        findViewById<View>(R.id.button_back)?.setOnClickListener { finish() }

        val emailField = findViewById<EditText>(R.id.edit_email)
        val passwordField = findViewById<EditText>(R.id.edit_password)
        val passwordUnderline = findViewById<View>(R.id.view_password_underline)

        val underlineTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit

            override fun afterTextChanged(s: Editable?) {
                val lineColor = if (s.isNullOrBlank()) R.color.auth_divider else R.color.onboarding_cta_green
                passwordUnderline.setBackgroundColor(ContextCompat.getColor(this@LoginActivity, lineColor))
            }
        }
        passwordField.addTextChangedListener(underlineTextWatcher)
        underlineTextWatcher.afterTextChanged(passwordField.text)

        findViewById<MaterialButton>(R.id.button_login).setOnClickListener {
            val email = emailField.text?.toString()?.trim().orEmpty()
            val password = passwordField.text?.toString()?.trim().orEmpty()

            if (email.isBlank() || password.isBlank()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    startActivity(Intent(this, DashboardActivity::class.java))
                    finish()
                }
                .addOnFailureListener { error ->
                    Toast.makeText(this, error.message ?: "Login failed", Toast.LENGTH_LONG).show()
                }
        }

        findViewById<TextView>(R.id.button_register).setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    override fun onStart() {
        super.onStart()
        if (FirebaseAuth.getInstance().currentUser != null) {
            startActivity(Intent(this, DashboardActivity::class.java))
            finish()
        }
    }
}
