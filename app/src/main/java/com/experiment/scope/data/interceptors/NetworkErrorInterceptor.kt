package com.experiment.scope.data.interceptors

import android.app.Activity
import android.widget.Toast
import okhttp3.Interceptor
import okhttp3.Response


class NetworkErrorInterceptor(
    private val activity: Activity
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)

        if (response.code() != 200) {
            val message = "We encountered network error. Retrying..."

            activity.runOnUiThread {
                Toast.makeText(
                    activity,
                    message,
                    Toast.LENGTH_SHORT)
                    .show()
            }
        }

        return response
    }

}