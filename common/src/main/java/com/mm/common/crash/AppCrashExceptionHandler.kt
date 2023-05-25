package com.mm.common.crash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.mm.common.ActivityDelegate
import com.mm.common.BuildConfig
import com.mm.common.DefaultSDKInitialize
import com.mm.common.utils.LogUtil

/**
 * Date : 2023/2/28
 * @since 1.0
 */
class AppCrashExceptionHandler private constructor() : Thread.UncaughtExceptionHandler {
    private var sActivityKiller: IActivityKiller? = null
    private val TAG = "AppCrashExceptionHandler"
    private val mainHandler: Handler = Handler(Looper.getMainLooper())

    /**
     * 测试环境不进行崩溃拦截
     */
    fun init() {
        if (!BuildConfig.BUILD_TYPE.equals("debug")) {
            initActivityKiller()
            Thread.setDefaultUncaughtExceptionHandler(this)
        }
    }

    override fun uncaughtException(t: Thread, e: Throwable) {
        //子线程崩溃不影响app运行仅打印日志用于查找问题
        LogUtil.e(TAG, "uncaughtException : " + Log.getStackTraceString(e))
        if (t === Looper.getMainLooper().thread) {
            uiSafeMode()
        }
    }

    private fun uiSafeMode() {
        try {
            //fix No Looper; Looper.prepare() wasn't called on this thread.
            if (Looper.myLooper() == null) {
                Looper.prepare()
            }
            Looper.loop()
        } catch (e: Exception) {
//            uncaughtException(Thread.currentThread(), e);
        }
    }

    private fun initActivityKiller() {
        //各版本android的ActivityManager获取方式，finishActivity的参数，token(binder对象)的获取不一样
        sActivityKiller = if (Build.VERSION.SDK_INT >= 28) {
            ActivityKillerV28()
        } else if (Build.VERSION.SDK_INT >= 26) {
            ActivityKillerV26()
        } else if (Build.VERSION.SDK_INT == 25 || Build.VERSION.SDK_INT == 24) {
            ActivityKillerV24_V25()
        } else {
            ActivityKillerV21_V23()
        }
        try {
            hookH()
            //            hookState();
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    @Throws(Exception::class)
    private fun hookH() {
        val LAUNCH_ACTIVITY = 100
        val PAUSE_ACTIVITY = 101
        val PAUSE_ACTIVITY_FINISHING = 102
        val STOP_ACTIVITY_HIDE = 104
        val RESUME_ACTIVITY = 107
        val DESTROY_ACTIVITY = 109
        val activityThreadClass = Class.forName("android.app.ActivityThread")
        val activityThread = activityThreadClass.getDeclaredMethod("currentActivityThread").invoke(null)
        val mhField = activityThreadClass.getDeclaredField("mH")
        mhField.isAccessible = true
        val mhHandler = mhField[activityThread] as Handler ?: return
        val callbackField = Handler::class.java.getDeclaredField("mCallback")
        callbackField.isAccessible = true
        callbackField[mhHandler] = Handler.Callback { msg ->
            if (Build.VERSION.SDK_INT >= 28) { //android P 生命周期全部走这
                val EXECUTE_TRANSACTION = 159
                if (msg.what == EXECUTE_TRANSACTION) {
                    try {
                        mhHandler.handleMessage(msg)
                    } catch (throwable: Throwable) {
                        sActivityKiller!!.finishLaunchActivity(msg)
                        openCrashActivity(throwable)
                    }
                    return@Callback true
                }
                return@Callback false
            }
            when (msg.what) {
                LAUNCH_ACTIVITY -> {
                    try {
                        mhHandler.handleMessage(msg)
                    } catch (throwable: Throwable) {
                        sActivityKiller!!.finishLaunchActivity(msg)
                        openCrashActivity(throwable)
                    }
                    return@Callback true
                }

                RESUME_ACTIVITY -> {
                    try {
                        mhHandler.handleMessage(msg)
                    } catch (throwable: Throwable) {
                        sActivityKiller!!.finishResumeActivity(msg)
                        openCrashActivity(throwable)
                    }
                    return@Callback true
                }

                PAUSE_ACTIVITY_FINISHING, PAUSE_ACTIVITY -> {
                    try {
                        mhHandler.handleMessage(msg)
                    } catch (throwable: Throwable) {
                        sActivityKiller!!.finishPauseActivity(msg)
                        openCrashActivity(throwable)
                    }
                    return@Callback true
                }

                STOP_ACTIVITY_HIDE -> {
                    try {
                        mhHandler.handleMessage(msg)
                    } catch (throwable: Throwable) {
                        sActivityKiller!!.finishStopActivity(msg)
                        openCrashActivity(throwable)
                    }
                    return@Callback true
                }

                DESTROY_ACTIVITY -> {
                    try {
                        mhHandler.handleMessage(msg)
                    } catch (throwable: Throwable) {
                        throwable.printStackTrace()
                        openCrashActivity(throwable)
                    }
                    return@Callback true
                }
            }
            false
        }
    }

    @SuppressLint("CheckResult")
    @Throws(Exception::class)
    private fun hookState() {
        val activityThreadClass = Class.forName("android.app.ActivityThread")
        val currentActivityThreadMethod = activityThreadClass.getDeclaredMethod("currentActivityThread")
        currentActivityThreadMethod.isAccessible = true
        val currentActivityThread = currentActivityThreadMethod.invoke(null)
        val mInstrumentationField = activityThreadClass.getDeclaredField("mInstrumentation")
        mInstrumentationField.isAccessible = true
        val proxy = InstrumentationProxy()
        mInstrumentationField[currentActivityThread] = proxy
    }

    private fun openCrashActivity(throwable: Throwable) {
        LogUtil.e(TAG, Log.getStackTraceString(throwable))
        if (throwable !is IllegalArgumentException) {
            val intent = Intent(DefaultSDKInitialize.mApplication, CrashHandlerActivity::class.java)
            intent.putExtra("throwable", throwable)
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            if (ActivityDelegate.delegate.get()?.activity != null) {
                ActivityDelegate.delegate.get()?.activity?.startActivity(intent)
            } else {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                DefaultSDKInitialize.mApplication.startActivity(intent)
            }
        }
    }

    companion object {
        private var exceptionHandler: AppCrashExceptionHandler? = null
        val instance: AppCrashExceptionHandler
            get() {
                synchronized(AppCrashExceptionHandler::class.java) {
                    if (exceptionHandler == null) {
                        exceptionHandler = AppCrashExceptionHandler()
                    }
                }
                return exceptionHandler!!
            }
    }
}