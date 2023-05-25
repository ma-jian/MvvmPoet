package com.mm.common.okbus;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Messenger;

import androidx.annotation.Nullable;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @since 1.0
 * Created by m
 * 客户端的唤醒服务
 */

public class NoticeService extends Service {
    private final CountDownLatch latch = new CountDownLatch(1);
    private final AtomicReference<Messenger> resultRef = new AtomicReference<>();

    /**
     * 收到唤醒通知之后，初始化模块，并自动去服务器注册
     */
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        BaseModule mBaseModule = BaseAppModuleApp.getBaseApplication().mBaseModule;
        if (!mBaseModule.isConnected.get()) {
            mBaseModule.init(latch, resultRef);
            mBaseModule.afterConnected(getApplication());
            try {
                latch.await(2000, TimeUnit.SECONDS);
            } catch (Exception e) { //等待中断
                e.printStackTrace();
            }
        }
        return mBaseModule.mWorkThread.clientHandler.getBinder();
    }
}