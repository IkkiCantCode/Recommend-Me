package com.ikki.recommendme

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ikki.recommendme.di.RecommendApplication
import com.ikki.recommendme.domain.model.ThemeMode
import com.ikki.recommendme.ui.navigation.AppNavHost
import com.ikki.recommendme.ui.navigation.Routes
import com.ikki.recommendme.ui.theme.RecommendMeTheme
import kotlinx.coroutines.flow.first

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val container = (application as RecommendApplication).container

        setContent {
            val themeMode by container.userProfileRepository.themeMode
                .collectAsStateWithLifecycle(initialValue = ThemeMode.SYSTEM)
            val systemDark = isSystemInDarkTheme()
            val darkTheme = when (themeMode) {
                ThemeMode.SYSTEM -> systemDark
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            RecommendMeTheme(darkTheme = darkTheme) {
                // Decide where the app starts: signed out -> sign in,
                // signed in but questionnaire pending -> questionnaire, else home.
                val startDestination by produceState<String?>(initialValue = null) {
                    val profile = container.authRepository.currentUser.first()
                    value = when {
                        profile == null -> Routes.SIGN_IN
                        !profile.onboarded -> Routes.QUESTIONNAIRE
                        else -> Routes.HOME
                    }
                }

                val destination = startDestination
                if (destination == null) {
                    SplashContent()
                } else {
                    AppNavHost(startDestination = destination)
                }
            }
        }
    }
}

@Composable
private fun SplashContent() {
    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("🎬", style = MaterialTheme.typography.displaySmall)
            Spacer(Modifier.height(10.dp))
            Text(
                "Recommend Me",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(18.dp))
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
    }
}
