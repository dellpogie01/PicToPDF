package com.example.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "pictopdf_preferences")

class UserPreferences(private val context: Context) {

  companion object {
    val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
    val DARK_THEME_MODE = intPreferencesKey("dark_theme_mode") // 0: System, 1: Light, 2: Dark
    val DEFAULT_PAGE_SIZE = stringPreferencesKey("default_page_size")
    val DEFAULT_ORIENTATION = stringPreferencesKey("default_orientation")
    val DEFAULT_QUALITY = stringPreferencesKey("default_quality")
    val DEFAULT_MARGIN = floatPreferencesKey("default_margin")
  }

  val onboardingCompleted: Flow<Boolean> =
    context.dataStore.data.map { preferences -> preferences[ONBOARDING_COMPLETED] ?: false }

  suspend fun setOnboardingCompleted(completed: Boolean) {
    context.dataStore.edit { preferences -> preferences[ONBOARDING_COMPLETED] = completed }
  }

  val darkThemeMode: Flow<Int> =
    context.dataStore.data.map { preferences -> preferences[DARK_THEME_MODE] ?: 0 }

  suspend fun setDarkThemeMode(mode: Int) {
    context.dataStore.edit { preferences -> preferences[DARK_THEME_MODE] = mode }
  }
}
