package dev.simonsickle.flux.core.sync

import android.util.Base64
import android.util.Log
import fi.iki.elonen.NanoHTTPD
import kotlinx.serialization.json.Json
import java.util.concurrent.atomic.AtomicBoolean
import javax.crypto.SecretKey

/**
 * Ephemeral HTTP server that runs on the sender device during P2P sync.
 *
 * Lifecycle:
 * 1. Start server on random port → display PIN to user
 * 2. Receiver POSTs /pair with hashed PIN → server validates, returns salt
 * 3. Receiver POSTs /sync → server returns encrypted settings payload
 * 4. Server shuts down after successful sync (or 5 minute timeout)
 *
 * Only one pairing attempt is allowed at a time.
 * After 3 failed PIN attempts, the server shuts down.
 */
internal class SyncServer(
    private val pin: String,
    private val salt: ByteArray,
    private val payloadProvider: () -> SyncPayload,
    private val onPaired: () -> Unit,
    private val onSyncComplete: () -> Unit,
    private val onError: (String) -> Unit
) : NanoHTTPD(0) {

    companion object {
        private const val TAG = "SyncServer"
        private const val MAX_FAILED_ATTEMPTS = 3
    }

    private val json = Json { ignoreUnknownKeys = true }
    private val paired = AtomicBoolean(false)
    private var failedAttempts = 0
    private var derivedKey: SecretKey? = null
    private val expectedPinHash = SyncCrypto.hashPin(pin)

    override fun serve(session: IHTTPSession): Response {
        return try {
            when {
                session.uri == "/pair" && session.method == Method.POST -> handlePair(session)
                session.uri == "/sync" && session.method == Method.POST -> handleSync()
                session.uri == "/health" && session.method == Method.GET -> handleHealth()
                else -> newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_PLAINTEXT, "Not found")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Server error", e)
            newFixedLengthResponse(Response.Status.INTERNAL_ERROR, MIME_PLAINTEXT, "Internal error")
        }
    }

    private fun handleHealth(): Response {
        return newFixedLengthResponse(Response.Status.OK, "application/json", """{"status":"ready"}""")
    }

    private fun handlePair(session: IHTTPSession): Response {
        if (paired.get()) {
            return newFixedLengthResponse(
                Response.Status.CONFLICT, "application/json",
                """{"error":"Already paired"}"""
            )
        }

        val body = readBody(session)
        val request = json.decodeFromString<PairRequest>(body)

        if (request.pinHash != expectedPinHash) {
            failedAttempts++
            Log.w(TAG, "PIN mismatch attempt $failedAttempts/$MAX_FAILED_ATTEMPTS")
            if (failedAttempts >= MAX_FAILED_ATTEMPTS) {
                onError("Too many failed PIN attempts")
                stop()
            }
            return newFixedLengthResponse(
                Response.Status.UNAUTHORIZED, "application/json",
                """{"error":"Invalid PIN","attemptsRemaining":${MAX_FAILED_ATTEMPTS - failedAttempts}}"""
            )
        }

        paired.set(true)
        derivedKey = SyncCrypto.deriveKey(pin, salt)
        onPaired()

        val response = PairResponse(
            salt = Base64.encodeToString(salt, Base64.NO_WRAP),
            accepted = true
        )
        return newFixedLengthResponse(
            Response.Status.OK, "application/json",
            json.encodeToString(PairResponse.serializer(), response)
        )
    }

    private fun handleSync(): Response {
        if (!paired.get()) {
            return newFixedLengthResponse(
                Response.Status.UNAUTHORIZED, "application/json",
                """{"error":"Not paired"}"""
            )
        }

        val key = derivedKey ?: return newFixedLengthResponse(
            Response.Status.INTERNAL_ERROR, "application/json",
            """{"error":"Key not derived"}"""
        )

        val payload = payloadProvider()
        val payloadJson = json.encodeToString(SyncPayload.serializer(), payload)
        val encrypted = SyncCrypto.encrypt(payloadJson.toByteArray(Charsets.UTF_8), key)
        val encoded = Base64.encodeToString(encrypted, Base64.NO_WRAP)

        val response = SyncResponse(encryptedPayload = encoded)

        onSyncComplete()

        return newFixedLengthResponse(
            Response.Status.OK, "application/json",
            json.encodeToString(SyncResponse.serializer(), response)
        )
    }

    private fun readBody(session: IHTTPSession): String {
        val contentLength = session.headers["content-length"]?.toIntOrNull() ?: 0
        val buffer = ByteArray(contentLength)
        session.inputStream.read(buffer, 0, contentLength)
        return String(buffer, Charsets.UTF_8)
    }
}
