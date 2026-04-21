package com.example.jetpackcomposeapp.ViewModels

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.jetpackcomposeapp.Model.LoginRequest
import com.example.jetpackcomposeapp.Model.RetrofitClient
import com.example.jetpackcomposeapp.Utils.DataStoreManager
import kotlinx.coroutines.launch

class LoginViewModel(application: Application) : AndroidViewModel(application) {
    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    private val dataStoreManager = DataStoreManager(application)

    fun login(onSuccess: (String, String) -> Unit) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val response = RetrofitClient.apiService.login(LoginRequest(email, password))

                if (response.isSuccessful) {
                    val body = response.body()
                    val access = body?.access_token
                    val refresh = body?.refresh_token

                    if (access != null && refresh != null) {
                        dataStoreManager.saveTokens(access, refresh)
                        onSuccess(access, refresh)
                    } else {
                        errorMessage = "Missing token data"
                    }
                } else {
                    errorMessage = "Invalid Credentials: ${response.code()}"
                }
            } catch (e: Exception) {
                errorMessage = "Network Error: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }
}
