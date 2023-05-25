package com.mm.common.crash

import android.annotation.TargetApi
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.Message
import org.lsposed.hiddenapibypass.HiddenApiBypass

/**
 * @since 1.0
 */
@TargetApi(Build.VERSION_CODES.P)
class ActivityKillerV28 : IActivityKiller {

    override fun finishLaunchActivity(message: Message) {
        try {
            tryFinish(message)
            return
        } catch (throwable: Throwable) {
            throwable.printStackTrace()
        }
        try {
            tryFinish2(message)
        } catch (throwable: Throwable) {
            throwable.printStackTrace()
        }
    }

    @Throws(Throwable::class)
    private fun tryFinish2(message: Message) {
        val clientTransaction = message.obj
        val mActivityTokenField = clientTransaction.javaClass.getDeclaredField("mActivityToken")
        val binder = mActivityTokenField[clientTransaction] as IBinder
        finish(binder)
    }

    @Throws(Throwable::class)
    private fun tryFinish(message: Message) {
        val clientTransaction = message.obj
        val getActivityTokenMethod = clientTransaction.javaClass.getDeclaredMethod("getActivityToken")
        getActivityTokenMethod.isAccessible = true
        val binder = getActivityTokenMethod.invoke(clientTransaction) as IBinder
        finish(binder)
    }

    override fun finishResumeActivity(message: Message) {}
    override fun finishPauseActivity(message: Message) {}
    override fun finishStopActivity(message: Message) {}

    @Throws(Exception::class)
    private fun finish(binder: IBinder) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val method = HiddenApiBypass.getDeclaredMethod(Class.forName("android.app.ActivityClient"), "getInstance")
            val activityClient = method.invoke(null)
            val finishActivity = HiddenApiBypass.getDeclaredMethod(
                Class.forName("android.app.ActivityClient"),
                "finishActivity",
                IBinder::class.java,
                Int::class.javaPrimitiveType,
                Intent::class.java,
                Int::class.javaPrimitiveType
            )
            finishActivity.invoke(
                activityClient,
                binder,
                Activity.RESULT_CANCELED,
                null,
                IActivityKiller.DONT_FINISH_TASK_WITH_ACTIVITY
            )
        }
    }
}