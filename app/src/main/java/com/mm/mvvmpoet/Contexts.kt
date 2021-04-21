package com.mm.mvvmpoet

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import androidx.fragment.app.Fragment
import java.io.Serializable
import java.util.*

/**
 * Created by : majian
 * Date : 4/20/21
 * Describe :
 */

inline fun <reified T : Activity> Context?.startActivity(vararg params: Pair<String, Any?>) =
    this?.startActivity(createIntent(T::class.java, params))

inline fun <reified T : Activity> Fragment?.startActivity(vararg params: Pair<String, Any?>) =
    this?.context?.also { startActivity(it.createIntent(T::class.java, params)) }

inline fun <reified T : Activity> Activity?.startActivityForResult(requestCode: Int, vararg params: Pair<String, Any?>) =
    this?.startActivityForResult(createIntent(T::class.java, params), requestCode)

inline fun <reified T : Activity> Fragment?.startActivityForResult(requestCode: Int, vararg params: Pair<String, Any?>) =
    this?.activity?.startActivityForResult(context.createIntent(T::class.java, params), requestCode)

fun <T> Context?.createIntent(clazz: Class<out T>, params: Array<out Pair<String, Any?>>): Intent {
    val intent = Intent(this, clazz)
    if (params.isNotEmpty()){
        intent.putExtras(fillIntentArguments(params))
    }
    return intent
}

fun fillIntentArguments( params: Array<out Pair<String, Any?>>) = Bundle().apply {
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
                value.isArrayOf<CharSequence>() -> putCharSequenceArray(it.first, value as Array<CharSequence>)
                value.isArrayOf<Parcelable>() -> putParcelableArray(it.first, value as Array<out Parcelable>)
                value.isArrayOf<String>() -> putStringArrayList(it.first, value as ArrayList<String>)
                else -> throw RuntimeException("Intent extra ${it.first} has wrong type ${value.javaClass.name}")
            }
            is IntArray -> putIntArray(it.first, value)
            is LongArray -> putLongArray(it.first, value)
            is FloatArray -> putFloatArray(it.first, value)
            is DoubleArray -> putDoubleArray(it.first, value)
            is CharArray -> putCharArray(it.first, value)
            is ShortArray -> putShortArray(it.first, value)
            is BooleanArray -> putBooleanArray(it.first, value)
            else -> throw RuntimeException("Intent extra ${it.first} has wrong type ${value.javaClass.name}")
        }
    }
}

inline fun <reified F : Fragment> Context.newFragment(vararg args: Pair<String, Any?>): F {
    val bundle = fillIntentArguments(args)
    return Fragment.instantiate(this, F::class.java.name, bundle) as F
}
