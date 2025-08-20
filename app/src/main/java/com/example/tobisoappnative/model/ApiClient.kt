package com.example.tobisoappnative.model

import okhttp3.Credentials
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.net.ssl.*

object ApiClient {
    private const val BASE_URL = "https://10.0.2.2:7270/api/"

    // Trust all certificates (pouze pro vývoj!)
    private fun getUnsafeOkHttpClient(): OkHttpClient {
        val username = "admin" // ZDE ZADEJ SVÉ JMÉNO
        val password = "secret123" // ZDE ZADEJ SVÉ HESLO
        val credential = Credentials.basic(username, password)
        return try {
            val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                override fun checkClientTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {}
                override fun checkServerTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {}
                override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate> = arrayOf()
            })
            val sslContext = SSLContext.getInstance("TLS")
            sslContext.init(null, trustAllCerts, java.security.SecureRandom())
            val trustManager = trustAllCerts[0] as X509TrustManager
            val builder = OkHttpClient.Builder()
            builder.sslSocketFactory(sslContext.socketFactory, trustManager)
            builder.hostnameVerifier { _, _ -> true }
            builder.addInterceptor { chain ->
                val request: Request = chain.request().newBuilder()
                    .addHeader("Authorization", credential)
                    .build()
                val response = chain.proceed(request)
                // Při HTTP chybě loguj pouze kód a zprávu, NE tělo odpovědi
                if (!response.isSuccessful) {
                    android.util.Log.e("ApiClient", "HTTP error: ${response.code()} ${response.message()}")
                }
                response
            }
            builder.build()
        } catch (e: Exception) {
            OkHttpClient.Builder().build()
        }
    }

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(getUnsafeOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
