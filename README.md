# AI Based Diet Tracker

## Overview
AI Based Diet Tracker is an Android app that helps users sign in, complete a personalized profile, calculate BMI, generate a diet plan, track daily nutrition, and chat with an AI coach for practical nutrition advice.

The app is currently built with Kotlin, XML layouts, Firebase Authentication, local profile storage, and an NVIDIA NIM-backed chat assistant.

## Current App State
The app is fully wired around a signed-in user flow:

1. Splash and auth entry open first.
2. Returning users are routed straight into the app when a saved profile exists.
3. New users complete onboarding questions once, then their profile is saved locally.
4. The dashboard shows live calorie, macro, hydration, sleep, meal, and streak summaries.
5. The profile page shows saved data, edit actions, and a logout button.

## What the App Does Today
- Email/password sign-in and registration with Firebase Authentication.
- First-time profile onboarding for goal, diet preference, height, weight, target weight, and activity level.
- Saved profile display with BMI and category.
- Dashboard summary with calories remaining, macro progress, water, sleep, meals logged, and streak.
- Rule-based diet plan generation from the saved profile.
- Daily diet tracking for meals, calories, macros, water, and sleep.
- AI coach chat powered by NVIDIA NIM when the API key is configured.
- Profile editing by section, plus logout from the profile page.
- Lightweight local persistence so returning users keep their profile data on the device.

## Main Screens
1. Onboarding splash
2. Auth entry
3. Login
4. Register
5. Profile setup / onboarding
6. Dashboard
7. Diet plan
8. Diet tracker
9. Chat coach
10. Profile page
11. Profile edit

## Data and Storage
- Firebase Authentication is used for sign-in state.
- Profile data is stored locally through `SharedPreferences` via `LocalProfileStore`.
- Daily tracking data is stored locally on the device for calories, meals, macros, water, and sleep.
- The chat screen calls an NVIDIA NIM chat endpoint using the API key from `local.properties`.
- The project includes Firestore dependencies, but the current user flow is centered on local profile persistence.

## Key Logic
- BMI is calculated from weight and height, then classified into standard BMI categories.
- Diet goals are calculated from the saved profile.
- The dashboard and diet plan are driven by the stored profile and today’s logged data.
- If a signed-in user already has saved profile data, the app skips onboarding and goes straight to the dashboard.

## Tech Stack
- Kotlin
- XML layouts
- AndroidX
- Material Components
- Firebase Authentication
- Firebase Firestore dependency
- Retrofit
- NVIDIA NIM API integration

## Project Structure
- `app/src/main/java/com/aidiettracker/ui/` contains the activities and UI helpers.
- `app/src/main/java/com/aidiettracker/data/` contains BMI, goal, and diet logic.
- `app/src/main/java/com/aidiettracker/data/local/` contains the local profile store.
- `app/src/main/res/layout/` contains the screen layouts.
- `app/src/main/res/drawable/` contains the card, background, and icon assets.

## Setup
1. Open the project in Android Studio.
2. Add your Firebase `google-services.json` file to `app/`.
3. Add `NIM_API_KEY` to `local.properties` if you want the AI coach to work.
4. Sync Gradle.
5. Run the app on an emulator or device.

## Build Details
- `compileSdk`: 34
- `minSdk`: 24
- `targetSdk`: 34
- Kotlin JVM target: 17

## Notes
This app provides nutrition guidance and should not replace professional medical advice.
