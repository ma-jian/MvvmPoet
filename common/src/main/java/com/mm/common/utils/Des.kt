package com.mm.common.utils

import android.util.Base64
import java.security.Key
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.DESKeySpec
import javax.crypto.spec.IvParameterSpec

/**
 * Created by : m
 * Date : 2022/3/5
 * 对称加密
 */

object Des {
    //    val transformation  = "DES/ECB/PKCS5Padding"
    private const val transformation = "DES/CBC/PKCS5Padding"
    private const val algorithm = "DES"
    private val cipher = Cipher.getInstance(transformation)
    private val instance = SecretKeyFactory.getInstance(algorithm)

    @JvmStatic
    fun encrypt(input: String, password: String): String {
        val keySpec = DESKeySpec(password.toByteArray())
        val key: Key? = instance.generateSecret(keySpec)
        val iv = IvParameterSpec(password.toByteArray())
        cipher.init(Cipher.ENCRYPT_MODE, key, iv)//需要额外的参数
        val doFinal = cipher.doFinal(input.toByteArray())
        //base64编码
        return Base64.encodeToString(doFinal, Base64.DEFAULT)
    }

    @JvmStatic
    fun decrypt(input: String, password: String): ByteArray {
        val keySpec = DESKeySpec(password.toByteArray())
        val key: Key? = instance.generateSecret(keySpec)
        val iv = IvParameterSpec(password.toByteArray())
        cipher.init(Cipher.DECRYPT_MODE, key, iv)
        return cipher.doFinal(Base64.decode(input,Base64.DEFAULT))
    }
}