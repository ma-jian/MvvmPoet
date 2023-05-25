@file:JvmName("ContextExt")

package com.mm.common.utils

import android.app.ActivityManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.res.Resources
import android.net.ConnectivityManager
import android.os.*
import android.provider.Settings
import android.text.Html
import android.text.Spanned
import android.text.TextUtils
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import androidx.viewbinding.ViewBinding
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.InputStreamReader
import java.io.Serializable
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.ParameterizedType

/**
 * Created by : m
 * @since 1.0
 */

val Context.windowManager: WindowManager
    get() = getSystemService(Context.WINDOW_SERVICE) as WindowManager

val Context.windowStatusBarHeight: Int
    get() {
        var statusBarHeight = 0
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            statusBarHeight = resources.getDimensionPixelSize(resourceId)
        }
        return statusBarHeight
    }

@Suppress("DEPRECATION")
fun Context?.isNetworkConnected(): Boolean {
    this?.let {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        if (networkInfo != null) {
            return networkInfo.isAvailable
        }
    }
    return false
}

/**
 * dp值转换为px
 */
fun Context?.dp2px(dp: Float): Int {
    val scale: Float = (this?.resources ?: Resources.getSystem()).displayMetrics.density
    return (dp.times(scale) + 0.5f).toInt()
}

/**
 * px值转换成dp
 */
fun Context?.px2dp(px: Float): Int {
    val scale = (this?.resources ?: Resources.getSystem()).displayMetrics.density
    return (px / scale + 0.5f).toInt()
}


/**
 * 获取屏幕宽度
 */
val Context.screenWidth
    get() = { ->
        val displayMetrics = DisplayMetrics()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            display?.getRealMetrics(displayMetrics)
        } else {
            windowManager.defaultDisplay.getRealMetrics(displayMetrics)
        }
        displayMetrics.widthPixels
    }

/**
 * 获取屏幕高度
 */
val Context.screenHeight
    get() = { ->
        val displayMetrics = DisplayMetrics()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            display?.getRealMetrics(displayMetrics)
        } else {
            windowManager.defaultDisplay.getRealMetrics(displayMetrics)
        }
        displayMetrics.heightPixels
    }

/**
 * 判断是否为空 并传入相关操作
 */
inline fun <reified T> T?.notNull(notNullAction: (T) -> Unit, nullAction: () -> Unit = {}) {
    if (this != null) {
        notNullAction.invoke(this)
    } else {
        nullAction.invoke()
    }
}

/**
 * 复制文本到粘贴板
 */
fun Context.copyToClipboard(text: String, label: String = "cloudcc") {
    if (!TextUtils.isEmpty(text)) {
        val clipData = ClipData.newPlainText(label, text)
        val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboardManager.setPrimaryClip(clipData)
    }
}

/**
 * 检查是否启用无障碍服务
 */
fun Context.checkAccessibilityServiceEnabled(serviceName: String): Boolean {
    Settings.Secure.getString(
        applicationContext.contentResolver,
        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
    )
    var result = false
    val splitter = TextUtils.SimpleStringSplitter(':')
    while (splitter.hasNext()) {
        if (splitter.next().equals(serviceName, true)) {
            result = true
            break
        }
    }
    return result
}

fun String.toHtml(flag: Int = Html.FROM_HTML_MODE_LEGACY): Spanned {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        Html.fromHtml(this, flag)
    } else {
        Html.fromHtml(this)
    }
}

/**
 * activity 获取 ViewBinding
 */

@Suppress("UNCHECKED_CAST")
@JvmName("inflateWithGeneric")
fun <VB : ViewBinding> AppCompatActivity.inflateBindingWithGeneric(layoutInflater: LayoutInflater): VB =
    withGenericBindingClass<VB>(this) { clazz ->
        clazz.getMethod("inflate", LayoutInflater::class.java).invoke(null, layoutInflater) as VB
    }

/**
 * fragment 获取 ViewBinding
 */
@Suppress("UNCHECKED_CAST")
@JvmName("inflateWithGeneric")
fun <VB : ViewBinding> Fragment.inflateBindingWithGeneric(
    layoutInflater: LayoutInflater, parent: ViewGroup?, attachToParent: Boolean,
): VB = withGenericBindingClass<VB>(this) { clazz ->
    clazz.getMethod(
        "inflate", LayoutInflater::class.java, ViewGroup::class.java, Boolean::class.java
    ).invoke(null, layoutInflater, parent, attachToParent) as VB
}

@Suppress("UNCHECKED_CAST")
private fun <VB : ViewBinding> withGenericBindingClass(
    any: Any, block: (Class<VB>) -> VB,
): VB {
    var genericSuperclass = any.javaClass.genericSuperclass
    var superclass = any.javaClass.superclass
    while (superclass != null) {
        if (genericSuperclass is ParameterizedType) {
            try {
                return block.invoke(genericSuperclass.actualTypeArguments[0] as Class<VB>)
            } catch (e: NoSuchMethodException) {
                e.printStackTrace()
            } catch (e: ClassCastException) {
                e.printStackTrace()
            } catch (e: InvocationTargetException) {
                throw e.targetException
            }
        }
        genericSuperclass = superclass.genericSuperclass
        superclass = superclass.superclass
    }
    throw IllegalArgumentException("There is no generic of ViewBinding. ")
}

@Suppress("UNCHECKED_CAST")
fun fillIntentArguments(params: Array<out Pair<String, Any?>>) = Bundle().apply {
    params.forEach {
        when (val value = it.second) {
            null -> putSerializable(it.first, null as Serializable?)
            is Int -> putInt(it.first, value)
            is Long -> putLong(it.first, value)
            is CharSequence -> putCharSequence(it.first, value)
            is String -> putString(it.first, value)
            is Float -> putFloat(it.first, value)
            is Double -> putDouble(it.first, value)
            is Char -> putChar(it.first, value)
            is Short -> putShort(it.first, value)
            is Boolean -> putBoolean(it.first, value)
            is Serializable -> putSerializable(it.first, value)
            is Bundle -> putBundle(it.first, value)
            is Parcelable -> putParcelable(it.first, value)
            is Array<*> -> when {
                value.isArrayOf<CharSequence>() -> putCharSequenceArray(
                    it.first, value as Array<CharSequence>
                )

                value.isArrayOf<Parcelable>() -> putParcelableArray(
                    it.first, value as Array<out Parcelable>
                )

                value.isArrayOf<String>() -> putStringArrayList(
                    it.first,
                    value as ArrayList<String>
                )

                else -> throw RuntimeException(
                    "Intent extra ${it.first} has wrong type ${value.javaClass.name}"
                )
            }

            is IntArray -> putIntArray(it.first, value)
            is LongArray -> putLongArray(it.first, value)
            is FloatArray -> putFloatArray(it.first, value)
            is DoubleArray -> putDoubleArray(it.first, value)
            is CharArray -> putCharArray(it.first, value)
            is ShortArray -> putShortArray(it.first, value)
            is BooleanArray -> putBooleanArray(it.first, value)
            else -> throw RuntimeException(
                "Intent extra ${it.first} has wrong type ${value.javaClass.name}"
            )
        }
    }
}

inline fun <reified F : Fragment> Context.newFragment(vararg args: Pair<String, Any?>): F {
    val bundle = fillIntentArguments(args)
    val clazz = FragmentFactory.loadFragmentClass(classLoader, F::class.java.name)
    val f = clazz.getConstructor().newInstance()
    bundle.classLoader = f.javaClass.classLoader
    f.arguments = bundle
    return f as F
}

inline fun <reified F : Fragment> Fragment.newFragment(vararg args: Pair<String, Any?>): F {
    return requireContext().newFragment(*args)
}


/**
 * 当前进程名称
 */
private var sCurProcessName: String? = null

/**
 * 是否为主进程
 */
fun Context?.isMainProcess(): Boolean {
    if (this == null) return false
    val processName = getCurProcessName()
    return if (processName != null && processName.contains(":")) {
        false
    } else processName != null && processName == packageName
}

/**
 * 获取当前进程名
 */
fun Context?.getCurProcessName(): String? {
    if (this == null) return null
    val processName = sCurProcessName
    if (!TextUtils.isEmpty(processName)) {
        return processName
    }
    try {
        val pid = android.os.Process.myPid()
        val mActivityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (appProcess in mActivityManager.runningAppProcesses) {
            if (appProcess.pid == pid) {
                sCurProcessName = appProcess.processName
                return sCurProcessName
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    sCurProcessName = curProcessNameFromProc
    return sCurProcessName
}

// 获取当前进程名
private val curProcessNameFromProc: String?
    get() {
        var cmdlineReader: BufferedReader? = null
        try {
            cmdlineReader = BufferedReader(
                InputStreamReader(
                    FileInputStream(
                        "/proc/" + android.os.Process.myPid() + "/cmdline"
                    ), "iso-8859-1"
                )
            )
            var c: Int
            val processName = StringBuilder()
            while (cmdlineReader.read().also { c = it } > 0) {
                processName.append(c.toChar())
            }
            return processName.toString()
        } catch (e: Throwable) {
            // ignore
        } finally {
            if (cmdlineReader != null) {
                try {
                    cmdlineReader.close()
                } catch (e: Exception) {
                    // ignore
                }
            }
        }
        return null
    }

/**
 * 开启震动
 */
fun Context?.vibrator(vararg timings: Long = longArrayOf(0, 500, 500, 500)) {
    this?.getSystemService(Context.VIBRATOR_SERVICE)?.let {
        val vibrator = it as Vibrator
        if (Build.VERSION.SDK_INT >= 26) {
            vibrator.vibrate(
                VibrationEffect.createWaveform(
                    timings,
                    VibrationEffect.DEFAULT_AMPLITUDE
                )
            )
        } else {
            vibrator.vibrate(150)
        }
    }
}

fun Context?.showSoftInput(view: View) {
    this?.apply {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if (view is EditText) {
            view.requestFocus()
        }
        view.isFocusableInTouchMode = true
        imm.showSoftInput(view, 0);
    }
}

fun Context?.hideSoftInput(view: View) {
    this?.apply {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
    }
}