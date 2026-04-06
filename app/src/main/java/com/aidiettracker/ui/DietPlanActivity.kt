package com.aidiettracker.ui

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.aidiettracker.R
import com.aidiettracker.data.NutritionGoalCalculator
import com.aidiettracker.data.ai.AiIndianSuggestor
import com.aidiettracker.data.local.LocalProfileStore
import com.google.firebase.auth.FirebaseAuth

class DietPlanActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_diet_plan)

        bindNavigation()

        renderPlanFromProfile()
    }

    override fun onResume() {
        super.onResume()
        renderPlanFromProfile()
    }

    private fun renderPlanFromProfile() {
        val profile = LocalProfileStore.load(this, FirebaseAuth.getInstance().currentUser?.uid)
        if (profile == null) {
            Toast.makeText(this, "Complete your profile first", Toast.LENGTH_SHORT).show()
            startActivitySmooth(ProfileActivity::class.java)
            finish()
            return
        }


        val goals = NutritionGoalCalculator.calculate(profile)
        val aiPlan = AiIndianSuggestor.buildDietPlan(profile, goals.caloriesTarget)
        findViewById<TextView>(R.id.tv_calories).text = "${goals.caloriesTarget} kcal"
        findViewById<TextView>(R.id.tv_protein).text = "${goals.proteinGrams}g"
        findViewById<TextView>(R.id.tv_carbs).text = "${goals.carbsGrams}g"
        findViewById<TextView>(R.id.tv_fat).text = "${goals.fatGrams}g"
        findViewById<TextView>(R.id.tv_ai_buddy_name).text = aiPlan.buddyName
        findViewById<TextView>(R.id.tv_ai_plan_summary).text =
            "Breakfast: ${aiPlan.breakfast}\nLunch: ${aiPlan.lunch}\nDinner: ${aiPlan.dinner}\nSnacks: ${aiPlan.snacks}\nTip: ${aiPlan.buddyTip}"
    }

    private fun bindNavigation() {
        findViewById<android.widget.LinearLayout>(R.id.nav_home).setOnClickListener {
            startTabActivitySmooth(DashboardActivity::class.java)
        }
        findViewById<android.widget.LinearLayout>(R.id.nav_view_plan).setOnClickListener {
            findViewById<android.widget.ScrollView>(R.id.diet_plan_scroll).smoothScrollTo(0, 0)
        }
        findViewById<android.widget.LinearLayout>(R.id.nav_track_diet).setOnClickListener {
            startTabActivitySmooth(com.aidiettracker.DietTrackerActivity::class.java)
        }
        findViewById<android.widget.LinearLayout>(R.id.nav_profile).setOnClickListener {
            startTabActivitySmooth(ProfilePageActivity::class.java)
        }
        findViewById<android.widget.FrameLayout>(R.id.nav_quick_actions).setOnClickListener {
            showQuickActions()
        }

        findViewById<android.widget.LinearLayout>(R.id.nav_home).attachTapFeedback()
        findViewById<android.widget.LinearLayout>(R.id.nav_view_plan).attachTapFeedback()
        findViewById<android.widget.LinearLayout>(R.id.nav_track_diet).attachTapFeedback()
        findViewById<android.widget.LinearLayout>(R.id.nav_profile).attachTapFeedback()
        findViewById<android.widget.FrameLayout>(R.id.nav_quick_actions).attachTapFeedback()
    }

    private fun showQuickActions() {
        val options = arrayOf("Track diet", "View plan", "Open profile", "Chat with coach")
        AlertDialog.Builder(this)
            .setTitle("Quick actions")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> startActivitySmooth(com.aidiettracker.DietTrackerActivity::class.java)
                    1 -> findViewById<android.widget.ScrollView>(R.id.diet_plan_scroll).smoothScrollTo(0, 0)
                    2 -> startActivitySmooth(ProfilePageActivity::class.java)
                    3 -> startActivitySmooth(ChatbotActivity::class.java)
                }
            }
            .show()
    }
}
