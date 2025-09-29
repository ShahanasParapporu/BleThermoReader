package com.takniatech.contec.navigation

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.takniatech.contec.presentation.SplashScreen
import com.takniatech.contec.presentation.auth.AuthLandingScreen
import com.takniatech.contec.presentation.auth.AuthViewModel
import com.takniatech.contec.presentation.auth.login.LoginScreen
import com.takniatech.contec.presentation.auth.login.LoginViewModel
import com.takniatech.contec.presentation.profile.ProfileScreen
import com.takniatech.contec.presentation.auth.signup.SignUpScreen
import com.takniatech.contec.presentation.auth.signup.SignUpViewModel
import com.takniatech.contec.presentation.profile.ProfileViewModel
import com.takniatech.contec.presentation.scan.ScanScreen

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ContecApp() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = hiltViewModel()
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
    val userId by authViewModel.userId.collectAsState()
    val context = LocalContext.current

    NavHost(navController, startDestination = "splash") {

        composable("splash") {
            SplashScreen(
                navController = navController,
                isLoggedIn = isLoggedIn,
                userId = userId
            )
        }
        composable("authLanding") {
            AuthLandingScreen(
                onLoginClick = { navController.navigate("login") },
                onSignUpClick = { navController.navigate("signup") }
            )
        }
        composable("signup") {
            val signUpViewModel: SignUpViewModel = hiltViewModel()
            SignUpScreen(
                viewModel = signUpViewModel,
                onSignUpSuccess = { newUserId ->
                    Toast.makeText(context, "Account Created Successfully", Toast.LENGTH_SHORT).show()
                    navController.navigate("login") {
                        popUpTo("signup") { inclusive = true  }
                        launchSingleTop = true
                    }
                },
                onNavigateToLogin = { navController.navigate("login"){
                    popUpTo("authLanding") { inclusive = false  } // removes login
                    launchSingleTop = true
                } }
            )
        }
        composable("login") {
            val loginViewModel: LoginViewModel = hiltViewModel()
            LoginScreen(
                viewModel = loginViewModel,
                onLoginSuccess = { newUserId ->
                    authViewModel.saveUserId(newUserId)
                    navController.navigate("profile") {
                        popUpTo("authLanding") { inclusive = true }
                    }
                },
                onNavigateToSignUp = { navController.navigate("signup") {
                    popUpTo("login") { inclusive = true } // removes login
                    launchSingleTop = true
                }}
            )
        }
        composable("profile") {
            val profileViewModel: ProfileViewModel = hiltViewModel()
            userId?.let { safeUserId ->
                ProfileScreen(
                    viewModel = profileViewModel,
                    userId = safeUserId,
                    onNavigateToScan = { navController.navigate("scan") },
                    onLogout = {
                        authViewModel.clearUserId()
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }
        }


        composable("scan") {
            val safeUserId = userId
            if (safeUserId == null) {
                navController.navigate("login") {
                    popUpTo("profile") { inclusive = true }
                }
                return@composable
            }

            ScanScreen(
                viewModel = hiltViewModel(),
                onBackClick = { navController.popBackStack() }
            )
        }

    }
}
