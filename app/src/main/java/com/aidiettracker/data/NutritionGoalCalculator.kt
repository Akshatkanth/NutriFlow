package com.aidiettracker.data

import com.aidiettracker.data.model.ActivityLevel
import com.aidiettracker.data.model.Goal
import com.aidiettracker.data.model.UserProfile
import kotlin.math.roundToInt

data class NutritionGoals(
    val caloriesTarget: Int,
    val proteinGrams: Int,
    val carbsGrams: Int,
    val fatGrams: Int
)

object NutritionGoalCalculator {
    fun calculate(profile: UserProfile): NutritionGoals {
        val referenceWeight = when {
            profile.targetWeightKg > 0.0 -> profile.targetWeightKg
            profile.weightKg > 0.0 -> profile.weightKg
            else -> 70.0
        }

        val activityMultiplier = when (profile.activityLevel) {
            ActivityLevel.SEDENTARY -> 1.0
            ActivityLevel.LIGHT -> 1.1
            ActivityLevel.MODERATE -> 1.2
            ActivityLevel.ACTIVE -> 1.35
        }

        val calorieFactor = when (profile.goal) {
            Goal.LOSE -> 26.0
            Goal.MAINTAIN -> 30.0
            Goal.GAIN -> 33.0
        }

        val caloriesTarget = (referenceWeight * calorieFactor * activityMultiplier)
            .roundToInt()
            .coerceIn(1200, 3500)

        val proteinPerKg = when (profile.goal) {
            Goal.LOSE -> 1.8
            Goal.MAINTAIN -> 1.4
            Goal.GAIN -> 1.8
        }

        val proteinGrams = (referenceWeight * proteinPerKg)
            .roundToInt()
            .coerceAtLeast(60)

        val fatGrams = (caloriesTarget * 0.25 / 9.0)
            .roundToInt()
            .coerceAtLeast(35)

        val carbsGrams = (((caloriesTarget - (proteinGrams * 4) - (fatGrams * 9)) / 4.0)
            .roundToInt())
            .coerceAtLeast(100)

        return NutritionGoals(
            caloriesTarget = caloriesTarget,
            proteinGrams = proteinGrams,
            carbsGrams = carbsGrams,
            fatGrams = fatGrams
        )
    }
}

