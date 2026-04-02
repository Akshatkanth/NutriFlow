package com.aidiettracker.ui

import android.content.Intent
import android.os.Bundle
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.aidiettracker.R
import com.aidiettracker.data.BmiCalculator
import com.aidiettracker.data.local.LocalProfileStore
import com.aidiettracker.data.model.ActivityLevel
import com.aidiettracker.data.model.BodyType
import com.aidiettracker.data.model.DietPreference
import com.aidiettracker.data.model.Goal
import com.aidiettracker.data.model.UserProfile
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import kotlin.math.round

class ProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val editName = findViewById<TextInputEditText>(R.id.edit_name)
        val editAge = findViewById<TextInputEditText>(R.id.edit_age)
        val editHeight = findViewById<TextInputEditText>(R.id.edit_height)
        val editWeight = findViewById<TextInputEditText>(R.id.edit_weight)
        val editTargetWeight = findViewById<TextInputEditText>(R.id.edit_target_weight)

        val radioActivity = findViewById<RadioGroup>(R.id.radio_activity)
        val radioGoal = findViewById<RadioGroup>(R.id.radio_goal)
        val radioDietPreference = findViewById<RadioGroup>(R.id.radio_diet_preference)

        LocalProfileStore.load(this)?.let { existing ->
            editName.setText(existing.name)
            if (existing.age > 0) editAge.setText(existing.age.toString())
            if (existing.heightCm > 0) editHeight.setText(existing.heightCm.toString())
            if (existing.weightKg > 0) editWeight.setText(existing.weightKg.toString())
            if (existing.targetWeightKg > 0) editTargetWeight.setText(existing.targetWeightKg.toString())

            radioActivity.check(
                when (existing.activityLevel) {
                    ActivityLevel.SEDENTARY -> R.id.radio_sedentary
                    ActivityLevel.LIGHT -> R.id.radio_light
                    ActivityLevel.MODERATE -> R.id.radio_moderate
                    ActivityLevel.ACTIVE -> R.id.radio_very_active
                }
            )
            radioGoal.check(
                when (existing.goal) {
                    Goal.LOSE -> R.id.radio_lose
                    Goal.MAINTAIN -> R.id.radio_maintain
                    Goal.GAIN -> R.id.radio_gain
                }
            )
            radioDietPreference.check(
                when (existing.dietPreference) {
                    DietPreference.VEG_ONLY -> R.id.radio_pref_veg
                    DietPreference.NON_VEG -> R.id.radio_pref_non_veg
                }
            )
        }

        findViewById<MaterialButton>(R.id.button_save_profile).setOnClickListener {
            val name = editName.text?.toString()?.trim().orEmpty()
            val age = editAge.text?.toString()?.trim()?.toIntOrNull() ?: 0
            val heightCm = editHeight.text?.toString()?.trim()?.toDoubleOrNull() ?: 0.0
            val weightKg = editWeight.text?.toString()?.trim()?.toDoubleOrNull() ?: 0.0
            val targetWeightKg = editTargetWeight.text?.toString()?.trim()?.toDoubleOrNull() ?: 0.0

            if (name.isBlank() || age <= 0 || heightCm <= 0.0 || weightKg <= 0.0 || targetWeightKg <= 0.0) {
                Toast.makeText(this, "Please complete name, age, height, weight and target weight", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val activityLevel = when (radioActivity.checkedRadioButtonId) {
                R.id.radio_sedentary -> ActivityLevel.SEDENTARY
                R.id.radio_light -> ActivityLevel.LIGHT
                R.id.radio_very_active -> ActivityLevel.ACTIVE
                else -> ActivityLevel.MODERATE
            }
            val goal = when (radioGoal.checkedRadioButtonId) {
                R.id.radio_lose -> Goal.LOSE
                R.id.radio_gain -> Goal.GAIN
                else -> Goal.MAINTAIN
            }
            val dietPreference = when (radioDietPreference.checkedRadioButtonId) {
                R.id.radio_pref_veg -> DietPreference.VEG_ONLY
                else -> DietPreference.NON_VEG
            }

            val rawBmi = BmiCalculator.calculate(weightKg = weightKg, heightCm = heightCm)
            val bmi = round(rawBmi * 10.0) / 10.0
            val bmiCategory = BmiCalculator.categoryFor(bmi)

            val profile = UserProfile(
                uid = FirebaseAuth.getInstance().currentUser?.uid.orEmpty(),
                name = name,
                age = age,
                heightCm = heightCm,
                weightKg = weightKg,
                targetWeightKg = targetWeightKg,
                bodyType = BodyType.MESOMORPH,
                dietPreference = dietPreference,
                activityLevel = activityLevel,
                goal = goal,
                bmi = bmi,
                bmiCategory = bmiCategory
            )
            LocalProfileStore.save(this, profile)

            Toast.makeText(this, "Profile saved. BMI: $bmi", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, DashboardActivity::class.java))
            finish()
        }
    }
}
