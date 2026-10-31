package com.example.security

import com.example.data.preferences.AppPreferences
import kotlinx.coroutines.flow.first
import java.security.MessageDigest
import java.security.SecureRandom

object SecurityManager {

    private const val HASH_ALGORITHM = "SHA-256"
    private const val ITERATIONS = 10000

    fun generateSalt(): String {
        val random = SecureRandom()
        val salt = ByteArray(16)
        random.nextBytes(salt)
        return salt.joinToString("") { "%02x".format(it) }
    }

    fun hashPin(pin: String, saltHex: String): String {
        val saltBytes = hexStringToByteArray(saltHex)
        val pinBytes = pin.toByteArray(Charsets.UTF_8)
        val digest = MessageDigest.getInstance(HASH_ALGORITHM)

        digest.update(saltBytes)
        var hash = digest.digest(pinBytes)

        for (i in 1 until ITERATIONS) {
            digest.reset()
            digest.update(hash)
            hash = digest.digest(pinBytes)
        }

        return hash.joinToString("") { "%02x".format(it) }
    }

    suspend fun verifyPin(pin: String, preferences: AppPreferences): Boolean {
        val saltHex = preferences.pinSalt.first() ?: return false
        val expectedHash = preferences.pinHash.first() ?: return false
        val computedHash = hashPin(pin, saltHex)
        return computedHash == expectedHash
    }

    private fun hexStringToByteArray(s: String): ByteArray {
        val len = s.length
        val data = ByteArray(len / 2)
        var i = 0
        while (i < len) {
            data[i / 2] = ((Character.digit(s[i], 16) shl 4) + Character.digit(s[i + 1], 16)).toByte()
            i += 2
        }
        return data
    }
}
