@file:JvmName("CoilImage")

package com.mm.common.utils

import android.graphics.drawable.Drawable
import android.os.Build
import android.widget.ImageView
import androidx.core.content.ContextCompat
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.CachePolicy
import coil.request.Disposable
import coil.request.ErrorResult
import coil.request.ImageRequest
import coil.request.SuccessResult
import coil.target.Target
import coil.transform.CircleCropTransformation
import coil.transform.RoundedCornersTransformation
import com.mm.common.Constants
import com.mm.common.DefaultSDKInitialize
import com.mm.common.R
import com.mm.http.SslSocketFactory
import com.tencent.mmkv.MMKV
import okhttp3.OkHttpClient

/**
 * Created by : m
 * Date : 2022/4/22
 * 图片加载器
 */
/*** 占位图  */
private val defaultPlaceholderImage: Int = R.drawable.ic_default_image

private val defaultCircleImage: Int = R.drawable.ic_defualt_person

/*** 错误图  */
private val defaultErrorImage: Int = R.drawable.ic_default_error

val defaultImageLoader = ImageLoader.Builder(DefaultSDKInitialize.mApplication).crossfade(true)
    .placeholder(defaultPlaceholderImage).error(defaultErrorImage).components {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            add(ImageDecoderDecoder.Factory())
        } else {
            add(GifDecoder.Factory())
        }
    }.memoryCachePolicy(CachePolicy.ENABLED).okHttpClient {
        OkHttpClient.Builder().sslSocketFactory(
            SslSocketFactory.sSLSocketFactory,
            SslSocketFactory.trustManager
        ).build()
    }.build()

fun ImageView?.load(url: Any?) = load(url, defaultPlaceholderImage, defaultErrorImage)

fun ImageView?.load(
    url: Any?, placeholderImage: Int = defaultPlaceholderImage, listener: ImageRequest.Listener
) = this?.loadUrl(url) {
    if (placeholderImage > 0) {
        placeholder(placeholderImage)
    }
    error(defaultErrorImage)
    listener(listener)
}

fun ImageView?.load(
    url: Any?, placeholderImage: Int = defaultPlaceholderImage, errorImage: Int = defaultErrorImage
): Disposable? {
    val newLoader =
        if (placeholderImage != defaultPlaceholderImage || errorImage != defaultErrorImage) defaultImageLoader.newBuilder()
            .placeholder(placeholderImage).error(errorImage).build()
        else defaultImageLoader
    return this?.loadUrl(url, newLoader)
}

fun ImageView?.loadCircle(url: Any?) = loadCircle(url, defaultCircleImage)

fun ImageView?.loadCircle(
    url: Any?, defaultCircleImage: Drawable? = ContextCompat.getDrawable(
        this?.context!!, R.drawable.ic_defualt_person
    )
) = this?.loadUrl(url) {
    crossfade(true)
    placeholder(defaultCircleImage)
    error(defaultCircleImage)
    memoryCachePolicy(CachePolicy.DISABLED)
    transformations(CircleCropTransformation())
}

fun ImageView?.loadCircle(url: Any?, defaultCircleImage: Int = R.drawable.ic_defualt_person) =
    loadCircle(url, ContextCompat.getDrawable(this?.context!!, defaultCircleImage))

fun ImageView?.loadRoundImage(
    url: Any?,
    topLeft: Float = 0f,
    topRight: Float = 0f,
    bottomLeft: Float = 0f,
    bottomRight: Float = 0f
) = this?.loadUrl(url) {
    crossfade(true)
    placeholder(defaultPlaceholderImage)
    error(defaultErrorImage)
    memoryCachePolicy(CachePolicy.DISABLED)
    transformations(RoundedCornersTransformation(topLeft, topRight, bottomLeft, bottomRight))
}

inline fun ImageView.loadUrl(
    data: Any?,
    imageLoader: ImageLoader = defaultImageLoader,
    builder: ImageRequest.Builder.() -> Unit = {}
): Disposable {
//    val token = MMKV.defaultMMKV().getString(Constants.KEY.ACCESS_TOKEN, "")
    val request = ImageRequest.Builder(context)
        .data(data)
//        .addHeader("accessToken", token ?: "")
        .target(this)
        .apply(builder)
        .listener(object : ImageRequest.Listener {
            override fun onError(request: ImageRequest, result: ErrorResult) {
                super.onError(request, result)
                LogUtil.e("CoilImage:onError ${result.request.data}\n${result.throwable}")
            }

            override fun onSuccess(request: ImageRequest, result: SuccessResult) {
                super.onSuccess(request, result)
                LogUtil.e("CoilImage:onSuccess ${result.request.data}")
            }
        })
        .build()
    return imageLoader.enqueue(request)
}

fun requestBitmap(url: Any?, target: Target?) {
    val request = ImageRequest.Builder(DefaultSDKInitialize.mApplication).data(url).allowHardware(false)
        .target(target).build()
    defaultImageLoader.enqueue(request)
}