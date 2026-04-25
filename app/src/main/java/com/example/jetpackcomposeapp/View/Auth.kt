package com.example.jetpackcomposeapp.View

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.jetpackcomposeapp.Model.ApiErrorResponse
import com.example.jetpackcomposeapp.Model.RetrofitClient
import com.example.jetpackcomposeapp.Model.UserRequest
import com.example.jetpackcomposeapp.Navigation.Dest
import com.example.jetpackcomposeapp.Utils.DataStoreManager
import com.example.jetpackcomposeapp.ViewModels.LoginViewModel
import com.google.gson.Gson
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch

@Composable
fun RegisterScreen(navController: NavController) {
    val context = LocalContext.current
    val dataStoreManager = remember { DataStoreManager(context) }
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var isLoading by rememberSaveable { mutableStateOf(false) }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    AuthCardLayout(
        title = "Create Account",
        subtitle = "Register first, then login to continue"
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        errorMessage?.let {
            Text(it, color = Color.Red, modifier = Modifier.padding(top = 8.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                scope.launch {
                    val normalizedName = name.trim()
                    val normalizedEmail = email.trim().lowercase()
                    val normalizedPassword = password.trim()

                    when {
                        normalizedName.isBlank() -> {
                            errorMessage = "Please enter your name"
                            return@launch
                        }

                        normalizedEmail.isBlank() -> {
                            errorMessage = "Please enter your email"
                            return@launch
                        }

                        normalizedPassword.length < 4 -> {
                            errorMessage = "Password must be at least 4 characters"
                            return@launch
                        }
                    }

                    isLoading = true
                    errorMessage = null

                    try {
                        val response = RetrofitClient.apiService.createUser(
                            UserRequest(
                                name = normalizedName,
                                email = normalizedEmail,
                                password = normalizedPassword,
                                avatar = "https://picsum.photos/200"
                            )
                        )

                        if (response.isSuccessful) {
                            dataStoreManager.saveUserCredentials(normalizedEmail, normalizedPassword)
                            navController.currentBackStackEntry
                                ?.savedStateHandle
                                ?.set("prefill_email", normalizedEmail)
                            navController.currentBackStackEntry
                                ?.savedStateHandle
                                ?.set("prefill_password", normalizedPassword)

                            navController.navigate(Dest.LOGIN) {
                                popUpTo(Dest.REGISTER) { inclusive = true }
                            }
                        } else {
                            val errorBody = response.errorBody()?.string()
                            val parsedError = errorBody
                                ?.takeIf { it.isNotBlank() }
                                ?.let { runCatching { Gson().fromJson(it, ApiErrorResponse::class.java) }.getOrNull() }

                            errorMessage = when (val message = parsedError?.message) {
                                is List<*> -> message.filterIsInstance<String>().joinToString("\n").ifBlank {
                                    "Registration failed: ${response.code()}"
                                }
                                is String -> message
                                else -> "Registration failed: ${response.code()}"
                            }
                        }
                    } catch (e: Exception) {
                        errorMessage = "Network Error: ${e.localizedMessage}"
                    } finally {
                        isLoading = false
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            enabled = !isLoading,
            shape = RoundedCornerShape(12.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp)
            } else {
                Text("Register")
            }
        }

        TextButton(
            onClick = {
                navController.navigate(Dest.LOGIN)
            },
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text("Already have an account? Login")
        }
    }
}

@Composable
fun LoginScreen(navController: NavController) {
    val viewModel: LoginViewModel = viewModel()
    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle

    LaunchedEffect(savedStateHandle) {
        val prefillEmail = savedStateHandle?.get<String>("prefill_email")
        val prefillPassword = savedStateHandle?.get<String>("prefill_password")

        if (!prefillEmail.isNullOrBlank()) {
            viewModel.email = prefillEmail
            savedStateHandle.remove<String>("prefill_email")
        }

        if (!prefillPassword.isNullOrBlank()) {
            viewModel.password = prefillPassword
            savedStateHandle.remove<String>("prefill_password")
        }
    }

    AuthCardLayout(
        title = "Welcome Back",
        subtitle = "Login to your account"
    ) {
        OutlinedTextField(
            value = viewModel.email,
            onValueChange = { viewModel.email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = viewModel.password,
            onValueChange = { viewModel.password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        viewModel.errorMessage?.let {
            Text(it, color = Color.Red, modifier = Modifier.padding(top = 8.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                viewModel.login { accessToken, refreshToken ->
                    navController.navigate(Dest.MAIN) {
                        popUpTo(Dest.LOGIN) { inclusive = true }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            enabled = !viewModel.isLoading,
            shape = RoundedCornerShape(12.dp)
        ) {
            if (viewModel.isLoading) {
                CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp)
            } else {
                Text("Login")
            }
        }

        TextButton(
            onClick = {
                navController.navigate(Dest.REGISTER) {
                    popUpTo(Dest.LOGIN) { inclusive = true }
                }
            },
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text("Create a new account")
        }
    }
}

@Composable
private fun AuthCardLayout(
    title: String,
    subtitle: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF6200EE), Color(0xFF3700B3)))),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(0.85f),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(title, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(subtitle, color = Color.Gray)
                Spacer(modifier = Modifier.height(24.dp))
                content()
            }
        }
    }
}
