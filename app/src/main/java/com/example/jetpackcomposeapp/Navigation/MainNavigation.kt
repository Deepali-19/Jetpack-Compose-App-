package com.example.jetpackcomposeapp.Navigation

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.jetpackcomposeapp.Model.LoginRequest
import com.example.jetpackcomposeapp.Model.RetrofitClient
import com.example.jetpackcomposeapp.View.CategoryProducts
import com.example.jetpackcomposeapp.View.RegisterScreen
import com.example.jetpackcomposeapp.Utils.DataStoreManager
import com.example.jetpackcomposeapp.View.LoginScreen
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.delay

@Composable
fun MainNavigation(navController: NavHostController) {
    NavHost(navController, startDestination = Dest.SPLASH) {
        composable(Dest.SPLASH) {
            SplashScreen(navController)
        }

        composable(Dest.REGISTER) {
            RegisterScreen(navController)
        }

        composable(Dest.LOGIN) {
            LoginScreen(navController)
        }

        composable(Dest.MAIN) {
            MainAppContainer(navController)
        }

        composable(Dest.CATEGORY_PRODUCTS) { backStackEntry ->
            val catId = backStackEntry.arguments?.getString("catId")?.toIntOrNull() ?: 0
            val catName = Uri.decode(backStackEntry.arguments?.getString("catName") ?: "")
            CategoryProducts(catId = catId, catName = catName)
        }
    }
}

@Composable
fun SplashScreen(navController: NavHostController) {
    val context = LocalContext.current
    val dataStoreManager = remember { DataStoreManager(context) }

    LaunchedEffect(Unit) {
        delay(2000)

        val token = dataStoreManager.accessToken.first()
        val savedEmail = dataStoreManager.userEmail.first()
        val savedPassword = dataStoreManager.userPassword.first()

        if (!token.isNullOrEmpty()) {
            navController.navigate(Dest.MAIN) {
                popUpTo(Dest.SPLASH) { inclusive = true }
            }
        } else if (!savedEmail.isNullOrBlank() && !savedPassword.isNullOrBlank()) {
            try {
                val response = RetrofitClient.apiService.login(
                    LoginRequest(
                        email = savedEmail,
                        password = savedPassword
                    )
                )

                if (response.isSuccessful) {
                    val body = response.body()
                    val access = body?.access_token
                    val refresh = body?.refresh_token

                    if (!access.isNullOrBlank() && !refresh.isNullOrBlank()) {
                        dataStoreManager.saveTokens(access, refresh)
                        navController.navigate(Dest.MAIN) {
                            popUpTo(Dest.SPLASH) { inclusive = true }
                        }
                    } else {
                        navController.navigate(Dest.LOGIN) {
                            popUpTo(Dest.SPLASH) { inclusive = true }
                        }
                    }
                } else {
                    navController.navigate(Dest.LOGIN) {
                        popUpTo(Dest.SPLASH) { inclusive = true }
                    }
                }
            } catch (_: Exception) {
                navController.navigate(Dest.LOGIN) {
                    popUpTo(Dest.SPLASH) { inclusive = true }
                }
            }
        } else {
            navController.navigate(Dest.REGISTER) {
                popUpTo(Dest.SPLASH) { inclusive = true }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF6200EE), Color(0xFF3700B3)))),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Store", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            CircularProgressIndicator(color = Color.White)
        }
    }
}
