package com.experiment.scope.data.repository

import android.app.Activity
import com.experiment.scope.data.interceptors.NetworkErrorInterceptor
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

abstract class Repository(
    private val activity: Activity,
    private val networkErrorInterceptor: NetworkErrorInterceptor = NetworkErrorInterceptor(activity),
    private val httpLoggingInterceptor: HttpLoggingInterceptor = HttpLoggingInterceptor()
        .setLevel(HttpLoggingInterceptor.Level.BODY),
    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(httpLoggingInterceptor)
        .addInterceptor(networkErrorInterceptor)
        .readTimeout(30, TimeUnit.SECONDS)
        .connectTimeout(30, TimeUnit.SECONDS)
        .build(),
    internal val retrofitBuilder: Retrofit.Builder = Retrofit.Builder()
        .client(okHttpClient)
        .baseUrl(API_URL + API_SUFFIX)
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
) {
    companion object {
        const val API_URL = "http://mobi.connectedcar360.net/"
        const val API_SUFFIX = "api/"
    }
}