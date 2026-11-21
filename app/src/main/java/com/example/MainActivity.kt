package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.navigation.EnglishTutorNavHost
import com.example.ui.theme.EnglishTutorTheme
import com.example.ui.viewmodel.TutorViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val appContainer = (application as EnglishTutorApp).container
        val factory = TutorViewModelFactory(appContainer)

        setContent {
            val prefs by appContainer.preferencesManager.preferencesFlow
                .collectAsStateWithLifecycle(initialValue = com.example.data.preferences.TutorPreferencesState())

            val darkTheme = if (prefs.darkModeFollowSystem) {
                isSystemInDarkTheme()
            } else {
                prefs.darkModeEnabled
            }

            EnglishTutorTheme(darkTheme = darkTheme) {
                EnglishTutorNavHost(factory = factory)
            }
        }
    }
}
