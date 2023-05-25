package com.mm.common.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.mm.common.db.entity.HostUrl


/**
 * Date : 2023/4/21
 * 用户 host url 地址
 * @since 1.0
 */
@Dao
interface UserHostUrlDao {

    @Query("SELECT * FROM host_url order by id asc")
    fun allHostUrl(): List<HostUrl>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUrl(url: HostUrl): Long

    @Update
    fun updateUrl(vararg urls: HostUrl)

    @Query("delete from host_url where originUrl = :key")
    fun delete(key: String): Int

    @Query("delete from host_url")
    fun deleteAll(): Int

    @Query("select * from host_url where originUrl =(:url)")
    fun getHostUrl(url: String): Int
}