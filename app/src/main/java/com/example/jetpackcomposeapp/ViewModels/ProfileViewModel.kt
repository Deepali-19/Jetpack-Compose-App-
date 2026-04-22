package com.example.jetpackcomposeapp.ViewModels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jetpackcomposeapp.Model.ProfileResponse
import com.example.jetpackcomposeapp.Model.RetrofitClient
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {
    var user by mutableStateOf<ProfileResponse?>(null)
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    fun getUserData(token: String) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            try {
                val response = RetrofitClient.apiService.getProfile(token)

                if (response.isSuccessful) {
                    user = response.body()
                } else {
                    errorMessage = "Unable to load profile: ${response.code()}"
                }
            } catch (e: Exception) {
                errorMessage = "Network Error: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }
}
