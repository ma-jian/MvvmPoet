package com.mm.common

import androidx.annotation.NonNull


/**
 * Date : 2023/4/12
 * 输入型回调
 * @since 1.0
 */

interface InputCallback<I> {

    fun onCall(@NonNull res: I)
}