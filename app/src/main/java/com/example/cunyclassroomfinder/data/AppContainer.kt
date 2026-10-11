package com.example.cunyclassroomfinder.data

import com.example.cunyclassroomfinder.network.CUNYApiService
import okhttp3.JavaNetCookieJar
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.net.CookieManager
import java.net.CookiePolicy

class AppContainer {
    val cookieManger = CookieManager().apply {
        setCookiePolicy(CookiePolicy.ACCEPT_ALL)
    }

    val okhttpClient = OkHttpClient.Builder()
        .cookieJar(JavaNetCookieJar(cookieManger))
        .build()
    private val baseUrl = "https://globalsearch.cuny.edu/CFGlobalSearchTool/"

    private val retrofit: Retrofit = Retrofit.Builder()
        .client(okhttpClient)
        .addConverterFactory(ScalarsConverterFactory.create())
        .baseUrl(baseUrl)
        .build()

    private val retrofitService: CUNYApiService by lazy {
        retrofit.create(CUNYApiService::class.java)
    }

    val cunyRepo: CUNYGlobalSearchRepository by lazy {
        CUNYGlobalSearchRepositoryRetrofit(retrofitService)
    }
}