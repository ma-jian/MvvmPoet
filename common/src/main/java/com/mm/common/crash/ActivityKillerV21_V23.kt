package com.mm.common.crash

import android.app.Activity
import android.content.Intent
import android.os.IBinder
import android.os.Message

/**
 * @since 1.0
 */
class ActivityKillerV21_V23 : IActivityKiller {

    override fun finishLaunchActivity(message: Message) {
        try {
            val activityClientRecord = message.obj
            val tokenField = activityClientRecord.javaClass.getDeclaredField("token")
            tokenField.isAccessible = true
            val binder = tokenField[activityClientRecord] as IBinder
            finish(binder)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun finishResumeActivity(message: Message) {
        try {
            finish(message.obj as IBinder)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun finishPauseActivity(message: Message) {
        try {
            finish(message.obj as IBinder)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun finishStopActivity(message: Message) {
        try {
            finish(message.obj as IBinder)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Throws(Exception::class)
    private fun finish(binder: IBinder) {
        val activityManagerNativeClass = Class.forName("android.app.ActivityManagerNative")
        val getDefaultMethod = activityManagerNativeClass.getDeclaredMethod("getDefault")
        val activityManager = getDefaultMethod.invoke(null)
        val finishActivityMethod = activityManager.javaClass.getDeclaredMethod(
            "finishActivity",
            IBinder::class.java,
            Int::class.javaPrimitiveType,
            Intent::class.java,
            Boolean::class.javaPrimitiveType
        )
        finishActivityMethod.invoke(activityManager, binder, Activity.RESULT_CANCELED, null, false)
    }
}