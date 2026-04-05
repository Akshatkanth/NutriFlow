package com.aidiettracker.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.aidiettracker.DietTrackerActivity
import com.aidiettracker.R
import com.aidiettracker.data.local.LocalProfileStore
import com.aidiettracker.data.model.ActivityLevel
import com.aidiettracker.data.model.BodyType
import com.aidiettracker.data.model.DietPreference
import com.aidiettracker.data.model.Goal
import java.util.Locale

class ProfilePageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_page)

        bindNavigation()
        bindActions()
        bindProfile()
    }

    override fun onResume() {
        super.onResume()
        bindProfile()
    }

    private fun bindActions() {
        findViewById<View>(R.id.button_edit_profile).setOnClickListener {
            startActivitySmooth(ProfileActivity::class.java)
        }
        findViewById<View>(R.id.button_complete_profile).setOnClickListener {
            startActivitySmooth(ProfileActivity::class.java)
        }

        findViewById<View>(R.id.button_edit_profile).attachTapFeedback()
        findViewById<View>(R.id.button_complete_profile).attachTapFeedback()
    }

    private fun bindNavigation() {
        findViewById<LinearLayout>(R.id.nav_home).setOnClickListener {
            startActivitySmooth(DashboardActivity::class.java)
        }
        findViewById<LinearLayout>(R.id.nav_view_plan).setOnClickListener {
            startActivitySmooth(DietPlanActivity::class.java)
        }
        findViewById<LinearLayout>(R.id.nav_track_diet).setOnClickListener {
            startActivity(Intent(this, DietTrackerActivity::class.java))
        }
        findViewById<LinearLayout>(R.id.nav_profile).setOnClickListener {
            findViewById<android.widget.ScrollView>(R.id.profile_page_scroll).smoothScrollTo(0, 0)
        }
        findViewById<FrameLayout>(R.id.nav_quick_actions).setOnClickListener {
            startActivitySmooth(ProfileActivity::class.java)
        }

        findViewById<LinearLayout>(R.id.nav_home).attachTapFeedback()
        findViewById<LinearLayout>(R.id.nav_view_plan).attachTapFeedback()
        findViewById<LinearLayout>(R.id.nav_track_diet).attachTapFeedback()
        findViewById<LinearLayout>(R.id.nav_profile).attachTapFeedback()
        findViewById<FrameLayout>(R.id.nav_quick_actions).attachTapFeedback()
    }

    private fun bindProfile() {
        val profile = LocalProfileStore.load(this)
        val completeProfileCta = findViewById<View>(R.id.button_complete_profile)
        val editProfileButton = findViewById<View>(R.id.button_edit_profile)
        val subtitle = findViewById<TextView>(R.id.text_profile_subtitle)

        if (profile == null) {
            subtitle.text = "No profile found yet. Complete the onboarding questions to personalize your plan."
            completeProfileCta.visibility = View.VISIBLE
            editProfileButton.visibility = View.GONE

            setText(R.id.text_profile_name, "Name: -")
            setText(R.id.text_profile_age, "Age: -")
            setText(R.id.text_profile_goal, "Goal: -")
            setText(R.id.text_profile_activity, "Activity: -")
            setText(R.id.text_profile_height, "Height: -")
            setText(R.id.text_profile_weight, "Weight: -")
            setText(R.id.text_profile_target_weight, "Target weight: -")
            setText(R.id.text_profile_diet, "Diet preference: -")
            setText(R.id.text_profile_body_type, "Body type: -")
            setText(R.id.text_profile_bmi, "BMI: -")
            return
        }

        subtitle.text = "Your onboarding answers are saved here."
        completeProfileCta.visibility = View.GONE
        editProfileButton.visibility = View.VISIBLE

        setText(R.id.text_profile_name, "Name: ${profile.name}")
        setText(R.id.text_profile_age, "Age: ${profile.age}")
        setText(R.id.text_profile_goal, "Goal: ${formatGoal(profile.goal)}")
        setText(R.id.text_profile_activity, "Activity: ${formatActivity(profile.activityLevel)}")
        setText(R.id.text_profile_height, "Height: ${profile.heightCm.toInt()} cm")
        setText(R.id.text_profile_weight, "Weight: ${oneDecimal(profile.weightKg)} kg")
        setText(R.id.text_profile_target_weight, "Target weight: ${oneDecimal(profile.targetWeightKg)} kg")
        setText(R.id.text_profile_diet, "Diet preference: ${formatDiet(profile.dietPreference)}")
        setText(R.id.text_profile_body_type, "Body type: ${formatBodyType(profile.bodyType)}")
        setText(
            R.id.text_profile_bmi,
            "BMI: ${oneDecimal(profile.bmi)} (${profile.bmiCategory.name.lowercase(Locale.getDefault()).replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }})"
        )
    }

    private fun setText(id: Int, value: String) {
        findViewById<TextView>(id).text = value
    }

    private fun oneDecimal(value: Double): String = String.format(Locale.getDefault(), "%.1f", value)

    private fun formatGoal(goal: Goal): String = when (goal) {
        Goal.LOSE -> "Lose weight"
        Goal.MAINTAIN -> "Maintain weight"
        Goal.GAIN -> "Gain weight"
    }

    private fun formatActivity(activity: ActivityLevel): String = when (activity) {
        ActivityLevel.SEDENTARY -> "Sedentary"
        ActivityLevel.LIGHT -> "Light"
        ActivityLevel.MODERATE -> "Moderately active"
        ActivityLevel.ACTIVE -> "Very active"
    }

    private fun formatDiet(dietPreference: DietPreference): String = when (dietPreference) {
        DietPreference.VEG_ONLY -> "Vegetarian"
        DietPreference.NON_VEG -> "Non-vegetarian"
    }

    private fun formatBodyType(bodyType: BodyType): String = when (bodyType) {
        BodyType.ECTOMORPH -> "Ectomorph"
        BodyType.MESOMORPH -> "Mesomorph"
        BodyType.ENDOMORPH -> "Endomorph"
    }
}

