package com.aidiettracker.ui

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.LinkMovementMethod
import android.widget.Toast
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AppCompatActivity
import com.aidiettracker.R
import com.google.android.material.button.MaterialButton
import android.widget.EditText
import com.google.firebase.auth.FirebaseAuth

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()

        findViewById<View>(R.id.button_back)?.setOnClickListener { finish() }
        findViewById<TextView>(R.id.button_login)?.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        val emailField = findViewById<EditText>(R.id.edit_email)
        val passwordField = findViewById<EditText>(R.id.edit_password)
        val nameField = findViewById<EditText>(R.id.edit_name)
        val passwordUnderline = findViewById<View>(R.id.view_password_underline)

        val underlineTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit

            override fun afterTextChanged(s: Editable?) {
                val lineColor = if (s.isNullOrBlank()) R.color.auth_divider else R.color.onboarding_cta_green
                passwordUnderline.setBackgroundColor(ContextCompat.getColor(this@RegisterActivity, lineColor))
            }
        }
        passwordField.addTextChangedListener(underlineTextWatcher)
        underlineTextWatcher.afterTextChanged(passwordField.text)

        findViewById<TextView?>(R.id.text_terms)?.movementMethod = LinkMovementMethod.getInstance()

        findViewById<MaterialButton>(R.id.button_create_account).setOnClickListener {
            val email = emailField.text.toString().trim()
            val password = passwordField.text.toString().trim()
            val name = nameField.text.toString().trim()

            if (email.isEmpty() || password.isEmpty() || name.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 8) {
                Toast.makeText(this, "Password must be at least 8 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    Toast.makeText(this, "Account created!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, ProfileActivity::class.java))
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }

    }
}
