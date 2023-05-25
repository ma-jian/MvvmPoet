package com.mm.common.utils

import android.util.Base64
import java.io.ByteArrayOutputStream
import java.security.PrivateKey
import java.security.PublicKey
import javax.crypto.Cipher

/**
 * Created by : m
 * Date : 2022/3/5
 * 非对称加密
 */

object RSACrypt {
    private const val transformation = "RSA"
    private val cipher = Cipher.getInstance(transformation)
    private const val ENCYPT_SISE = 117
    private const val ENCYPT_MAX_SISE = 128

    @JvmStatic
    fun encryptBrPrivateKey(intput: String, privateKey: PrivateKey): String {
        val inputArray = intput.toByteArray()
        var temp: ByteArray?
        var offset = 0//偏一
        val byteArrayOutputStream = ByteArrayOutputStream()
        cipher.init(Cipher.ENCRYPT_MODE, privateKey)
        //拆成几个部分
        while (inputArray.size - offset > 0) {
            if (inputArray.size - offset >= ENCYPT_SISE) {
                temp = cipher.doFinal(inputArray, offset, ENCYPT_SISE)
                offset += ENCYPT_SISE
            } else {
                temp = cipher.doFinal(inputArray, offset, inputArray.size - offset)
                offset = inputArray.size
            }
            byteArrayOutputStream.write(temp)
        }
        byteArrayOutputStream.close()
        return Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT)

    }

    @JvmStatic
    fun encryptBrPublicKey(intput: String, privateKey: PublicKey): String {
        val inputArray = intput.toByteArray()
        var temp: ByteArray?
        var offset = 0//偏一
        val byteArrayOutputStream = ByteArrayOutputStream()
        cipher.init(Cipher.ENCRYPT_MODE, privateKey)
        //拆成几个部分
        while (inputArray.size - offset > 0) {
            if (inputArray.size - offset >= ENCYPT_SISE) {
                temp = cipher.doFinal(inputArray, offset, ENCYPT_SISE)
                offset += ENCYPT_SISE
            } else {
                temp = cipher.doFinal(inputArray, offset, inputArray.size - offset)
                offset = inputArray.size
            }
            byteArrayOutputStream.write(temp)
        }
        byteArrayOutputStream.close()
        return Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT)

    }

    @JvmStatic
    fun decryptBrPublicKey(intput: String, privateKey: PublicKey): String {
        val inputArray = Base64.decode(intput, Base64.DEFAULT)
        var temp: ByteArray?
        var offset = 0//偏一
        val byteArrayOutputStream = ByteArrayOutputStream()
        cipher.init(Cipher.DECRYPT_MODE, privateKey)
//拆成几个部分
        while (inputArray.size - offset > 0) {
            if (inputArray.size - offset >= ENCYPT_MAX_SISE) {
                temp = cipher.doFinal(inputArray, offset, ENCYPT_MAX_SISE)
                offset += ENCYPT_MAX_SISE
            } else {
                temp = cipher.doFinal(inputArray, offset, inputArray.size - offset)
                offset = inputArray.size
            }
            byteArrayOutputStream.write(temp)
        }
        byteArrayOutputStream.close()
        return String(byteArrayOutputStream.toByteArray())
    }

    @JvmStatic
    fun decryptBrPrivateKey(intput: String, privateKey: PrivateKey): String {
        val inputArray = Base64.decode(intput, Base64.DEFAULT)
        var temp: ByteArray?
        var offset = 0//偏一
        val byteArrayOutputStream = ByteArrayOutputStream()
        cipher.init(Cipher.DECRYPT_MODE, privateKey)
        //拆成几个部分
        while (inputArray.size - offset > 0) {
            if (inputArray.size - offset >= ENCYPT_MAX_SISE) {
                temp = cipher.doFinal(inputArray, offset, ENCYPT_MAX_SISE)
                offset += ENCYPT_MAX_SISE
            } else {
                temp = cipher.doFinal(inputArray, offset, inputArray.size - offset)
                offset = inputArray.size
            }
            byteArrayOutputStream.write(temp)
        }
        byteArrayOutputStream.close()
        return String(byteArrayOutputStream.toByteArray())
    }
}