package com.aidiettracker.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.aidiettracker.DietTrackerActivity
import com.aidiettracker.R
import com.aidiettracker.data.NutritionGoalCalculator
import com.aidiettracker.data.local.LocalProfileStore
import com.aidiettracker.data.model.BmiCategory
import com.google.android.material.button.MaterialButton
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
        val water: Int,
        val streak: Int
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        bindNavigation()
        bindLiveSummary()
    }

    private fun bindNavigation() {
        findViewById<MaterialButton>(R.id.button_view_plan).setOnClickListener {
            startActivity(Intent(this, DietPlanActivity::class.java))
        }

        findViewById<MaterialButton>(R.id.button_track_diet).setOnClickListener {
            startActivity(Intent(this, DietTrackerActivity::class.java))
        }

        findViewById<MaterialButton>(R.id.button_profile).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        bindLiveSummary()
    }

    private fun bindLiveSummary() {
        val profile = LocalProfileStore.load(this)
        if (profile == null) {
            renderEmptySummary()
            return
        }

        val goals = NutritionGoalCalculator.calculate(profile)
        val today = loadTodayTrackingStats()

        val calorieTarget = goals.caloriesTarget.coerceAtLeast(1)
        val calorieProgress = percent(today.calories, calorieTarget)
        val caloriesRemaining = (calorieTarget - today.calories).coerceAtLeast(0)

        findViewById<TextView>(R.id.text_bmi).text = String.format(Locale.getDefault(), "%.1f", profile.bmi)
        findViewById<TextView>(R.id.text_bmi_category).text = formatBmiCategory(profile.bmiCategory)

        findViewById<TextView>(R.id.text_calories_consumed).text = today.calories.toString()
        findViewById<TextView>(R.id.text_calories_target).text = String.format(Locale.getDefault(), "/ %d kcal", goals.caloriesTarget)
        findViewById<TextView>(R.id.text_calories_remaining).text = String.format(Locale.getDefault(), "%d kcal remaining", caloriesRemaining)
        findViewById<ProgressBar>(R.id.progress_calories).progress = calorieProgress

        findViewById<TextView>(R.id.text_water).text = String.format(Locale.getDefault(), "%d / 8", today.water)
        findViewById<TextView>(R.id.text_steps).text = today.streak.toString()
        findViewById<TextView>(R.id.text_steps_label).text = if (today.streak == 1) "day streak" else "days streak"

        findViewById<TextView>(R.id.text_protein_value).text = String.format(Locale.getDefault(), "%dg / %dg", today.protein, goals.proteinGrams)
        findViewById<TextView>(R.id.text_carbs_value).text = String.format(Locale.getDefault(), "%dg / %dg", today.carbs, goals.carbsGrams)
        findViewById<TextView>(R.id.text_fat_value).text = String.format(Locale.getDefault(), "%dg / %dg", today.fat, goals.fatGrams)

        findViewById<ProgressBar>(R.id.progress_protein).progress = percent(today.protein, goals.proteinGrams)
        findViewById<ProgressBar>(R.id.progress_carbs).progress = percent(today.carbs, goals.carbsGrams)
        findViewById<ProgressBar>(R.id.progress_fat).progress = percent(today.fat, goals.fatGrams)
    }

    private fun renderEmptySummary() {
        findViewById<TextView>(R.id.text_bmi).text = "--"
        findViewById<TextView>(R.id.text_bmi_category).text = "Complete profile"
        findViewById<TextView>(R.id.text_calories_consumed).text = "0"
        findViewById<TextView>(R.id.text_calories_target).text = String.format(Locale.getDefault(), "/ %d kcal", 0)
        findViewById<TextView>(R.id.text_calories_remaining).text = String.format(Locale.getDefault(), "%d kcal remaining", 0)
        findViewById<TextView>(R.id.text_water).text = String.format(Locale.getDefault(), "%d / 8", 0)
        findViewById<TextView>(R.id.text_steps).text = "0"
        findViewById<TextView>(R.id.text_steps_label).text = "day streak"
        findViewById<TextView>(R.id.text_protein_value).text = String.format(Locale.getDefault(), "%dg / %dg", 0, 0)
        findViewById<TextView>(R.id.text_carbs_value).text = String.format(Locale.getDefault(), "%dg / %dg", 0, 0)
        findViewById<TextView>(R.id.text_fat_value).text = String.format(Locale.getDefault(), "%dg / %dg", 0, 0)
        findViewById<ProgressBar>(R.id.progress_calories).progress = 0
        findViewById<ProgressBar>(R.id.progress_protein).progress = 0
        findViewById<ProgressBar>(R.id.progress_carbs).progress = 0
        findViewById<ProgressBar>(R.id.progress_fat).progress = 0
    }

    private fun loadTodayTrackingStats(): DailyTrackingStats {
        val prefs = getSharedPreferences("diet_tracker_prefs", MODE_PRIVATE)
        val dateKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val mealsJson = prefs.getString("meals_$dateKey", "[]") ?: "[]"
        val water = prefs.getInt("water_$dateKey", 0)
        val streak = prefs.getInt("streak_count", 0)

        val totals = runCatching {
            val jsonArray = JSONArray(mealsJson)
            var calories = 0
            var protein = 0f
            var carbs = 0f
            var fat = 0f

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
                water = water,
                streak = streak
            )
        }.getOrDefault(
            DailyTrackingStats(
                calories = 0,
                protein = 0,
                carbs = 0,
                fat = 0,
                water = water,
                streak = streak
            )
        )

        return totals
    }

    private fun percent(value: Int, target: Int): Int {
        if (target <= 0) {
            return 0
        }
        return ((value.toFloat() / target.toFloat()) * 100).toInt().coerceIn(0, 100)
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
