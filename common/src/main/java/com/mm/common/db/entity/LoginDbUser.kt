package com.mm.common.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mm.common.Constants
import com.mm.common.utils.Aes

/**
 * Created by : m
 * @since 1.0
 */
@Entity(tableName = "login_user")
class LoginDbUser {
    @PrimaryKey
    var userName: String = ""
    var passWord: String? = null
    var groupName: String? = null
    var lastLogin: Long = 0L
    var isMD5: Boolean = false
    var isAutoLogin: Boolean = true
    var isRememberUser: Boolean = true

    companion object {
        //加密
        @JvmStatic
        fun encryptPassword(password: String?): String {
            return password?.run {
                Aes.encrypt(password, Constants.ENCRYPT_KEY)
            } ?: ""
        }

        //解密
        @JvmStatic
        fun decryptPassword(password: String?): String {
            return password?.run {
                Aes.decrypt(password, Constants.ENCRYPT_KEY)
            } ?: ""
        }
    }
}