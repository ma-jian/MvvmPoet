package com.mm.common.utils

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import com.mm.common.DefaultSDKInitialize
import java.io.File


/**
 * Created by : m
 * Date : 2022/3/1
 */

object SystemUtil {
    @JvmStatic
    fun getUserAgent(): String {
        val userAgent = StringBuilder()
        userAgent.append("CloudCC/")
        userAgent.append(getAppVersion() + "(") //应用版本号
        userAgent.append(Build.BRAND + ";") //手机厂商
        userAgent.append(Build.MODEL + ";") //手机型号
        userAgent.append("Android;")
        userAgent.append(Build.VERSION.RELEASE + ")") //系统版本
        return userAgent.toString()
    }

    /**
     * 获取本地安装的版本名即versionName,并转换成类似"820"的形式
     */
    @JvmStatic
    fun getAppVersion(): String {
        return try {
            //获取packagemanager的实例
            val packageManager = DefaultSDKInitialize.mApplication.packageManager
            val packInfo = packageManager.getPackageInfo(DefaultSDKInitialize.mApplication.packageName, 0)
            packInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            "000"
        }
    }

    /**
     * 获取缓存
     */
    @JvmStatic
    fun getCacheSize(): String {
        val size = getFolderSize(File(DefaultSDKInitialize.mApplication.cacheDir, "okhttp"))
        return size.formatFileSize()
    }

    /**
     * 获取缓存
     */
    @JvmStatic
    fun deleteCache(): Boolean {
        return deleteDir(DefaultSDKInitialize.mApplication.cacheDir)
    }

    @JvmStatic
    fun getFolderSize(file: File): Long {
        var size: Long = 0
        try {
            val fileList: Array<File> = file.listFiles() ?: arrayOf()
            for (i in fileList.indices) {
                // 如果下面还有文件
                if (fileList[i].isDirectory) {
                    size += getFolderSize(fileList[i])
                } else {
                    size += fileList[i].length()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return size
    }

    private fun deleteDir(dir: File?): Boolean {
        var delete = false
        dir?.let {
            if (dir.isDirectory) {
                val children = dir.list() ?: arrayOf()
                for (f in children.indices) {
                    val success = deleteDir(File(dir, children[f]))
                    if (!success) {
                        delete = false
                    }
                }
            } else {
                delete = dir.delete()
            }
        }
        return delete
    }


    /**
     * 检测地图应用是否安装
     * @param context
     * @param packageName
     */
    @JvmStatic
    fun checkAppsIsExist(context: Context, packageName: String): Boolean {
        var packageInfo: PackageInfo?
        try {
            packageInfo = context.packageManager.getPackageInfo(packageName, PackageManager.MATCH_UNINSTALLED_PACKAGES)
        } catch (e: java.lang.Exception) {
            packageInfo = null
            e.printStackTrace()
        }
        return packageInfo != null
    }

}