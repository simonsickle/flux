package dev.simonsickle.flux.core.sync

import android.util.Base64
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

/**
 * HTTP client used by the receiver to connect to the sender's [SyncServer].
 * Handles the pair → sync handshake and decrypts the received payload.
 */
internal class SyncClient(
    private val host: String,
    private val port: Int
) {
    private val json = Json { ignoreUnknownKeys = true }
    private val jsonMediaType = "application/json".toMediaType()

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    private val baseUrl get() = "http://$host:$port"

    /** Verify the server is reachable. */
    fun checkHealth(): Boolean {
        return try {
            val request = Request.Builder().url("$baseUrl/health").get().build()
            client.newCall(request).execute().use { it.isSuccessful }
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Send hashed PIN to the server for verification.
     * Returns the salt on success, or throws on failure.
     */
    fun pair(pin: String): PairResult {
        val pinHash = SyncCrypto.hashPin(pin)
        val body = json.encodeToString(PairRequest.serializer(), PairRequest(pinHash))

        val request = Request.Builder()
            .url("$baseUrl/pair")
            .post(body.toRequestBody(jsonMediaType))
            .build()

        val response = client.newCall(request).execute()

        if (!response.isSuccessful) {
            val errorBody = response.body?.string() ?: ""
            return when (response.code) {
                401 -> PairResult.InvalidPin(errorBody)
                409 -> PairResult.AlreadyPaired
                else -> PairResult.Error("Server error: ${response.code}")
            }
        }

        val pairResponse = json.decodeFromString<PairResponse>(response.body!!.string())
        val salt = Base64.decode(pairResponse.salt, Base64.NO_WRAP)
        return PairResult.Success(salt)
    }

    /** Fetch the encrypted sync payload after successful pairing. */
    fun fetchPayload(pin: String, salt: ByteArray): SyncPayload {
        val request = Request.Builder()
            .url("$baseUrl/sync")
            .post("{}".toRequestBody(jsonMediaType))
            .build()

        val response = client.newCall(request).execute()

        if (!response.isSuccessful) {
            throw SyncException("Failed to fetch payload: ${response.code}")
        }

        val syncResponse = json.decodeFromString<SyncResponse>(response.body!!.string())
        val encrypted = Base64.decode(syncResponse.encryptedPayload, Base64.NO_WRAP)
        val key = SyncCrypto.deriveKey(pin, salt)
        val decrypted = SyncCrypto.decrypt(encrypted, key)
        return json.decodeFromString(String(decrypted, Charsets.UTF_8))
    }

    fun close() {
        client.dispatcher.executorService.shutdown()
        client.connectionPool.evictAll()
    }

    sealed interface PairResult {
        data class Success(val salt: ByteArray) : PairResult
        data class InvalidPin(val message: String) : PairResult
        data object AlreadyPaired : PairResult
        data class Error(val message: String) : PairResult
    }
}

class SyncException(message: String, cause: Throwable? = null) : Exception(message, cause)
