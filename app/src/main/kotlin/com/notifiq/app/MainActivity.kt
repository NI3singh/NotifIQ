package com.notifiq.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.notifiq.app.navigation.NotifIQNavHost
import com.notifiq.core.datastore.UserPreferenceDataStore
import com.notifiq.core.designsystem.theme.NotifIQTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var userPreferenceDataStore: UserPreferenceDataStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val userPreference by userPreferenceDataStore.userPreference.collectAsState(
                initial = com.notifiq.core.model.UserPreference()
            )

            NotifIQTheme(darkTheme = userPreference.darkModeEnabled) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NotifIQNavHost(
                        hasCompletedOnboarding = userPreference.hasCompletedOnboarding,
                        userPreferenceDataStore = userPreferenceDataStore
                    )
                }
            }
        }
    }
}