package com.aidiettracker.ui

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.aidiettracker.R
import com.aidiettracker.data.NutritionGoalCalculator
import com.aidiettracker.data.ai.AiIndianSuggestor
import com.aidiettracker.data.local.LocalProfileStore

class DietPlanActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_diet_plan)

        val profile = LocalProfileStore.load(this)
        if (profile == null) {
            Toast.makeText(this, "Complete your profile first", Toast.LENGTH_SHORT).show()
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
}
