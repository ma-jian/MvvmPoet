package com.mm.common.okbus;

import android.os.Message;

/**
 * @since 1.0
 * okbus 事件总线服务信息回调
 */

public interface CallBack<T> {
    T onCall(Message msg);
}
