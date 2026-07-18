package com.travelify.auth.api

import com.travelify.auth.util.AuthInterceptor
import com.travelify.auth.util.TokenManager
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

object ApiClient {

    // Replace with your actual base URL
    private const val BASE_URL = "https://api.travelify.com/"

    fun create(tokenManager: TokenManager): AuthService {
        val authInterceptor = AuthInterceptor(tokenManager)

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(AuthService::class.java)
    }
}

interface AuthService {
    @POST("login")
    suspend fun login(@Body loginRequest: LoginRequest): LoginResponse

    // Add other auth-related endpoints here
}

data class LoginRequest(val email: String, val pass: String)
data class LoginResponse(val token: String)