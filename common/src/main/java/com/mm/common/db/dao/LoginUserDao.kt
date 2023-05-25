package com.mm.common.db.dao

import androidx.room.*
import com.mm.common.db.entity.LoginDbUser


/**
 * Created by : m
 * 用户登录信息
 * @since 1.0
 */
@Dao
interface LoginUserDao {

    @Query("SELECT * FROM login_user order by lastLogin desc limit 10")
    fun allLoginUser(): List<LoginDbUser>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUser(user: LoginDbUser): Long

    @Update
    fun updateUser(vararg user: LoginDbUser)

    @Query("delete from login_user where userName = :key")
    fun delete(key: String): Int

    @Query("delete from login_user")
    fun deleteAll(): Int
}