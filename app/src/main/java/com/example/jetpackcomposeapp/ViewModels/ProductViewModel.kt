package com.example.jetpackcomposeapp.ViewModels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jetpackcomposeapp.Model.ProductResponseItem
import com.example.jetpackcomposeapp.Model.RetrofitClient
import kotlinx.coroutines.launch

class ProductViewModel : ViewModel() {
    var productsByCategory by mutableStateOf<List<ProductResponseItem>>(emptyList())
    var isLoading by mutableStateOf(false)

    fun fetchProductsByCategory(id: Int) {
        viewModelScope.launch {
            isLoading = true
            try {
                val response = RetrofitClient.apiService.getProductsByCategory(id)
                productsByCategory = response
            } catch (_: Exception) {
            } finally {
                isLoading = false
            }
        }
    }
}
