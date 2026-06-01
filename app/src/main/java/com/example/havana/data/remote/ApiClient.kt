package com.example.havana.data.remote

import com.example.havana.data.session.SessionManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import com.google.gson.Gson


object ApiClient {

    /**
     * Backend base URL.
     *
     * For production builds, change this to your live server:
     *   "https://api.havana.com/api/"
     *
     * For emulator development, use:
     *   "http://10.0.2.2:8000/api/"
     *
     * For physical device on same network as your local server:
     *   "http://192.168.x.x:8000/api/"
     */
    var BASE_URL: String = "http://10.0.2.2:8000/api/"

    /** Enable/disable verbose HTTP logging. Set to false for production. */
    var enableLogging: Boolean = true

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (enableLogging) HttpLoggingInterceptor.Level.BODY
        else HttpLoggingInterceptor.Level.NONE
    }

    private val authInterceptor = Interceptor { chain ->
        val requestBuilder = chain.request().newBuilder()
        SessionManager.token?.let { token ->
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }
        chain.proceed(requestBuilder.build())
    }

    private val tokenAuthenticator = TokenAuthenticator()

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .authenticator(tokenAuthenticator)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    /**
     * Separate Retrofit instance for token refresh calls.
     * Uses a plain OkHttpClient without the authenticator to prevent
     * infinite retry loops when the refresh token itself is expired.
     */
    val refreshRetrofit: Retrofit
        get() = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(
                OkHttpClient.Builder()
                    .addInterceptor(loggingInterceptor)
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(15, TimeUnit.SECONDS)
                    .writeTimeout(15, TimeUnit.SECONDS)
                    .build()
            )
            .addConverterFactory(GsonConverterFactory.create(lenientGson))
            .build()

    private val lenientGson = Gson().newBuilder().setLenient().create()

    val retrofit: Retrofit
        get() = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(lenientGson))
            .build()

    fun <T> createService(serviceClass: Class<T>): T {
        return retrofit.create(serviceClass)
    }
}