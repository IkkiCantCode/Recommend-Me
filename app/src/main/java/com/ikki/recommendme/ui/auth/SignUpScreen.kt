package com.ikki.recommendme.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.ikki.recommendme.domain.model.UserProfile
import com.ikki.recommendme.ui.components.FormTextField
import com.ikki.recommendme.ui.components.GlowCircle
import com.ikki.recommendme.ui.components.GoogleButton
import com.ikki.recommendme.ui.components.GradientButton
import com.ikki.recommendme.ui.navigation.Routes

@Composable
fun SignUpScreen(
    navController: NavController,
    viewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    fun goToApp(profile: UserProfile) {
        // New accounts always go through the genre questionnaire first.
        val target = if (!profile.onboarded) Routes.QUESTIONNAIRE else Routes.HOME
        viewModel.consumeEvent()
        navController.navigate(target) {
            popUpTo(Routes.SIGN_IN) { inclusive = true }
        }
    }

    LaunchedEffect(state.event) {
        val event = state.event
        if (event is AuthEvent.SignedIn) goToApp(event.profile)
    }

    fun submit() {
        viewModel.signUp(name, email, password)
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        GlowCircle(
            color = MaterialTheme.colorScheme.secondary,
            size = 260.dp,
            alpha = 0.14f,
            modifier = Modifier.offset(x = (-70).dp, y = (-90).dp)
        )
        GlowCircle(
            color = MaterialTheme.colorScheme.primary,
            size = 220.dp,
            alpha = 0.12f,
            modifier = Modifier.align(Alignment.TopEnd).offset(x = 80.dp, y = 60.dp)
        )

        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 24.dp)
        ) {
            Spacer(Modifier.height(44.dp))

            Box(
                Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(com.ikki.recommendme.ui.theme.BrandGradient),
                contentAlignment = Alignment.Center
            ) {
                Text("🍿", style = MaterialTheme.typography.headlineSmall)
            }

            Spacer(Modifier.height(20.dp))
            Text(
                "Create your account",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "Save watchlists and get recommendations made for you",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(28.dp))

            FormTextField(
                value = name,
                onValueChange = {
                    name = it
                    viewModel.clearError()
                },
                label = "Name",
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            )
            Spacer(Modifier.height(14.dp))
            FormTextField(
                value = email,
                onValueChange = {
                    email = it
                    viewModel.clearError()
                },
                label = "Email",
                imeAction = ImeAction.Next
            )
            Spacer(Modifier.height(14.dp))
            FormTextField(
                value = password,
                onValueChange = {
                    password = it
                    viewModel.clearError()
                },
                label = "Password",
                isPassword = true,
                passwordVisible = passwordVisible,
                onTogglePasswordVisibility = { passwordVisible = !passwordVisible },
                imeAction = ImeAction.Done,
                onImeAction = { submit() }
            )

            state.error?.let { error ->
                Spacer(Modifier.height(10.dp))
                Text(
                    error,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(Modifier.height(24.dp))
            GradientButton(
                text = "Create Account",
                onClick = { submit() },
                loading = state.loading
            )

            Spacer(Modifier.height(20.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                HorizontalDivider(Modifier.weight(1f))
                Text(
                    "  or  ",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                HorizontalDivider(Modifier.weight(1f))
            }
            Spacer(Modifier.height(20.dp))

            GoogleButton(
                text = "Sign up with Google",
                onClick = { viewModel.signInWithGoogle() },
                enabled = !state.loading
            )

            Spacer(Modifier.height(24.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Do you have an account? ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(onClick = {
                    navController.navigate(Routes.SIGN_IN) {
                        popUpTo(Routes.SIGN_UP) { inclusive = true }
                    }
                }) {
                    Text(
                        "Sign In",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}
