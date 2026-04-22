package com.example.jetpackcomposeapp.ViewModels

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.jetpackcomposeapp.Model.ApiErrorResponse
import com.example.jetpackcomposeapp.Model.LoginRequest
import com.example.jetpackcomposeapp.Model.RetrofitClient
import com.example.jetpackcomposeapp.Utils.DataStoreManager
import com.google.gson.Gson
import kotlinx.coroutines.launch

class LoginViewModel(application: Application) : AndroidViewModel(application) {
    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    private val dataStoreManager = DataStoreManager(application)

    fun login(onSuccess: (String, String) -> Unit) {
        viewModelScope.launch {
            val normalizedEmail = email.trim().lowercase()
            val normalizedPassword = password.trim()

            when {
                normalizedEmail.isBlank() -> {
                    errorMessage = "Please enter your email"
                    return@launch
                }

                normalizedPassword.isBlank() -> {
                    errorMessage = "Please enter your password"
                    return@launch
                }
            }

            isLoading = true
            errorMessage = null
            try {
                val response = RetrofitClient.apiService.login(
                    LoginRequest(
                        email = normalizedEmail,
                        password = normalizedPassword
                    )
                )

                if (response.isSuccessful) {
                    val body = response.body()
                    val access = body?.access_token
                    val refresh = body?.refresh_token

                    if (access != null && refresh != null) {
                        dataStoreManager.saveTokens(access, refresh)
                        onSuccess(access, refresh)
                    } else {
                        errorMessage = "Login succeeded but no token was returned"
                    }
                } else {
                    val parsedError = response.errorBody()
                        ?.string()
                        ?.takeIf { it.isNotBlank() }
                        ?.let { runCatching { Gson().fromJson(it, ApiErrorResponse::class.java) }.getOrNull() }

                    errorMessage = when (val message = parsedError?.message) {
                        is List<*> -> message.filterIsInstance<String>().joinToString("\n").ifBlank {
                            "Invalid credentials. Please register first or check your password."
                        }

                        is String -> message
                        else -> "Invalid credentials. Please register first or check your password."
                    }
                }
            } catch (e: Exception) {
                errorMessage = "Network Error: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }
}
