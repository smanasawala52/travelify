package com.travelify.shared

/**
 * Shared Android API client stub reused by Customer / Travel Agent / Admin apps.
 * Wire OkHttp/Retrofit against VITE-equivalent base URL from BuildConfig.
 */
object ApiClient {
    const val DEFAULT_BASE_URL = "http://10.0.2.2:8080/api"

    fun authHeader(token: String): String = "Bearer $token"
}