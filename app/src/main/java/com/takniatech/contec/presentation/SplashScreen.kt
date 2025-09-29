package com.takniatech.contec.presentation

// SplashScreen.kt
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import com.takniatech.contec.R
import com.takniatech.contec.ui.theme.Montserrat

@Composable
fun SplashScreen(
    navController: NavController,
    isLoggedIn: Boolean,
    userId: Int?
) {
    // Keep your existing navigation logic
    LaunchedEffect(isLoggedIn, userId) {
        delay(1000) // Delay to show the splash screen
        if (isLoggedIn && userId != null) {
            navController.navigate("profile") {
                popUpTo("splash") { inclusive = true }
            }
        } else {
            navController.navigate("authLanding") {
                popUpTo("splash") { inclusive = true }
            }
        }
    }

    // --- UI Design for Splash Screen ---
    val alphaAnim = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        alphaAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1000, easing = LinearEasing)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary, // Start with your primary brand color
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                        MaterialTheme.colorScheme.background // Fade to background or a complementary color
                    )
                )
            )
            .alpha(alphaAnim.value), // Apply fade-in animation
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Your App Logo/Icon
            Image(
                painter = painterResource(id = R.drawable.welcomeimg), // Use your actual app logo/icon
                contentDescription = "App Logo",
                modifier = Modifier.size(150.dp) // Adjust size as needed
            )
            Spacer(modifier = Modifier.height(24.dp))
            // App Name
            Text(
                text = "Contec Health", // Replace with your app's actual name
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontFamily = Montserrat, // Assuming Montserrat is your app's main font
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp
                ),
                color = MaterialTheme.colorScheme.onPrimary // Text color that contrasts well
            )
            Spacer(modifier = Modifier.height(8.dp))
            // Tagline (Optional)
            Text(
                text = "Your Wellness Journey Starts Here", // Replace with your app's tagline
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
            )
        }

    }
}
