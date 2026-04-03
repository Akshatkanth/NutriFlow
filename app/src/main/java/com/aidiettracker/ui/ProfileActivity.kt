package com.aidiettracker.ui

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.inputmethod.InputMethodManager
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
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
import com.google.firebase.auth.FirebaseAuth
import kotlin.math.round

class ProfileActivity : AppCompatActivity() {

    private lateinit var stepViews: List<View>
    private lateinit var textStepHint: TextView
    private lateinit var textQuestion: TextView
    private lateinit var textHelper: TextView
    private lateinit var optionsContainer: LinearLayout
    private lateinit var optionOne: TextView
    private lateinit var optionTwo: TextView
    private lateinit var optionThree: TextView
    private lateinit var inputContainer: LinearLayout
    private lateinit var editNumeric: EditText
    private lateinit var textUnit: TextView
    private lateinit var inputUnderline: View
    private lateinit var buttonNext: MaterialButton

    private var currentStep = 0
    private val totalSteps = 6
    private var existingProfile: UserProfile? = null

    private var selectedGoal: Goal? = null
    private var selectedGender: String? = null
    private var selectedActivity: ActivityLevel? = null
    private var heightCm: Double? = null
    private var weightKg: Double? = null
    private var targetWeightKg: Double? = null

    private val profileTextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit

        override fun afterTextChanged(s: Editable?) {
            val value = s?.toString()?.trim()?.toDoubleOrNull()
            when (currentStep) {
                2 -> heightCm = value
                3 -> weightKg = value
                4 -> targetWeightKg = value
            }
            updateInputUnderline()
            updateProgressAndActionState()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        stepViews = listOf(
            findViewById(R.id.step_1),
            findViewById(R.id.step_2),
            findViewById(R.id.step_3),
            findViewById(R.id.step_4),
            findViewById(R.id.step_5),
            findViewById(R.id.step_6)
        )
        textStepHint = findViewById(R.id.text_step_hint)
        textQuestion = findViewById(R.id.text_question)
        textHelper = findViewById(R.id.text_helper)
        optionsContainer = findViewById(R.id.options_container)
        optionOne = findViewById(R.id.option_one)
        optionTwo = findViewById(R.id.option_two)
        optionThree = findViewById(R.id.option_three)
        inputContainer = findViewById(R.id.input_container)
        editNumeric = findViewById(R.id.edit_numeric)
        textUnit = findViewById(R.id.text_unit)
        inputUnderline = findViewById(R.id.view_input_underline)
        buttonNext = findViewById(R.id.button_save_profile)

        existingProfile = LocalProfileStore.load(this)
        existingProfile?.let {
            selectedGoal = it.goal
            selectedActivity = it.activityLevel
            heightCm = it.heightCm.takeIf { h -> h > 0 }
            weightKg = it.weightKg.takeIf { w -> w > 0 }
            targetWeightKg = it.targetWeightKg.takeIf { t -> t > 0 }
        }

        findViewById<View>(R.id.button_back).setOnClickListener {
            if (currentStep > 0) {
                currentStep -= 1
                renderStep()
            } else {
                finish()
            }
        }

        optionOne.setOnClickListener { onOptionSelected(0) }
        optionTwo.setOnClickListener { onOptionSelected(1) }
        optionThree.setOnClickListener { onOptionSelected(2) }

        editNumeric.addTextChangedListener(profileTextWatcher)

        buttonNext.setOnClickListener {
            if (!isStepAnswered(currentStep)) {
                Toast.makeText(this, "Please answer this step", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (currentStep < totalSteps - 1) {
                currentStep += 1
                renderStep()
            } else {
                saveProfile()
            }
        }

        renderStep()
    }

    private fun onOptionSelected(index: Int) {
        when (currentStep) {
            0 -> selectedGoal = when (index) {
                0 -> Goal.LOSE
                1 -> Goal.MAINTAIN
                else -> Goal.GAIN
            }

            1 -> selectedGender = if (index == 0) "Female" else "Male"

            5 -> selectedActivity = when (index) {
                0 -> ActivityLevel.SEDENTARY
                1 -> ActivityLevel.MODERATE
                else -> ActivityLevel.ACTIVE
            }
        }

        applySelectionStyles(index)
        updateProgressAndActionState()
    }

    private fun renderStep() {
        when (currentStep) {
            0 -> {
                configureStepText(
                    hint = "Let's get to know you better!",
                    question = "What goal do you have\nin mind?",
                    helper = ""
                )
                configureOptions(listOf("Lose weight", "Maintain weight", "Gain weight"))
                applySelectionStyles(
                    when (selectedGoal) {
                        Goal.LOSE -> 0
                        Goal.MAINTAIN -> 1
                        Goal.GAIN -> 2
                        else -> -1
                    }
                )
            }

            1 -> {
                configureStepText(
                    hint = "Great, let's continue.",
                    question = "What's your gender?",
                    helper = "We use this information to calculate and provide you with daily personalized recommendations."
                )
                configureOptions(listOf("Female", "Male"))
                applySelectionStyles(
                    when (selectedGender) {
                        "Female" -> 0
                        "Male" -> 1
                        else -> -1
                    }
                )
            }

            2 -> {
                configureStepText(
                    hint = "Thanks, you're doing great!",
                    question = "What's your height?",
                    helper = "We use this information to calculate and provide you with daily personalized recommendations."
                )
                configureNumericInput(value = heightCm, unit = "cm")
            }

            3 -> {
                configureStepText(
                    hint = "Ok, let's continue.",
                    question = "What's your current weight?",
                    helper = "We use this information to calculate and provide you with daily personalized recommendations."
                )
                configureNumericInput(value = weightKg, unit = "kg")
            }

            4 -> {
                configureStepText(
                    hint = "Almost there!",
                    question = "What's your target weight?",
                    helper = "This helps us personalize your daily plan and calorie target."
                )
                configureNumericInput(value = targetWeightKg, unit = "kg")
            }

            else -> {
                configureStepText(
                    hint = "Last step, keep going!",
                    question = "How active are you\nmost days?",
                    helper = "Activity level helps estimate your daily calorie needs accurately."
                )
                configureOptions(listOf("Sedentary", "Moderately active", "Very active"))
                applySelectionStyles(
                    when (selectedActivity) {
                        ActivityLevel.SEDENTARY -> 0
                        ActivityLevel.MODERATE -> 1
                        ActivityLevel.ACTIVE -> 2
                        else -> -1
                    }
                )
            }
        }

        buttonNext.text = if (currentStep == totalSteps - 1) "SAVE PROFILE" else "NEXT"
        updateProgressAndActionState()
    }

    private fun configureStepText(hint: String, question: String, helper: String) {
        textStepHint.text = hint
        textQuestion.text = question
        textHelper.text = helper
        textHelper.visibility = if (helper.isBlank()) View.GONE else View.VISIBLE
    }

    private fun configureOptions(options: List<String>) {
        optionsContainer.visibility = View.VISIBLE
        inputContainer.visibility = View.GONE

        optionOne.text = options.getOrElse(0) { "" }
        optionTwo.text = options.getOrElse(1) { "" }
        optionThree.text = options.getOrElse(2) { "" }

        optionOne.visibility = if (options.isNotEmpty()) View.VISIBLE else View.GONE
        optionTwo.visibility = if (options.size > 1) View.VISIBLE else View.GONE
        optionThree.visibility = if (options.size > 2) View.VISIBLE else View.GONE
    }

    private fun configureNumericInput(value: Double?, unit: String) {
        optionsContainer.visibility = View.GONE
        inputContainer.visibility = View.VISIBLE
        textUnit.text = unit

        editNumeric.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        editNumeric.isFocusable = true
        editNumeric.isFocusableInTouchMode = true
        editNumeric.isCursorVisible = true

        val display = value?.toString().orEmpty()
        if (editNumeric.text?.toString() != display) {
            editNumeric.setText(display)
        }
        editNumeric.post {
            editNumeric.requestFocus()
            editNumeric.setSelection(editNumeric.text?.length ?: 0)
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(editNumeric, InputMethodManager.SHOW_IMPLICIT)
        }
        updateInputUnderline()
    }

    private fun applySelectionStyles(selectedIndex: Int) {
        val options = listOf(optionOne, optionTwo, optionThree)
        options.forEachIndexed { index, view ->
            val visible = view.visibility == View.VISIBLE
            val selected = visible && index == selectedIndex
            view.isSelected = selected
            view.setTextColor(
                ContextCompat.getColor(
                    this,
                    if (selected) android.R.color.white else R.color.auth_text_primary
                )
            )
        }
    }

    private fun updateInputUnderline() {
        if (inputContainer.visibility != View.VISIBLE) return
        val hasInput = editNumeric.text?.toString()?.trim()?.isNotEmpty() == true
        inputUnderline.setBackgroundResource(
            if (hasInput) R.drawable.bg_profile_progress_active else R.drawable.bg_profile_progress_inactive
        )
    }

    private fun updateProgressAndActionState() {
        val activeIndex = if (isStepAnswered(currentStep)) currentStep else currentStep - 1
        stepViews.forEachIndexed { index, view ->
            view.setBackgroundResource(
                if (index <= activeIndex) R.drawable.bg_profile_progress_active
                else R.drawable.bg_profile_progress_inactive
            )
        }

        val enabled = isStepAnswered(currentStep)
        buttonNext.isEnabled = enabled
        buttonNext.backgroundTintList = ContextCompat.getColorStateList(
            this,
            if (enabled) R.color.onboarding_cta_green else R.color.auth_divider
        )
    }

    private fun isStepAnswered(step: Int): Boolean {
        return when (step) {
            0 -> selectedGoal != null
            1 -> !selectedGender.isNullOrBlank()
            2 -> (heightCm ?: 0.0) > 0.0
            3 -> (weightKg ?: 0.0) > 0.0
            4 -> (targetWeightKg ?: 0.0) > 0.0
            5 -> selectedActivity != null
            else -> false
        }
    }

    private fun saveProfile() {
        val finalHeight = heightCm ?: 0.0
        val finalWeight = weightKg ?: 0.0
        val finalTargetWeight = targetWeightKg ?: 0.0

        if (finalHeight <= 0.0 || finalWeight <= 0.0 || finalTargetWeight <= 0.0 || selectedGoal == null || selectedActivity == null) {
            Toast.makeText(this, "Please complete all required steps", Toast.LENGTH_SHORT).show()
            return
        }

        val existing = existingProfile
        val name = existing?.name?.takeIf { it.isNotBlank() }
            ?: FirebaseAuth.getInstance().currentUser?.displayName
            ?: "User"
        val age = existing?.age?.takeIf { it > 0 } ?: 25
        val dietPreference = existing?.dietPreference ?: DietPreference.NON_VEG

        val rawBmi = BmiCalculator.calculate(weightKg = finalWeight, heightCm = finalHeight)
        val bmi = round(rawBmi * 10.0) / 10.0
        val bmiCategory = BmiCalculator.categoryFor(bmi)

        val profile = UserProfile(
            uid = FirebaseAuth.getInstance().currentUser?.uid.orEmpty(),
            name = name,
            age = age,
            heightCm = finalHeight,
            weightKg = finalWeight,
            targetWeightKg = finalTargetWeight,
            bodyType = existing?.bodyType ?: BodyType.MESOMORPH,
            dietPreference = dietPreference,
            activityLevel = selectedActivity ?: ActivityLevel.MODERATE,
            goal = selectedGoal ?: Goal.MAINTAIN,
            bmi = bmi,
            bmiCategory = bmiCategory
        )
        LocalProfileStore.save(this, profile)

        Toast.makeText(this, "Profile saved. BMI: $bmi", Toast.LENGTH_SHORT).show()
        startActivity(Intent(this, DashboardActivity::class.java))
        finish()
    }
}
