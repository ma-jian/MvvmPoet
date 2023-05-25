package com.mm.common.webview

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.mm.router.Router

/**
 * Created by : m
 * Date : 2022/3/3
 */
object WebActivityRouter {
    @JvmStatic
    fun startFromWeb(intent: IntentBuilder): Boolean {
        val result = if (intent.mUrl.startsWith("cloudcc")) {
            Router.init().open(intent.mUrl).navigation()
        } else {
            false
        }
        if (result) {
            intent.mRouterResult?.onActivityFound()
        } else {
            intent.mRouterResult?.onActivityNotFound()
        }
        return result
    }

    class IntentBuilder internal constructor(internal val mUrl: String) {
        var mRouterResult: RouterActivityResult? = null

        fun setRouterActivityResult(routerActivityResult: RouterActivityResult?): IntentBuilder {
            mRouterResult = routerActivityResult
            return this
        }
    }

    @JvmStatic
    fun systemLink(context: Context, url: String): Boolean {
        when (url) {
            "tel:" -> {
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse(url))
                context.startActivity(intent)
                return true
            }
            "sms:", "smsto:" -> {
                val intent = Intent(Intent.ACTION_SENDTO, Uri.parse(url))
                context.startActivity(intent)
                return true
            }
            "mailto:" -> {
                val intent = Intent(Intent.ACTION_SENDTO, Uri.parse(url))
                context.startActivity(intent)
                return true
            }
            "market:" -> {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                context.startActivity(intent)
                return true
            }
            else -> {
                return false
            }
        }
    }
}