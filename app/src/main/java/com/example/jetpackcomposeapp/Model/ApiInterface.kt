package com.example.jetpackcomposeapp.Model

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {
    @GET("categories")
    suspend fun getCategories(): List<Category>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @GET("products")
    suspend fun getProductsByCategory(
        @Query("categoryId") categoryId: Int
    ): List<ProductResponseItem>

    @POST("users")
    suspend fun createUser(@Body user: UserRequest): Response<UserResponse>
}
