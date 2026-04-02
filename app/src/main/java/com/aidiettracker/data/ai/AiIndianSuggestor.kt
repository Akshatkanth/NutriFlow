package com.aidiettracker.data.ai

import com.aidiettracker.data.model.DietPreference
import com.aidiettracker.data.model.UserProfile

data class AiDietDayPlan(
    val buddyName: String,
    val breakfast: String,
    val lunch: String,
    val dinner: String,
    val snacks: String,
    val buddyTip: String
)

object AiIndianSuggestor {
    private const val DEFAULT_BUDDY_NAME = "Nourish Mitra"

    fun buildDietPlan(profile: UserProfile, calorieTarget: Int): AiDietDayPlan {
        val vegOnly = profile.dietPreference == DietPreference.VEG_ONLY

        val breakfast = if (vegOnly) {
            "Poha + sprouts + 1 fruit (banana/apple)"
        } else {
            "Masala omelette + multigrain toast + 1 fruit"
        }

        val lunch = if (vegOnly) {
            "2 roti + dal + seasonal sabzi + salad + curd"
        } else {
            "2 roti + chicken/fish curry + salad + curd"
        }

        val dinner = if (vegOnly) {
            "Bajra roti + paneer/soy bhurji + mixed veggies"
        } else {
            "Roti + grilled chicken/fish + sauteed veggies"
        }

        val snacks = if (vegOnly) {
            "Roasted chana, buttermilk, coconut water, papaya"
        } else {
            "Roasted chana, buttermilk, boiled eggs, guava"
        }

        val buddyTip = if (calorieTarget < 1900) {
            "Keep portions controlled and add fiber-rich veggies at each meal."
        } else {
            "Distribute calories across 4-5 meals and include protein each time."
        }

        return AiDietDayPlan(
            buddyName = DEFAULT_BUDDY_NAME,
            breakfast = breakfast,
            lunch = lunch,
            dinner = dinner,
            snacks = snacks,
            buddyTip = buddyTip
        )
    }

    fun suggestMealAddOn(
        profile: UserProfile,
        mealName: String,
        proteinGap: Int,
        carbsGap: Int,
        fatGap: Int
    ): String {
        val vegOnly = profile.dietPreference == DietPreference.VEG_ONLY
        val base = if (mealName.isBlank()) "your meal" else mealName

        return when {
            proteinGap > 20 -> {
                if (vegOnly) {
                    "$DEFAULT_BUDDY_NAME: For $base, add paneer/tofu or roasted chana for extra protein."
                } else {
                    "$DEFAULT_BUDDY_NAME: For $base, add eggs/chicken/fish or paneer for extra protein."
                }
            }
            carbsGap > 40 -> "$DEFAULT_BUDDY_NAME: Add a fruit or a small serving of rice/roti to balance energy."
            fatGap > 10 -> "$DEFAULT_BUDDY_NAME: Add 6-8 nuts or seeds for healthy fats."
            else -> "$DEFAULT_BUDDY_NAME: Great balance! Add cucumber-carrot salad or fruit for micronutrients."
        }
    }

    fun buddyName(): String = DEFAULT_BUDDY_NAME
}

