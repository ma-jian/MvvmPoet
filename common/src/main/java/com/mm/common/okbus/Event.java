package com.mm.common.okbus;

import android.os.Message;

import androidx.annotation.NonNull;

/**
 * @since 1.0
 * okbus 事件总线 普通事件消息回调
 */

public interface Event {

    void call(@NonNull Message msg);
}
