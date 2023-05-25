package com.mm.common.utils

import java.security.MessageDigest


/**
 * Date : 2023/4/21
 */

object Md5 {

    /**
     * 加密 16进制
     */
    @JvmStatic
    fun encodeHex(input: String): String {
        val digest = MessageDigest.getInstance("MD5")
        val result = digest.digest(input.toByteArray())
        return toHex(result)
    }

    //16进制
    @JvmStatic
    fun toHex(byteArray: ByteArray): String {
        val result = with(StringBuilder()) {
            byteArray.forEach {
                val hex = it.toInt() and (0xFF)
                val hexStr = Integer.toHexString(hex)
                if (hexStr.length == 1) {
                    this.append("0").append(hexStr)
                } else {
                    this.append(hexStr)
                }
            }
            this.toString()
        }
        //转成16进制后是32字节
        return result
    }

    @JvmStatic
    fun sha1(input: String): String {
        val instance = MessageDigest.getInstance("SHA-1")
        val digest = instance.digest(input.toByteArray())
        return toHex(digest)
    }

    @JvmStatic
    fun sha256(input: String): String {
        val instance = MessageDigest.getInstance("SHA-256")
        val digest = instance.digest(input.toByteArray())
        return toHex(digest)
    }
}