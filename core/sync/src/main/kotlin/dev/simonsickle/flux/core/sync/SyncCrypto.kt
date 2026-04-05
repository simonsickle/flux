package dev.simonsickle.flux.core.sync

import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Handles PIN-based key derivation and AES-256-GCM encryption for sync payloads.
 *
 * Security model:
 * - PIN displayed on sender screen, typed by receiver (never sent over network)
 * - PIN hash sent for verification (so sender can reject wrong PINs)
 * - AES-256-GCM key derived from PIN + random salt via SHA-256
 * - Each encryption uses a fresh random IV
 */
internal object SyncCrypto {

    private const val GCM_IV_LENGTH = 12
    private const val GCM_TAG_LENGTH = 128

    fun generatePin(): String {
        val random = SecureRandom()
        return String.format("%06d", random.nextInt(1_000_000))
    }

    fun generateSalt(): ByteArray {
        val salt = ByteArray(32)
        SecureRandom().nextBytes(salt)
        return salt
    }

    /** Hash the PIN for network verification (sender checks this, not the raw PIN). */
    fun hashPin(pin: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(pin.toByteArray(Charsets.UTF_8))
        return hash.joinToString("") { "%02x".format(it) }
    }

    /** Derive AES-256 key from PIN + salt. Uses SHA-256(pin || salt). */
    fun deriveKey(pin: String, salt: ByteArray): SecretKey {
        val digest = MessageDigest.getInstance("SHA-256")
        digest.update(pin.toByteArray(Charsets.UTF_8))
        digest.update(salt)
        val keyBytes = digest.digest()
        return SecretKeySpec(keyBytes, "AES")
    }

    /** Encrypt plaintext bytes using AES-256-GCM. Returns IV prepended to ciphertext. */
    fun encrypt(data: ByteArray, key: SecretKey): ByteArray {
        val iv = ByteArray(GCM_IV_LENGTH)
        SecureRandom().nextBytes(iv)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(GCM_TAG_LENGTH, iv))
        val ciphertext = cipher.doFinal(data)
        return iv + ciphertext
    }

    /** Decrypt data produced by [encrypt]. Expects IV prepended to ciphertext. */
    fun decrypt(data: ByteArray, key: SecretKey): ByteArray {
        val iv = data.sliceArray(0 until GCM_IV_LENGTH)
        val ciphertext = data.sliceArray(GCM_IV_LENGTH until data.size)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(GCM_TAG_LENGTH, iv))
        return cipher.doFinal(ciphertext)
    }
}
