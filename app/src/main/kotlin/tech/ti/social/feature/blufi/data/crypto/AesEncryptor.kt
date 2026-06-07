package tech.ti.social.feature.blufi.data.crypto

import android.util.Log
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

private const val TAG = "BluFiAesEncryptor"

/**
 * AES-128-CFB encryptor for BluFi secure communication.
 *
 * IV construction: sequence number as first byte, remaining 15 bytes are zero.
 */
class AesEncryptor {

    private var aesKey: ByteArray? = null
    private var initialized = false

    /**
     * Set the AES-128 key (must be 16 bytes).
     */
    fun setKey(key: ByteArray) {
        require(key.size >= 16) { "AES key must be at least 16 bytes, got ${key.size}" }
        aesKey = key.copyOf(16)
        initialized = true
        Log.d(TAG, "AES key set (${key.size} bytes -> 16 bytes)")
    }

    /**
     * Encrypt data using AES-128-CFB.
     * IV = [sequence, 0, 0, ..., 0] (16 bytes)
     */
    fun encrypt(data: ByteArray, sequence: Int): ByteArray {
        check(initialized) { "AES key not set" }

        val iv = buildIv(sequence)
        val cipher = Cipher.getInstance("AES/CFB/NoPadding")
        val keySpec = SecretKeySpec(aesKey!!, "AES")
        val ivSpec = IvParameterSpec(iv)
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec)

        return cipher.doFinal(data)
    }

    /**
     * Decrypt data using AES-128-CFB.
     * IV = [sequence, 0, 0, ..., 0] (16 bytes)
     */
    fun decrypt(data: ByteArray, sequence: Int): ByteArray {
        check(initialized) { "AES key not set" }

        val iv = buildIv(sequence)
        val cipher = Cipher.getInstance("AES/CFB/NoPadding")
        val keySpec = SecretKeySpec(aesKey!!, "AES")
        val ivSpec = IvParameterSpec(iv)
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec)

        return cipher.doFinal(data)
    }

    private fun buildIv(sequence: Int): ByteArray {
        return ByteArray(16).apply {
            this[0] = (sequence and 0xFF).toByte()
        }
    }
}
