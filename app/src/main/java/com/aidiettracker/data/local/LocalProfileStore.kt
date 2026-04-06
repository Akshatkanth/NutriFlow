package com.aidiettracker.data.local

import android.content.Context
import androidx.core.content.edit
import com.aidiettracker.data.model.ActivityLevel
import com.aidiettracker.data.model.BmiCategory
import com.aidiettracker.data.model.BodyType
import com.aidiettracker.data.model.DietPreference
import com.aidiettracker.data.model.Goal
import com.aidiettracker.data.model.UserProfile

object LocalProfileStore {
    private const val PREFS_NAME = "ai_diet_profile"

    private const val KEY_UID = "uid"
    private const val KEY_NAME = "name"
    private const val KEY_AGE = "age"
    private const val KEY_HEIGHT_CM = "height_cm"
    private const val KEY_WEIGHT_KG = "weight_kg"
    private const val KEY_TARGET_WEIGHT_KG = "target_weight_kg"
    private const val KEY_BODY_TYPE = "body_type"
    private const val KEY_DIET_PREFERENCE = "diet_preference"
    private const val KEY_ACTIVITY_LEVEL = "activity_level"
    private const val KEY_GOAL = "goal"
    private const val KEY_BMI = "bmi"
    private const val KEY_BMI_CATEGORY = "bmi_category"
    private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
    private const val KEY_ONBOARDING_COMPLETED_PREFIX = "onboarding_completed_"

    fun save(context: Context, profile: UserProfile) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit(commit = true) {
            putString(KEY_UID, profile.uid)
            putString(KEY_NAME, profile.name)
            putInt(KEY_AGE, profile.age)
            putFloat(KEY_HEIGHT_CM, profile.heightCm.toFloat())
            putFloat(KEY_WEIGHT_KG, profile.weightKg.toFloat())
            putFloat(KEY_TARGET_WEIGHT_KG, profile.targetWeightKg.toFloat())
            putString(KEY_BODY_TYPE, profile.bodyType.name)
            putString(KEY_DIET_PREFERENCE, profile.dietPreference.name)
            putString(KEY_ACTIVITY_LEVEL, profile.activityLevel.name)
            putString(KEY_GOAL, profile.goal.name)
            putFloat(KEY_BMI, profile.bmi.toFloat())
            putString(KEY_BMI_CATEGORY, profile.bmiCategory.name)
            putBoolean(KEY_ONBOARDING_COMPLETED, true)
            if (profile.uid.isNotBlank()) {
                putBoolean(KEY_ONBOARDING_COMPLETED_PREFIX + profile.uid, true)
            }
        }
    }

    fun clear(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit(commit = true) {
            clear()
        }
    }

    fun load(context: Context, expectedUid: String? = null): UserProfile? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val storedUid = prefs.getString(KEY_UID, "") ?: ""

        val name = prefs.getString(KEY_NAME, "") ?: ""
        if (name.isBlank()) {
            return null
        }

        val resolvedUid = when {
            expectedUid.isNullOrBlank() -> storedUid
            storedUid == expectedUid -> storedUid
            storedUid.isBlank() -> {
                prefs.edit(commit = true) {
                    putString(KEY_UID, expectedUid)
                    putBoolean(KEY_ONBOARDING_COMPLETED_PREFIX + expectedUid, true)
                }
                expectedUid
            }
            else -> return null
        }

        return UserProfile(
            uid = resolvedUid,
            name = name,
            age = prefs.getInt(KEY_AGE, 0),
            heightCm = prefs.getFloat(KEY_HEIGHT_CM, 0f).toDouble(),
            weightKg = prefs.getFloat(KEY_WEIGHT_KG, 0f).toDouble(),
            targetWeightKg = prefs.getFloat(KEY_TARGET_WEIGHT_KG, 0f).toDouble(),
            bodyType = enumOrDefault(prefs.getString(KEY_BODY_TYPE, null), BodyType.MESOMORPH),
            dietPreference = enumOrDefault(prefs.getString(KEY_DIET_PREFERENCE, null), DietPreference.NON_VEG),
            activityLevel = enumOrDefault(prefs.getString(KEY_ACTIVITY_LEVEL, null), ActivityLevel.MODERATE),
            goal = enumOrDefault(prefs.getString(KEY_GOAL, null), Goal.MAINTAIN),
            bmi = prefs.getFloat(KEY_BMI, 0f).toDouble(),
            bmiCategory = enumOrDefault(prefs.getString(KEY_BMI_CATEGORY, null), BmiCategory.NORMAL)
        )
    }

    fun hasCompletedOnboarding(context: Context, uid: String?): Boolean {
        if (uid.isNullOrBlank()) {
            return false
        }
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val storedUid = prefs.getString(KEY_UID, "") ?: ""
        if (storedUid.isNotBlank() && storedUid != uid) {
            return false
        }
        val explicitCompleted = prefs.getBoolean(
            KEY_ONBOARDING_COMPLETED_PREFIX + uid,
            prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)
        )
        return explicitCompleted || load(context, uid) != null
    }

    private inline fun <reified T : Enum<T>> enumOrDefault(value: String?, default: T): T {
        if (value.isNullOrBlank()) {
            return default
        }
        return runCatching { enumValueOf<T>(value) }.getOrDefault(default)
    }
}

