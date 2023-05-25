package com.mm.common

import android.text.TextUtils

/**
 * @since 1.0
 * Describe:路由路径
 *          scheme  host  path  params
 * 路由格式 cloudcc://app/module分类/path?a=12
 *
 */
object RoutPath {
    const val PATH_WELCOME = "cloudcc://app/welcome"    //欢迎页
    const val PATH_HOME_MAIN = "cloudcc://app/main"     //首页
    const val PATH_HOME_GUIDE = "cloudcc://app/guide"     //引导页


    /**
     * 当前路径排除
     * 欢迎页 引导页 首页
     */
    fun excludePath(str: String?): Boolean {
        return !TextUtils.isEmpty(str) && (!str.equals(PATH_WELCOME) && !str.equals(PATH_HOME_MAIN) && !str.equals(PATH_HOME_GUIDE))
    }

}