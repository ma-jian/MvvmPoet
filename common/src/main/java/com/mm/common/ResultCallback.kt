package com.mm.common

import androidx.annotation.NonNull


/**
 * Date : 2023/4/12
 * 结果型回调
 */

interface ResultCallback<I, R> {

    fun onResult(@NonNull res: I): R?
}