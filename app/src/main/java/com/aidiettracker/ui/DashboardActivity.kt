package com.aidiettracker.ui

import android.content.Intent
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView
import com.aidiettracker.DietTrackerActivity
import com.aidiettracker.R
import com.aidiettracker.data.NutritionGoalCalculator
import com.aidiettracker.data.local.LocalProfileStore
import com.aidiettracker.data.model.BmiCategory
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DashboardActivity : AppCompatActivity() {

    private data class DailyTrackingStats(
        val calories: Int,
        val protein: Int,
        val carbs: Int,
        val fat: Int,
        val waterGlasses: Int,
        val sleepHours: Int,
        val mealsLogged: Int,
        val streak: Int
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        bindNavigation()
        bindLiveSummary()
    }

    override fun onResume() {
        super.onResume()
        bindLiveSummary()
    }

    private fun bindNavigation() {
        findViewById<LinearLayout>(R.id.nav_home).setOnClickListener {
            findViewById<NestedScrollView>(R.id.dashboard_scroll).smoothScrollTo(0, 0)
        }

        findViewById<LinearLayout>(R.id.nav_view_plan).setOnClickListener {
            startActivitySmooth(DietPlanActivity::class.java)
        }

        findViewById<LinearLayout>(R.id.nav_track_diet).setOnClickListener {
            startActivitySmooth(DietTrackerActivity::class.java)
        }

        findViewById<LinearLayout>(R.id.nav_profile).setOnClickListener {
            startActivitySmooth(ProfilePageActivity::class.java)
        }

        findViewById<FrameLayout>(R.id.nav_quick_actions).setOnClickListener {
            showQuickActions()
        }

        findViewById<TextView>(R.id.text_chat_shortcut).setOnClickListener {
            startActivitySmooth(ChatbotActivity::class.java)
        }

        findViewById<LinearLayout>(R.id.nav_home).attachTapFeedback()
        findViewById<LinearLayout>(R.id.nav_view_plan).attachTapFeedback()
        findViewById<LinearLayout>(R.id.nav_track_diet).attachTapFeedback()
        findViewById<LinearLayout>(R.id.nav_profile).attachTapFeedback()
        findViewById<FrameLayout>(R.id.nav_quick_actions).attachTapFeedback()
        findViewById<TextView>(R.id.text_chat_shortcut).attachTapFeedback()
    }

    private fun showQuickActions() {
        val options = arrayOf("Track diet", "View plan", "Open profile", "Chat with coach")
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Quick actions")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> startActivitySmooth(DietTrackerActivity::class.java)
                    1 -> startActivitySmooth(DietPlanActivity::class.java)
                    2 -> startActivitySmooth(ProfilePageActivity::class.java)
                    3 -> startActivitySmooth(ChatbotActivity::class.java)
                }
            }
            .show()
    }

    private fun bindLiveSummary() {
        val profile = LocalProfileStore.load(this)
        if (profile == null) {
            renderEmptySummary()
            return
        }

        val goals = NutritionGoalCalculator.calculate(profile)
        val today = loadTodayTrackingStats()
        val caloriesRemaining = (goals.caloriesTarget - today.calories).coerceAtLeast(0)
        val caloriesProgress = scaledProgress(today.calories, goals.caloriesTarget)
        val caloriesPercent = percentageValue(today.calories, goals.caloriesTarget)
        val waterProgress = macroProgress(today.waterGlasses, 8)
        val sleepProgress = macroProgress(today.sleepHours * 10, 80)
        val proteinGap = (goals.proteinGrams - today.protein).coerceAtLeast(0)
        val waterGap = (8 - today.waterGlasses).coerceAtLeast(0)

        findViewById<TextView>(R.id.text_bmi).text = String.format(Locale.getDefault(), "BMI %.1f", profile.bmi)
        findViewById<TextView>(R.id.text_bmi_category).text = formatBmiCategory(profile.bmiCategory)
        findViewById<TextView>(R.id.text_dashboard_focus).text = buildDashboardFocus(caloriesRemaining, today.mealsLogged, today.waterGlasses)

        findViewById<TextView>(R.id.text_calories_consumed).text = today.calories.toString()
        findViewById<TextView>(R.id.text_calories_target).text = goals.caloriesTarget.toString()
        findViewById<TextView>(R.id.text_calories_remaining).text = caloriesRemaining.toString()
        findViewById<TextView>(R.id.text_calorie_percent).text = String.format(Locale.getDefault(), "%.1f%% of goal", caloriesPercent)
        findViewById<TextView>(R.id.text_steps).text = today.streak.toString()
        findViewById<TextView>(R.id.text_steps_label).text = "STREAK"
        findViewById<TextView>(R.id.text_sleep_value).text = String.format(Locale.getDefault(), "%d h", today.sleepHours)
        findViewById<TextView>(R.id.text_meals_logged_value).text = String.format(Locale.getDefault(), "%d meals", today.mealsLogged)
        findViewById<TextView>(R.id.text_water_status_value).text = String.format(Locale.getDefault(), "%d / 8", today.waterGlasses)
        findViewById<ProgressBar>(R.id.progress_calories_circle).progress = caloriesProgress
        findViewById<ProgressBar>(R.id.progress_macro_calories).progress = caloriesProgress
        findViewById<ProgressBar>(R.id.progress_macro_carbs).progress = macroProgress(today.carbs, goals.carbsGrams)
        findViewById<ProgressBar>(R.id.progress_macro_protein).progress = macroProgress(today.protein, goals.proteinGrams)
        findViewById<ProgressBar>(R.id.progress_macro_fat).progress = macroProgress(today.fat, goals.fatGrams)
        findViewById<ProgressBar>(R.id.progress_sleep_circle).progress = sleepProgress
        findViewById<TextView>(R.id.text_calories_goal_value).text = String.format(Locale.getDefault(), "%d kcal", goals.caloriesTarget)
        findViewById<TextView>(R.id.text_macro_carbs_value).text = String.format(Locale.getDefault(), "%d/%dg", today.carbs, goals.carbsGrams)
        findViewById<TextView>(R.id.text_macro_fat_value).text = String.format(Locale.getDefault(), "%d/%dg", today.fat, goals.fatGrams)
        findViewById<TextView>(R.id.text_macro_protein_value).text = String.format(Locale.getDefault(), "%d/%dg", today.protein, goals.proteinGrams)
        findViewById<TextView>(R.id.text_water_value).text = String.format(Locale.getDefault(), "%d / 8 glasses", today.waterGlasses)
        findViewById<ProgressBar>(R.id.progress_water).progress = waterProgress

        bindSuggestions(caloriesRemaining, proteinGap, waterGap, today.mealsLogged)
        bindFacts()
    }

    private fun renderEmptySummary() {
        findViewById<TextView>(R.id.text_bmi).text = "BMI --"
        findViewById<TextView>(R.id.text_bmi_category).text = "Complete profile"
        findViewById<TextView>(R.id.text_dashboard_focus).text = "Complete your profile to unlock custom goals"
        findViewById<TextView>(R.id.text_calories_consumed).text = "0"
        findViewById<TextView>(R.id.text_calories_target).text = "0"
        findViewById<TextView>(R.id.text_calories_remaining).text = "0"
        findViewById<TextView>(R.id.text_calorie_percent).text = "0.0% of goal"
        findViewById<TextView>(R.id.text_steps).text = "0"
        findViewById<TextView>(R.id.text_steps_label).text = "STREAK"
        findViewById<TextView>(R.id.text_sleep_value).text = "0 h"
        findViewById<TextView>(R.id.text_meals_logged_value).text = "0 meals"
        findViewById<TextView>(R.id.text_water_status_value).text = "0 / 8"
        findViewById<TextView>(R.id.text_macro_protein_value).text = "0/0g"
        findViewById<TextView>(R.id.text_macro_carbs_value).text = "0/0g"
        findViewById<TextView>(R.id.text_macro_fat_value).text = "0/0g"
        findViewById<ProgressBar>(R.id.progress_macro_carbs).progress = 0
        findViewById<ProgressBar>(R.id.progress_macro_protein).progress = 0
        findViewById<ProgressBar>(R.id.progress_macro_fat).progress = 0
        findViewById<ProgressBar>(R.id.progress_calories_circle).progress = 0
        findViewById<ProgressBar>(R.id.progress_macro_calories).progress = 0
        findViewById<ProgressBar>(R.id.progress_sleep_circle).progress = 0
        findViewById<TextView>(R.id.text_calories_goal_value).text = "0 kcal"
        findViewById<TextView>(R.id.text_water_value).text = "0 / 8 glasses"
        findViewById<ProgressBar>(R.id.progress_water).progress = 0

        bindSuggestions(0, 0, 8, 0)
        bindFacts()
    }

    private fun bindSuggestions(caloriesRemaining: Int, proteinGap: Int, waterGap: Int, mealsLogged: Int) {
        val suggestions = listOf(
            when {
                caloriesRemaining <= 0 -> "You have reached your calorie goal. Keep the rest of the day light."
                caloriesRemaining < 350 -> "You are close to your goal. Keep the next meal lean and balanced."
                else -> "You still have $caloriesRemaining kcal left. Use them on a balanced meal."
            },
            when {
                proteinGap <= 0 -> "Protein is on track. Keep the next plate built around vegetables and protein."
                else -> "Add about $proteinGap g protein today to stay on target."
            },
            when {
                waterGap <= 0 -> "Hydration is on track. Keep the momentum going."
                else -> "Drink $waterGap more glass${if (waterGap == 1) "" else "es"} of water today."
            }
        )

        findViewById<TextView>(R.id.text_suggestion_one).text = suggestions[0]
        findViewById<TextView>(R.id.text_suggestion_two).text = suggestions[1]
        findViewById<TextView>(R.id.text_suggestion_three).text = if (mealsLogged <= 0) {
            "Log your first meal early so the tracker stays accurate."
        } else {
            suggestions[2]
        }
    }

    private fun bindFacts() {
        findViewById<TextView>(R.id.text_fact_one).text = "Protein helps you stay full for longer."
        findViewById<TextView>(R.id.text_fact_two).text = "Fiber-rich meals make it easier to manage hunger."
        findViewById<TextView>(R.id.text_fact_three).text = "A little planning at lunch often saves calories at night."
    }

    private fun buildDashboardFocus(caloriesRemaining: Int, mealsLogged: Int, waterGlasses: Int): String {
        return when {
            caloriesRemaining <= 0 -> "You have hit today's calorie goal. Keep the rest of the day light."
            caloriesRemaining < 350 -> "You are close to your goal. Keep the next meal balanced and simple."
            mealsLogged <= 0 -> "Start with your first meal and keep the day easy to track."
            waterGlasses < 4 -> "Hydration is the next easy win. Drink a glass of water now."
            else -> "Stay steady and keep the day balanced."
        }
    }

    private fun macroProgress(current: Int, goal: Int): Int {
        if (goal <= 0) return 0
        return ((current.toFloat() / goal.toFloat()) * 100f)
            .coerceIn(0f, 100f)
            .toInt()
    }

    private fun scaledProgress(current: Int, goal: Int): Int {
        if (goal <= 0) return 0
        return ((current.toFloat() / goal.toFloat()) * 1000f)
            .coerceIn(0f, 1000f)
            .toInt()
    }

    private fun percentageValue(current: Int, goal: Int): Float {
        if (goal <= 0) return 0f
        return (current.toFloat() / goal.toFloat()) * 100f
    }

    private fun loadTodayTrackingStats(): DailyTrackingStats {
        val prefs = getSharedPreferences("diet_tracker_prefs", MODE_PRIVATE)
        val sleepPrefs = getSharedPreferences("sleep_tracker_prefs", MODE_PRIVATE)
        val dateKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val mealsJson = prefs.getString("meals_$dateKey", "[]") ?: "[]"
        val streak = prefs.getInt("streak_count", 0)
        val waterGlasses = prefs.getInt("water_$dateKey", prefs.getInt("water_glasses_$dateKey", 0))
        val sleepHours = when {
            sleepPrefs.contains("sleep_hours_int_$dateKey") -> sleepPrefs.getInt("sleep_hours_int_$dateKey", 0)
            sleepPrefs.contains("sleep_hours_$dateKey") -> sleepPrefs.getFloat("sleep_hours_$dateKey", 0f).toInt()
            else -> 0
        }

        return runCatching {
            val jsonArray = JSONArray(mealsJson)
            var calories = 0
            var protein = 0f
            var carbs = 0f
            var fat = 0f
            val mealsLogged = jsonArray.length()

            for (i in 0 until jsonArray.length()) {
                val meal = jsonArray.getJSONObject(i)
                calories += meal.optInt("calories", 0)
                protein += meal.optDouble("protein", 0.0).toFloat()
                carbs += meal.optDouble("carbs", 0.0).toFloat()
                fat += meal.optDouble("fat", 0.0).toFloat()
            }

            DailyTrackingStats(
                calories = calories,
                protein = protein.toInt(),
                carbs = carbs.toInt(),
                fat = fat.toInt(),
                waterGlasses = waterGlasses,
                sleepHours = sleepHours,
                mealsLogged = mealsLogged,
                streak = streak
            )
        }.getOrDefault(
            DailyTrackingStats(
                calories = 0,
                protein = 0,
                carbs = 0,
                fat = 0,
                waterGlasses = waterGlasses,
                sleepHours = sleepHours,
                mealsLogged = 0,
                streak = streak
            )
        )
    }

    private fun formatBmiCategory(category: BmiCategory): String {
        return when (category) {
            BmiCategory.UNDERWEIGHT -> "Underweight"
            BmiCategory.NORMAL -> "Healthy"
            BmiCategory.OVERWEIGHT -> "Overweight"
            BmiCategory.OBESE -> "Obese"
        }
    }
}
