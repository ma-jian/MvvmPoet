package com.mm.common.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey


/**
 * Date : 2023/4/21
 * @since 1.0
 */
@Entity(tableName = "host_url")
class HostUrl {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0

    var tag: String = ""

    var scheme: String = ""

    var host: String = ""

    var path: String = ""

    var allUrl: String = ""

    var originUrl: String = ""
}