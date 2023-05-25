package com.mm.common.http

import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.JsonPrimitive
import java.lang.reflect.Type

/**
 * Created by : m
 * @since 1.0
 * Gson 解析json数据
 */
object JsonUtil {
    private val gson = GsonBuilder() //.excludeFieldsWithoutExposeAnnotation() //不对没有用@Expose注解的属性进行操作
        //.enableComplexMapKeySerialization() //当Map的key为复杂对象时,需要开启该方法
        .serializeNulls() //当字段值为空或null时，依然对该字段进行转换
        .setDateFormat("yyyy-MM-dd HH:mm:ss") //时间转化为特定格式
        //            .setPrettyPrinting() //对结果进行格式化，增加换行
        .disableHtmlEscaping() //防止特殊字符出现乱码
        .create()
    private val gsonNoNulls = GsonBuilder().disableHtmlEscaping() //防止特殊字符出现乱码
        .create()

    @JvmStatic
    fun <T> fromJson(element: JsonElement?, type: Type): T {
        return gson.fromJson(element, type)
    }

    @JvmStatic
    fun <T> fromJson(string: String?, type: Type): T {
        return gson.fromJson(string, type)
    }

    @JvmStatic
    fun <T> fromJson(any: Any?, type: Type): T {
        val jsonElement = JsonParser.parseString(toJson(any))
        return fromJson(jsonElement, type)
    }

    @JvmStatic
    fun <T> fromJson(string: String?, clazz: Class<T>): T {
        return gson.fromJson(string, clazz)
    }

    /**
     * 解析data数据单个字段
     *
     * @param any    data数据
     * @param fieldName 字段名称
     * @return JsonElement 注意转换为对应值类型
     * @throws IllegalStateException 解析类型为JsonArray 仅支持数组数目为1 的情况，正常情况解析列表单个字段无意义
     */
    @JvmStatic
    fun fromField(any: Any?, fieldName: String): JsonElement {
        var jsonElement = JsonParser.parseString(toJson(any))
        if (jsonElement.isJsonPrimitive) {
            jsonElement = JsonParser.parseString(jsonElement.asString)
        }
        if (jsonElement.isJsonObject) {
            val element = (jsonElement as JsonObject)[fieldName]
            return if (element == null || element.isJsonNull) JsonPrimitive("") else element
        } else if (jsonElement.isJsonArray) {
            val jsonArray = jsonElement as JsonArray
            return if (jsonArray.size() == 1) {
                val element = jsonArray[0]
                if (element == null || element.isJsonNull) JsonPrimitive("") else element
            } else {
                throw IllegalStateException()
            }
        } else if (jsonElement.isJsonNull) {
            return JsonPrimitive("")
        }
        return jsonElement
    }

    /**
     * @param any data数据
     * @return json 字符串
     */
    @JvmStatic
    fun toJson(any: Any?): String {
        return gsonNoNulls.toJson(any)
    }

    /**
     * @param object
     * @return
     */
    @JvmStatic
    fun parseString(any: Any?): JsonElement {
        return JsonParser.parseString(toJson(any))
    }
}