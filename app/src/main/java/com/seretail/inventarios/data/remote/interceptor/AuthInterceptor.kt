package com.seretail.inventarios.data.remote.interceptor

import com.seretail.inventarios.util.PreferencesManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val preferencesManager: PreferencesManager,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // Skip auth for login endpoint
        if (originalRequest.url.encodedPath.endsWith("/login")) {
            return chain.proceed(originalRequest)
        }

        val token = runBlocking { preferencesManager.token.first() }
        if (token.isNullOrEmpty()) {
            return chain.proceed(originalRequest)
        }

        val authenticatedRequest = originalRequest.newBuilder()
            .header("Authorization", "Bearer $token")
            .header("Accept", "application/json")
            .build()

        return chain.proceed(authenticatedRequest)
    }
}
