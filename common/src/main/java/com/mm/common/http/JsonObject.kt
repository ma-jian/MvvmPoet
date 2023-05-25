package com.mm.common.http

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Created by : m
 * @since 1.0
 */

class JsonObject<T> {
    companion object {
        const val PAGE_SIZE = 20
    }

    @SerializedName("data", alternate = [])
    var data: T? = null

    @SerializedName("returnInfo", alternate = ["message"])
    var returnInfo: String? = ""

    @SerializedName("returnCode", alternate = ["code"])
    var returnCode: String = "-1"

    var result: Boolean = false

    var flag: String = ""

    val isSuccess: Boolean
        get() = (result || "SUCCESS" == flag)


    override fun toString(): String {
        return JsonUtil.toJson(this)
    }
}