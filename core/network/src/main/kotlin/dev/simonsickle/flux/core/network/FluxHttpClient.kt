package dev.simonsickle.flux.core.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FluxHttpClient @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val json: Json
) {
    suspend inline fun <reified T> getJson(url: String): T = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        val response = okHttpClient.newCall(request).execute()
        if (!response.isSuccessful) {
            throw HttpException(response.code, "HTTP ${response.code}: ${response.message}")
        }

        val body = response.body?.string()
            ?: throw HttpException(response.code, "Empty response body")

        json.decodeFromString(body)
    }
}

class HttpException(val code: Int, message: String) : Exception(message)
