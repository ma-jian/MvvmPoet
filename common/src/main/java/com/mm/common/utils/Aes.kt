package com.mm.common.utils

import android.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Created by : m
 * Date : 2022/3/5
 */
object Aes {
    private val cipher: Cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")

    @JvmStatic
    fun encrypt(input: String, key: String): String {
        val keys = ivParameterSpec(key)
        val keySpec = SecretKeySpec(keys, "AES")
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, IvParameterSpec(keys))
        val doFinal = cipher.doFinal(input.toByteArray())
        return Base64.encodeToString(doFinal, Base64.DEFAULT)
    }

    @JvmStatic
    fun decrypt(input: String, key: String): String {
        val keys = ivParameterSpec(key)
        val keySpec = SecretKeySpec(keys, "AES")
        cipher.init(Cipher.DECRYPT_MODE, keySpec, IvParameterSpec(keys))
        val doFinal = cipher.doFinal(Base64.decode(input, Base64.DEFAULT))
        return String(doFinal)
    }

    private fun ivParameterSpec(key: String): ByteArray {
        return InsecureSHA1PRNGKeyDerivator.deriveInsecureKey(key.toByteArray(), 16);
    }
}